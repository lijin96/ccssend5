package com.holyes.ccssend5.lib.bluetooth;

import com.holyes.ccssend5.lib.SysUserInfo;

import java.io.UnsupportedEncodingException;

/**
 * @ClassName: BluetoothPrintUtil
 * @Description: 蓝牙打印工具类，用于统一处理打印模板替换和发送
 * @Author: AI Assistant
 * @Date: 2025/10/15
 */
public class BluetoothPrintUtil {
    
    /**
     * 处理打印模板替换并发送到蓝牙设备
     * @param bluetoothManager 蓝牙管理器
     * @param message 打印模板字符串
     * @param boxTag 盒标数据
     * @param sysUserInfo 用户信息
     * @return true 表示发送成功，false 表示发送失败
     */
    public static boolean printBoxTag(BluetoothManager bluetoothManager, String message, BoxTag boxTag, SysUserInfo sysUserInfo) {
        if (!bluetoothManager.isBluetoothConnected()) {
            return false;
        }
        
        // 根据企业ID处理不同的打印模板
        message = processPrintTemplate(message, boxTag, sysUserInfo);
        
        // 发送数据到蓝牙设备
        try {
            byte[] send = message.getBytes("GBK");
            bluetoothManager.write(send);
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 处理打印模板替换
     * @param message 原始模板字符串
     * @param boxTag 盒标数据
     * @param sysUserInfo 用户信息
     * @return 处理后的模板字符串
     */
    public static String processPrintTemplate(String message, BoxTag boxTag, SysUserInfo sysUserInfo) {
        if (message == null || boxTag == null) {
            return message;
        }
        
        String enterpriseId = sysUserInfo.getEnterpriseId().toString();
        
        // 根据企业ID处理不同的打印模板
        if (enterpriseId.equals("52") || enterpriseId.equals("53")) {
            // 52万新  53帕兰德
            message = message.replace("%BOX", boxTag.getBoxNo());
            message = message.replace("%U", boxTag.getUserCode());
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%S", boxTag.getSerialName());
            message = message.replace("%M", boxTag.getModel());
            message = message.replace("%C", boxTag.getColor());
            message = message.replace("%N", boxTag.getNum());
            // message = message.replace("%D", boxTag.getPackDate());
        } else if (enterpriseId.equals("12")) {
            // 12邦维 用汉印IT4S打印机打印
            message = message.replace("%BOX", boxTag.getBoxNo());
            message = message.replace("%U", boxTag.getUserCode());
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%S", boxTag.getSerialName());
            message = message.replace("%M", boxTag.getModel());
            message = message.replace("%C", boxTag.getColor());
            message = message.replace("%N", boxTag.getNum());
            // message = message.replace("%D", boxTag.getPackDate());
        } else if (enterpriseId.equals("76") || enterpriseId.equals("74") || enterpriseId.equals("00")) {
            // 逸夫和阿塔那都需要加仓库，品牌代号是76和74
            message = message.replace("%BOX", boxTag.getBoxNo());
            message = message.replace("%U", boxTag.getUserCode());
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%S", boxTag.getSerialName());
            message = message.replace("%M", boxTag.getModel());
            message = message.replace("%C", boxTag.getColor());
            message = message.replace("%N", boxTag.getNum());
            message = message.replace("%D", boxTag.getStockName());
        } else {
            // 默认处理（如51）
            message = message.replace("%BOX", boxTag.getBoxNo());
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%M", boxTag.getModel());
            message = message.replace("%C", boxTag.getColor());
            message = message.replace("%N", boxTag.getNum());
            message = message.replace("%D", boxTag.getPackDate());
        }
        
        return message;
    }
    
    /**
     * 处理默认打印模板（当没有企业特定模板时使用）
     * @param message 原始模板字符串
     * @param boxTag 盒标数据
     * @return 处理后的模板字符串
     */
    public static String processDefaultTemplate(String message, BoxTag boxTag) {
        if (message == null || boxTag == null) {
            return message;
        }
        
        message = message.replace("%BOX", boxTag.getBoxNo());
        message = message.replace("%U", boxTag.getUserCode());
        message = message.replace("%B", boxTag.getBrandName());
        message = message.replace("%S", boxTag.getSerialName());
        message = message.replace("%M", boxTag.getModel());
        message = message.replace("%C", boxTag.getColor());
        message = message.replace("%N", boxTag.getNum());
        message = message.replace("%D", boxTag.getPackDate());
        
        return message;
    }
    
    /**
     * 检查蓝牙连接状态并显示提示信息
     * @param bluetoothManager 蓝牙管理器
     * @param context 上下文
     * @return true 表示已连接，false 表示未连接
     */
    public static boolean checkBluetoothConnection(BluetoothManager bluetoothManager, android.content.Context context) {
        if (!bluetoothManager.isBluetoothConnected()) {
            com.holyes.ccssend5.lib.ShowMessage.Show(context, "未连接蓝牙");
            return false;
        }
        return true;
    }
}
