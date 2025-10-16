package com.holyes.ccssend5.lib.bluetooth;

import android.graphics.Color;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;

import com.holyes.ccssend5.lib.SysUserInfo;

/**
 * @ClassName: BluetoothStateHandler
 * @Description: 蓝牙状态处理工具类，用于统一处理蓝牙连接状态的UI更新
 * @Author: AI Assistant
 * @Date: 2025/10/15
 */
public class BluetoothStateHandler {
    
    /**
     * 处理蓝牙状态变化消息
     * @param msg 消息对象
     * @param btn_connect 连接按钮
     * @param tv_connect_state 连接状态文本
     * @param connectedDeviceName 连接的设备名称
     * @param connectedDeviceAddress 连接的设备地址
     * @param sysUserInfo 用户信息
     * @return 返回连接状态（true表示已连接，false表示未连接）
     */
    public static boolean handleBluetoothStateChange(Message msg, Button btn_connect, TextView tv_connect_state, 
                                                   String connectedDeviceName, String connectedDeviceAddress, 
                                                   SysUserInfo sysUserInfo) {
        if (msg.what != BluetoothUtil.MESSAGE_STATE_CHANGE) {
            return false;
        }
        
        switch (msg.arg1) {
            case BluetoothService.STATE_CONNECTED:
                // 连接成功
                btn_connect.setText("断开");
                tv_connect_state.setText("已连接:" + connectedDeviceName);
                tv_connect_state.setTextColor(Color.parseColor("#008000"));
                // 保存连接信息
                if (sysUserInfo != null) {
                    sysUserInfo.setConnectedBluetoothName(connectedDeviceName);
                    sysUserInfo.setConnectedBluetoothAddress(connectedDeviceAddress);
                }
                return true;
                
            case BluetoothService.STATE_CONNECTING:
                // 正在连接
                btn_connect.setText("连接");
                tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                tv_connect_state.setTextColor(Color.BLACK);
                return false;
                
            case BluetoothService.STATE_LISTEN:
            case BluetoothService.STATE_NONE:
                // 未连接
                btn_connect.setText("连接");
                tv_connect_state.setText("未连接");
                tv_connect_state.setTextColor(Color.RED);
                return false;
                
            default:
                return false;
        }
    }
    
    /**
     * 更新连接成功的UI状态
     * @param btn_connect 连接按钮
     * @param tv_connect_state 连接状态文本
     * @param connectedDeviceName 连接的设备名称
     * @param connectedDeviceAddress 连接的设备地址
     * @param sysUserInfo 用户信息
     */
    public static void updateConnectedUI(Button btn_connect, TextView tv_connect_state, 
                                       String connectedDeviceName, String connectedDeviceAddress, 
                                       SysUserInfo sysUserInfo) {
        btn_connect.setText("断开");
        tv_connect_state.setText("已连接:" + connectedDeviceName);
        tv_connect_state.setTextColor(Color.parseColor("#008000"));
        if (sysUserInfo != null) {
            sysUserInfo.setConnectedBluetoothName(connectedDeviceName);
            sysUserInfo.setConnectedBluetoothAddress(connectedDeviceAddress);
        }
    }
    
    /**
     * 更新连接中的UI状态
     * @param btn_connect 连接按钮
     * @param tv_connect_state 连接状态文本
     * @param connectedDeviceName 连接的设备名称
     */
    public static void updateConnectingUI(Button btn_connect, TextView tv_connect_state, String connectedDeviceName) {
        btn_connect.setText("连接");
        tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
        tv_connect_state.setTextColor(Color.BLACK);
    }
    
    /**
     * 更新未连接的UI状态
     * @param btn_connect 连接按钮
     * @param tv_connect_state 连接状态文本
     */
    public static void updateDisconnectedUI(Button btn_connect, TextView tv_connect_state) {
        btn_connect.setText("连接");
        tv_connect_state.setText("未连接");
        tv_connect_state.setTextColor(Color.RED);
    }
    
    /**
     * 根据蓝牙连接状态更新UI
     * @param isConnected 是否已连接
     * @param btn_connect 连接按钮
     * @param tv_connect_state 连接状态文本
     * @param connectedDeviceName 连接的设备名称
     * @param connectedDeviceAddress 连接的设备地址
     * @param sysUserInfo 用户信息
     */
    public static void updateUIByConnectionState(boolean isConnected, Button btn_connect, TextView tv_connect_state, 
                                               String connectedDeviceName, String connectedDeviceAddress, 
                                               SysUserInfo sysUserInfo) {
        if (isConnected) {
            updateConnectedUI(btn_connect, tv_connect_state, connectedDeviceName, connectedDeviceAddress, sysUserInfo);
        } else {
            updateDisconnectedUI(btn_connect, tv_connect_state);
        }
    }
}
