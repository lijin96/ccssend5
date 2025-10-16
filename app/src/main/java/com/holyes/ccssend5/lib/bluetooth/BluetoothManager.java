package com.holyes.ccssend5.lib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import com.holyes.ccssend5.lib.SysUserInfo;

/**
 * @ClassName: BluetoothManager
 * @Description: 蓝牙服务全局管理类（单例模式）
 * @Author: AI Assistant
 * @Date: 2025/10/15
 */
public class BluetoothManager {
    
    private static BluetoothManager instance;
    private BluetoothService bluetoothService;
    private BluetoothAdapter bluetoothAdapter;
    
    // 私有构造函数，防止外部实例化
    private BluetoothManager() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized BluetoothManager getInstance() {
        if (instance == null) {
            instance = new BluetoothManager();
        }
        return instance;
    }
    
    /**
     * 初始化蓝牙服务（如果未初始化）
     * @param context 上下文
     * @param handler 消息处理器
     */
    public void initBluetoothService(Context context, Handler handler) {
        if (bluetoothService == null) {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                bluetoothService = new BluetoothService(context, handler);
            }
        }
    }
    
    /**
     * 初始化蓝牙服务并自动连接上次保存的设备
     * @param context 上下文
     * @param handler 消息处理器
     * @return true 表示开始自动连接，false 表示没有保存的设备地址
     */
    public boolean initBluetoothServiceAndAutoConnect(Context context, Handler handler) {
        // 如果服务为null或已停止，则重新创建
        if (bluetoothService == null || bluetoothService.getState() == BluetoothService.STATE_NONE) {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                bluetoothService = new BluetoothService(context, handler);
            }
        }
        
        // 尝试自动连接上次保存的设备
        return tryAutoConnectLastDevice(context);
    }
    
    /**
     * 尝试自动连接上次保存的蓝牙设备
     * @param context 上下文
     * @return true 表示开始连接，false 表示没有保存的设备地址
     */
    public boolean tryAutoConnectLastDevice(Context context) {
        SysUserInfo sysUserInfo = new SysUserInfo(context);
        String connectedDeviceAddress = sysUserInfo.getConnectedBluetoothAddress();
        String connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
        
        if (!connectedDeviceAddress.isEmpty()) {
            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(connectedDeviceAddress);
                if (bluetoothService != null) {
                    // 设置设备名称和地址，用于连接成功后的UI显示
                    bluetoothService.setConnectedDeviceInfo(connectedDeviceName, connectedDeviceAddress);
                    bluetoothService.connect(device);
                    return true;
                }
            } catch (Exception e) {
                // 设备地址无效或设备不存在
                e.printStackTrace();
            }
        }
        return false;
    }
    
    /**
     * 获取蓝牙服务实例
     * 如果未初始化，则返回 null
     */
    public BluetoothService getBluetoothService() {
        return bluetoothService;
    }
    
    /**
     * 获取连接的设备名称
     * @return 设备名称，如果未连接则返回空字符串
     */
    public String getConnectedDeviceName() {
        if (bluetoothService != null) {
            return bluetoothService.getConnectedDeviceName();
        }
        return "";
    }
    
    /**
     * 获取连接的设备地址
     * @return 设备地址，如果未连接则返回空字符串
     */
    public String getConnectedDeviceAddress() {
        if (bluetoothService != null) {
            return bluetoothService.getConnectedDeviceAddress();
        }
        return "";
    }
    
    /**
     * 判断蓝牙是否已连接
     * @return true 表示已连接，false 表示未连接
     */
    public boolean isBluetoothConnected() {
        if (bluetoothService == null) {
            return false;
        }
        return bluetoothService.getState() == BluetoothService.STATE_CONNECTED;
    }
    
    /**
     * 获取蓝牙连接状态
     * @return 连接状态码
     */
    public int getBluetoothState() {
        if (bluetoothService == null) {
            return BluetoothService.STATE_NONE;
        }
        return bluetoothService.getState();
    }
    
    /**
     * 判断蓝牙适配器是否可用
     * @return true 表示可用，false 表示不可用
     */
    public boolean isBluetoothAvailable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
    
    /**
     * 获取蓝牙适配器
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }
    
    /**
     * 连接蓝牙设备
     * @param device 要连接的设备
     */
    public void connect(BluetoothDevice device) {
        if (bluetoothService != null) {
            // 检查当前连接状态，避免重复连接
            if (bluetoothService.getState() == BluetoothService.STATE_CONNECTING) {
                // 如果正在连接中，不进行重复连接
                return;
            }
            
            // 如果已经连接到相同设备，不需要重新连接
            if (bluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                String currentAddress = bluetoothService.getConnectedDeviceAddress();
                if (currentAddress != null && currentAddress.equals(device.getAddress())) {
                    // 已经连接到相同设备，不需要重新连接
                    return;
                }
            }
            
            // 设置设备信息，确保连接成功后能正确显示
            String deviceName = device.getName();
            if (deviceName == null || deviceName.isEmpty()) {
                deviceName = device.getAddress();
            }
            bluetoothService.setConnectedDeviceInfo(deviceName, device.getAddress());
            bluetoothService.connect(device);
        }
    }
    
    /**
     * 启动蓝牙服务
     */
    public void start() {
        if (bluetoothService != null) {
            bluetoothService.start();
        }
    }
    
    /**
     * 停止蓝牙服务（完全停止，用于应用退出时）
     */
    public void stop() {
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }
    
    /**
     * 断开蓝牙连接（用于返回主界面时）
     */
    public void disconnect() {
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }
    
    /**
     * 断开当前连接但保持服务可用（用于用户手动断开）
     */
    public void disconnectCurrentConnection() {
        if (bluetoothService != null) {
            try {
                bluetoothService.stop();
                // 重新启动服务以保持可用状态
                bluetoothService.start();
            } catch (Exception e) {
                // 如果断开连接失败，记录错误但不影响用户体验
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 写入数据到蓝牙
     * @param data 要写入的数据
     */
    public void write(byte[] data) {
        if (bluetoothService != null) {
            bluetoothService.write(data);
        }
    }
    
    /**
     * 重置蓝牙服务（用于切换 Handler 时）
     */
    public void resetBluetoothService(Context context, Handler handler) {
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
        bluetoothService = null;
        initBluetoothService(context, handler);
    }
    
    /**
     * 销毁单例（谨慎使用，一般在应用退出时调用）
     */
    public static void destroy() {
        if (instance != null && instance.bluetoothService != null) {
            instance.bluetoothService.stop();
            instance.bluetoothService = null;
        }
        instance = null;
    }
}

