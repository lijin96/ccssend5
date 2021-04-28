package com.holyes.ccssend5.lib;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.example.ccssend5.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @ClassName: NetManager
 * @Description: 网络管理类，获取和设置IP
 * @Author: lijin
 * @Date: 2021/3/6 14:28
 */
public class NetManager {

        private Context context;
        private ProgressDialog dialog=null;
        public NetManager(Context context) {
            this.context=context;

        }
        /**
         * 设置安卓设备wifi的IP
         * @param androidIp
         */
        public void setWifiIp(String androidIp){
            WifiConfiguration wifiConf = null;
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            List<WifiConfiguration> configuredNetworks = wifiManager
                    .getConfiguredNetworks();
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.networkId == connectionInfo.getNetworkId()) {
                    wifiConf = conf;
                    break;
                }
            }
            try {
                setIpAssignment("STATIC", wifiConf);
                setIpAddress(InetAddress.getByName(androidIp), 24, wifiConf);
                setGateway(InetAddress.getByName(ipGetGateway(androidIp)), wifiConf);
                setDNS(InetAddress.getByName(ipGetGateway(androidIp)), wifiConf);
            } catch (Exception e) {

                e.printStackTrace();
            }
            wifiManager.updateNetwork(wifiConf); // apply the setting
            wifiManager.setWifiEnabled(false);
            wifiManager.setWifiEnabled(true);
        }

        /**
         * 获取网络类型
         */
        public static String getNetType(Context mContext){
            ConnectivityManager connectMgr = (ConnectivityManager) mContext .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectMgr.getActiveNetworkInfo();
            if(info==null){
                new AlertDialog.Builder(mContext)
                        .setTitle("\t\t\t温馨提示")
                        .setIcon(R.drawable.attention)
                        .setMessage("抱歉，网络无法使用，请检查网络！")
                        .setPositiveButton("确\t定", null)
                        .show();
                return null;
            }
            if(info.getType()==ConnectivityManager.TYPE_WIFI){
                return "wifi";
            }else if(info.getType()==ConnectivityManager.TYPE_ETHERNET){
                return "ethernet";//以太网
            }else{
                return "other";
            }
        }



        /**
         * 取得本设备的ip地址
         * @return
         */
        public String getLocalIpAddress() {
            String ip="";
            try
            {
                for (Enumeration<NetworkInterface> en = NetworkInterface
                        .getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    //loopback地址就是代表本机的IP地址
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                            ip = inetAddress.getHostAddress().toString();
                        }else{
                            ip=null;
                        }
                    }
                }
                if(ip==null||ip.equals(""))
                {
                    // 获取wifi服务
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    // 判断wifi是否开启
                    if (wifiManager.isWifiEnabled())
                    {
                        // wifiManager.setWifiEnabled(true);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        int ipAddress = wifiInfo.getIpAddress();
                        ip=intToIp(ipAddress);
                    }
                }
            } catch (Exception ex) {
            }
            return ip;
        }

        /**
         * 转换wifi获取的ip形式
         * @param ip
         * @return
         */
        private String intToIp(int ip) {
            return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
                    + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
        }

        /**
         * 通过ip获得网关
         * @param ip
         * @return gateway
         */
        public String ipGetGateway(String ip){
            String gateway=ip.substring(0,ip.lastIndexOf(".")+1)+"1";
            return gateway;
        }

        /**
         * 把弹出的dialog给隐藏掉
         * @param str 吐司的信息
         */
        public void missDialog(String str){
            if(dialog!=null){
                dialog.dismiss();
            }
            Toast.makeText(context,str, Toast.LENGTH_SHORT).show();
        }

        /**
         * 检查ip的格式是否正确
         * @param text：ip字符串
         * @return
         */
        @SuppressLint("NewApi")
        public boolean ipCheck(String text) {
            if (text != null && !text.isEmpty()) {
                // 定义正则表达式
                String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                        + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                        + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                        + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
                // 判断ip地址是否与正则表达式匹配
                if (text.matches(regex)) {
                    // 返回判断信息
                    return true;
                } else {
                    // 返回判断信息
                    Toast.makeText(context, "IP地址格式有误！", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            Toast.makeText(context, "IP地址不能为空！", Toast.LENGTH_SHORT).show();
            // 返回判断信息
            return false;
        }

        public static void setIpAssignment(String assign, WifiConfiguration wifiConf)
                throws SecurityException, IllegalArgumentException,
                NoSuchFieldException, IllegalAccessException {
            setEnumField(wifiConf, assign, "ipAssignment");
        }

        public static void setIpAddress(InetAddress addr, int prefixLength,
                                        WifiConfiguration wifiConf) throws SecurityException,
                IllegalArgumentException, NoSuchFieldException,
                IllegalAccessException, NoSuchMethodException,
                ClassNotFoundException, InstantiationException,
                InvocationTargetException {
            Object linkProperties = getField(wifiConf, "linkProperties");
            if (linkProperties == null)
                return;
            Class laClass = Class.forName("android.net.LinkAddress");
            Constructor laConstructor = laClass.getConstructor(new Class[] {
                    InetAddress.class, int.class });
            Object linkAddress = laConstructor.newInstance(addr, prefixLength);

            ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties,
                    "mLinkAddresses");
            mLinkAddresses.clear();
            mLinkAddresses.add(linkAddress);
        }

        public static void setGateway(InetAddress gateway,
                                      WifiConfiguration wifiConf) throws SecurityException,
                IllegalArgumentException, NoSuchFieldException,
                IllegalAccessException, ClassNotFoundException,
                NoSuchMethodException, InstantiationException,
                InvocationTargetException {
            Object linkProperties = getField(wifiConf, "linkProperties");
            if (linkProperties == null)
                return;
            Class routeInfoClass = Class.forName("android.net.RouteInfo");
            Constructor routeInfoConstructor = routeInfoClass
                    .getConstructor(new Class[] { InetAddress.class });
            Object routeInfo = routeInfoConstructor.newInstance(gateway);

            ArrayList mRoutes = (ArrayList) getDeclaredField(linkProperties,
                    "mRoutes");
            mRoutes.clear();
            mRoutes.add(routeInfo);
        }

        public static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
                throws SecurityException, IllegalArgumentException,
                NoSuchFieldException, IllegalAccessException {
            Object linkProperties = getField(wifiConf, "linkProperties");
            if (linkProperties == null)
                return;

            ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(
                    linkProperties, "mDnses");
            mDnses.clear(); // or add a new dns address , here I just want to
            // replace DNS1
            mDnses.add(dns);
        }

        public static Object getField(Object obj, String name)
                throws SecurityException, NoSuchFieldException,
                IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getField(name);
            Object out = f.get(obj);
            return out;
        }

        public static Object getDeclaredField(Object obj, String name)
                throws SecurityException, NoSuchFieldException,
                IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            Object out = f.get(obj);
            return out;
        }

        public static void setEnumField(Object obj, String value, String name)
                throws SecurityException, NoSuchFieldException,
                IllegalArgumentException, IllegalAccessException {
            Field f = obj.getClass().getField(name);
            f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
        }

    }

