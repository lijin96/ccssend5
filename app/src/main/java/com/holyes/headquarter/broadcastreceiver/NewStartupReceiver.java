package com.holyes.headquarter.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.holyes.agent.activity.P_Dv_Select_Brand;
import com.holyes.ccssend5.activity.LoginActivity;
import com.holyes.ccssend5.lib.ADevicesManager;

/**
 * @ClassName: NewStartupReceiver
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:26
 */
public class NewStartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        ADevicesManager.SetKey(false);
        Intent firstIntent = null ;
        if(!ADevicesManager.CorpCompany)
        {
            firstIntent = new Intent(context, P_Dv_Select_Brand.class);
        }
        else
        {
            firstIntent = new Intent(context, LoginActivity.class);
        }
        firstIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //将intent以startActivity传送给操作系统
        context.startActivity(firstIntent);
    }
}
