package com.holyes.ccssend5.lib.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @ClassName: BluetoothService
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:25
 */
public class BluetoothService {

        // Debugging
        private static final boolean D = true;

        // Name for the SDP record when creating server socket
        private static final String NAME = "BTPrinter";

        // Unique UUID for this application
        private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");	//change by chongqing jinou

        // Member fields
        private final BluetoothAdapter mAdapter;
        private final Handler mHandler;
        private AcceptThread mAcceptThread;
        private ConnectThread mConnectThread;
        private ConnectedThread mConnectedThread;
        private int mState;
        
        // 连接的设备信息
        private String mConnectedDeviceName = "";
        private String mConnectedDeviceAddress = "";

        // Constants that indicate the current connection state
        public static final int STATE_NONE = 0;       // we're doing nothing
        public static final int STATE_LISTEN = 1;     // now listening for incoming connections
        public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
        public static final int STATE_CONNECTED = 3;  // now connected to a remote device

        /**
         * Constructor. Prepares a new BTPrinter session.
         * @param context  The UI Activity Context
         * @param handler  A Handler to send messages back to the UI Activity
         */
        public BluetoothService(Context context, Handler handler) {
            mAdapter = BluetoothAdapter.getDefaultAdapter();
            mState = STATE_NONE;
            mHandler = handler;
        }

