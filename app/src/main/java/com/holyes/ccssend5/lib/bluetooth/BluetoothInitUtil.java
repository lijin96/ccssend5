package com.holyes.ccssend5.lib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SysUserInfo;

/**
 * @ClassName: BluetoothInitUtil
 * @Description: 蓝牙初始化工具类，用于统一处理蓝牙初始化和自动连接
 * @Author: AI Assistant
 * @Date: 2025/10/15
 */
public class BluetoothInitUtil {
    
    /**
     * 初始化蓝牙管理器并设置自动连接
     * @param context 上下文
     * @param handler 消息处理器
     * @param sysUserInfo 用户信息
     * @param btn_connect 连接按钮
     * @param tv_connect_state 连接状态文本
     * @return 返回蓝牙管理器和适配器的数组 [BluetoothManager, BluetoothAdapter]
     */
    public static Object[] initBluetoothWithAutoConnect(Context context, Handler handler, SysUserInfo sysUserInfo, 
                                                       Button btn_connect, TextView tv_connect_state) {
        // 使用全局蓝牙管理器
        BluetoothManager bluetoothManager = BluetoothManager.getInstance();
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getBluetoothAdapter();
        
        if (bluetoothManager.isBluetoothAvailable()) {
            // 如果已经连接，则不需要重新初始化
            if (!bluetoothManager.isBluetoothConnected()) {
                // 自动连接上次保存的蓝牙设备
                boolean isAutoConnecting = bluetoothManager.initBluetoothServiceAndAutoConnect(context, handler);
                if (isAutoConnecting) {
                    // 正在自动连接，更新UI状态
                    String connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
                    BluetoothStateHandler.updateConnectingUI(btn_connect, tv_connect_state, connectedDeviceName);
                } else {
                    // 没有保存的设备地址，显示未连接状态
                    BluetoothStateHandler.updateDisconnectedUI(btn_connect, tv_connect_state);
                }
            } else {
                // 已经连接，直接更新UI状态
                String connectedDeviceName = sysUserInfo.getConnectedBluetoothName();
                String connectedDeviceAddress = sysUserInfo.getConnectedBluetoothAddress();
                BluetoothStateHandler.updateConnectedUI(btn_connect, tv_connect_state, connectedDeviceName, connectedDeviceAddress, sysUserInfo);
            }
        } else {
            ShowMessage.Show(context, "蓝牙未打开或不可用，请到系统设置中检查");
            BluetoothStateHandler.updateDisconnectedUI(btn_connect, tv_connect_state);
        }
        
        return new Object[]{bluetoothManager, mBluetoothAdapter};
    }
    
    /**
     * 初始化蓝牙管理器（不设置自动连接）
     * @param context 上下文
     * @param handler 消息处理器
     * @return 返回蓝牙管理器和适配器的数组 [BluetoothManager, BluetoothAdapter]
     */
    public static Object[] initBluetooth(Context context, Handler handler) {
        BluetoothManager bluetoothManager = BluetoothManager.getInstance();
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getBluetoothAdapter();
        
        if (bluetoothManager.isBluetoothAvailable()) {
            bluetoothManager.initBluetoothService(context, handler);
        }
        
        return new Object[]{bluetoothManager, mBluetoothAdapter};
    }
    
    /**
     * 检查蓝牙是否可用并显示提示
     * @param context 上下文
     * @param bluetoothManager 蓝牙管理器
     * @return true 表示蓝牙可用，false 表示蓝牙不可用
     */
    public static boolean checkBluetoothAvailability(Context context, BluetoothManager bluetoothManager) {
        if (!bluetoothManager.isBluetoothAvailable()) {
            ShowMessage.Show(context, "蓝牙未打开或不可用，请到系统设置中检查");
            return false;
        }
        return true;
    }
    
    /**
     * 处理蓝牙连接状态变化
     * @param bluetoothManager 蓝牙管理器
     * @param context 上下文
     * @param handler 消息处理器
     * @param btn_connect 连接按钮
     * @param tv_connect_state 连接状态文本
     * @param sysUserInfo 用户信息
     * @param connectedDeviceName 连接的设备名称
     * @param connectedDeviceAddress 连接的设备地址
     * @return 返回连接状态（true表示已连接，false表示未连接）
     */
    public static boolean handleBluetoothStateChange(BluetoothManager bluetoothManager, Context context, Handler handler,
                                                   Button btn_connect, TextView tv_connect_state, SysUserInfo sysUserInfo,
                                                   String connectedDeviceName, String connectedDeviceAddress) {
        if (bluetoothManager.isBluetoothConnected()) {
            BluetoothStateHandler.updateConnectedUI(btn_connect, tv_connect_state, connectedDeviceName, connectedDeviceAddress, sysUserInfo);
            return true;
        } else {
            BluetoothStateHandler.updateDisconnectedUI(btn_connect, tv_connect_state);
            return false;
        }
    }
}
