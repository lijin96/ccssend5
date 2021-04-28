package com.holyes.ccssend5.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.holyes.ccssend5.lib.UpdateManager2;

/**
 * @ClassName: PackageReceiver
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 9:55
 */
public class PackageReceiver extends BroadcastReceiver {
        /***应用是否已经启动，在login里面改变它的值 */

        @SuppressLint("NewApi")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            String packageName = intent.getDataString().substring(8);//package:com.holyes.ccsyft
            //如果不是安装了第四代的都不用管
            if(!"com.holyes.ccs_dev8".equals(packageName))
            {
                return;
            }
            //安装
            if (("android.intent.action.PACKAGE_ADDED").equals(action))
            {
//        	UpdateManager2.showUninstallDialog(context);
                UpdateManager2.uninstallApp(context, "com.holyes.ccsdevs5");
//        	Log.i("main", "安装完成了"+packageName);
            }
            // 覆盖安装,安装完成后跳转到登录界面
            else if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
//        	Log.i("main", "覆盖安装");
            }
            //移除
            else if (("android.intent.action.PACKAGE_REMOVED").equals(action))
            {
//        	Log.i("main", "移除      ");
            }
            //下载完成
            else if("android.intent.action.DOWNLOAD_COMPLETE".equals(action))
            {
//        	Log.i("main", "下载完成");
            }

        }
    }