        /**
         * Set the current state of the connection
         * @param state  An integer defining the current connection state
         */
        private synchronized void setState(int state) {
            mState = state;

            // Give the new state to the Handler so the UI Activity can update
            mHandler.obtainMessage(BluetoothUtil.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        }

        /**
         * Return the current connection state. */
        public synchronized int getState() {
            return mState;
        }
        
        /**
         * 设置连接的设备信息
         * @param deviceName 设备名称
         * @param deviceAddress 设备地址
         */
        public void setConnectedDeviceInfo(String deviceName, String deviceAddress) {
            mConnectedDeviceName = deviceName;
            mConnectedDeviceAddress = deviceAddress;
        }
        
        /**
         * 获取连接的设备名称
         * @return 设备名称
         */
        public String getConnectedDeviceName() {
            return mConnectedDeviceName;
        }
        
        /**
         * 获取连接的设备地址
         * @return 设备地址
         */
        public String getConnectedDeviceAddress() {
            return mConnectedDeviceAddress;
        }

        /**
         * Start the service. Specifically start AcceptThread to begin a
         * session in listening (server) mode. Called by the Activity onResume() */
        public synchronized void start()
        {

            // Cancel any thread attempting to make a connection
            if (mConnectThread != null)
            {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null)
            {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            // Start the thread to listen on a BluetoothServerSocket
            if (mAcceptThread == null)
            {
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
            }
            setState(STATE_LISTEN);
        }

        /**
         * Start the ConnectThread to initiate a connection to a remote device.
         * @param device  The BluetoothDevice to connect
         */
        public synchronized void connect(BluetoothDevice device) {

            // Cancel any thread attempting to make a connection
            if (mState == STATE_CONNECTING)
            {
                if (mConnectThread != null)
                {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null)
            {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            // Start the thread to connect with the given device
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
            setState(STATE_CONNECTING);
        }

        /**
         * Start the ConnectedThread to begin managing a Bluetooth connection
         * @param socket  The BluetoothSocket on which the connection was made
         * @param device  The BluetoothDevice that has been connected
         */
        public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

            // Cancel the thread that completed the connection
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

            // Cancel the accept thread because we only want to connect to one device
            if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

            // 设置连接的设备信息
            String deviceName = device.getName();
            if (deviceName == null || deviceName.isEmpty()) {
                // 如果设备名称为空，优先使用已设置的设备名称
                if (mConnectedDeviceName != null && !mConnectedDeviceName.isEmpty()) {
                    deviceName = mConnectedDeviceName;
                } else {
                    // 如果已设置的设备名称也为空，使用设备地址作为显示名称
                    deviceName = device.getAddress();
                }
            }
            // 确保设备名称不为空
            if (deviceName == null || deviceName.isEmpty()) {
                deviceName = device.getAddress();
            }
            mConnectedDeviceName = deviceName;
            mConnectedDeviceAddress = device.getAddress();

            // Start the thread to manage the connection and perform transmissions
            mConnectedThread = new ConnectedThread(socket);
            mConnectedThread.start();

            // 发送蓝牙名称和地址到uiactivity
            Message msg = mHandler.obtainMessage(BluetoothUtil.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothUtil.DEVICE_NAME, mConnectedDeviceName);
            bundle.putString(BluetoothUtil.DEVICE_ADDRESS, mConnectedDeviceAddress);
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            setState(STATE_CONNECTED);
        }

        /**
         * Stop all threads
         */
        public synchronized void stop() {
            setState(STATE_NONE);
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
            if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
            if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
            
            // 清理设备信息
            mConnectedDeviceName = "";
            mConnectedDeviceAddress = "";
        }

        /**
         * Write to the ConnectedThread in an unsynchronized manner
         * @param out The bytes to write
         * @see ConnectedThread#write(byte[])
         */
        public void write(byte[] out) {
            // Create temporary object
            ConnectedThread r;
            // Synchronize a copy of the ConnectedThread
            synchronized (this) {
                if (mState != STATE_CONNECTED) return;
                r = mConnectedThread;
            }
            // Perform the write unsynchronized
            r.write(out);
        }

        /**
         * Indicate that the connection attempt failed and notify the UI Activity.
         */
        private void connectionFailed() {
            setState(STATE_LISTEN);

            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(BluetoothUtil.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothUtil.TOAST, "无法连接到设备，连接超时");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        /**
         * Indicate that the connection was lost and notify the UI Activity.
         */
        private void connectionLost() {
            //setState(STATE_LISTEN);

            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(BluetoothUtil.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothUtil.TOAST, "Device connection was lost");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        /**
         * This thread runs while listening for incoming connections. It behaves
         * like a server-side client. It runs until a connection is accepted
         * (or until cancelled).
         */
        private class AcceptThread extends Thread {
            // The local server socket
            private final BluetoothServerSocket mmServerSocket;

            public AcceptThread() {
                BluetoothServerSocket tmp = null;

                // Create a new listening server socket
                try {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                } catch (IOException e) {
                }
                mmServerSocket = tmp;
            }

            public void run() {
                setName("AcceptThread");
                BluetoothSocket socket = null;

                // Listen to the server socket if we're not connected
                while (mState != STATE_CONNECTED) {
                    try {
                        // This is a blocking call and will only return on a
                        // successful connection or an exception
                        socket = mmServerSocket.accept();
                    } catch (IOException e) {
                        break;
                    }

                    // If a connection was accepted
                    if (socket != null) {
                        synchronized (BluetoothService.this) {
                            switch (mState) {
                                case STATE_LISTEN:
                                case STATE_CONNECTING:
                                    // Situation normal. Start the connected thread.
                                    connected(socket, socket.getRemoteDevice());
                                    break;
                                case STATE_NONE:
                                case STATE_CONNECTED:
                                    // Either not ready or already connected. Terminate new socket.
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

            public void cancel() {
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                }
            }
        }


        /**
         * This thread runs while attempting to make an outgoing connection
         * with a device. It runs straight through; the connection either
         * succeeds or fails.
         */
        private class ConnectThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final BluetoothDevice mmDevice;
            private volatile boolean cancelled = false;

            public ConnectThread(BluetoothDevice device) {
                mmDevice = device;
                BluetoothSocket tmp = null;

                // Get a BluetoothSocket for a connection with the
                // given BluetoothDevice
                try {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                }
                mmSocket = tmp;
            }

            public void run() {
                setName("ConnectThread");

                // Always cancel discovery because it will slow down a connection
                mAdapter.cancelDiscovery();

                // 启动超时检查线程
                Thread timeoutThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(BluetoothUtil.CONNECTION_TIMEOUT);
                            // 超时后取消连接
                            if (!cancelled && mmSocket != null) {
                                try {
                                    mmSocket.close();
                                } catch (IOException e) {
                                    // 忽略关闭异常
                                }
                                connectionFailed();
                                // 启动服务以重新监听
                                BluetoothService.this.start();
                            }
                        } catch (InterruptedException e) {
                            // 连接成功或取消，超时线程被中断
                        }
                    }
                });
                timeoutThread.start();

                // Make a connection to the BluetoothSocket
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmSocket.connect();
                    
                    // 连接成功，取消超时检查
                    cancelled = true;
                    timeoutThread.interrupt();
                    
                } catch (IOException e) {
                    cancelled = true;
                    timeoutThread.interrupt();
                    connectionFailed();
                    // Close the socket
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                    }
                    // Start the service over to restart listening mode
                    BluetoothService.this.start();
                    return;
                }

                // Reset the ConnectThread because we're done
                synchronized (BluetoothService.this) {
                    mConnectThread = null;
                }

                // Start the connected thread
                connected(mmSocket, mmDevice);
            }

            public void cancel() {
                cancelled = true;
                try {
                    mmSocket.close();
                } catch (IOException e) {
                }
            }
        }

        /**
         * This thread runs during a connection with a remote device.
         * It handles all incoming and outgoing transmissions.
         */
        private class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket) {
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the BluetoothSocket input and output streams
                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {
                int bytes;

                // Keep listening to the InputStream while connected
                while (true) {
                    try {
                        byte[] buffer = new byte[512];
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);

                        if(bytes>0)
                        {
                            // Send the obtained bytes to the UI Activity
                            mHandler.obtainMessage(BluetoothUtil.MESSAGE_READ, bytes, -1, buffer)
                                    .sendToTarget();
                        }
                        else
                        {
                            connectionLost();

                            //add by chongqing jinou
                            if(mState != STATE_NONE)
                            {
                                // Start the service over to restart listening mode
                                BluetoothService.this.start();
                            }
                            break;
                        }
                    } catch (IOException e) {
                        connectionLost();

                        //add by chongqing jinou
                        if(mState != STATE_NONE)
                        {
                            // Start the service over to restart listening mode
                            BluetoothService.this.start();
                        }
                        break;
                    }
                }
            }

            /**
             * Write to the connected OutStream.
             * @param buffer  The bytes to write
             */
            public void write(byte[] buffer)
            {
                try {

                    mmOutStream.write(buffer);
                    mmOutStream.flush();//>>>

                    // Share the sent message back to the UI Activity
                    mHandler.obtainMessage(BluetoothUtil.MESSAGE_WRITE, -1, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                }
            }

            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                }
            }
        }
    }

