package com.holyes.ccssend5.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.view.Display;

import java.lang.reflect.Method;

/**
 * @ClassName: DisplayUtil
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:01
 */
public class DisplayUtil {

        private static final String TAG = DisplayUtil.class.getSimpleName();

        /**
         * 系统设置"显示大小"时原有UI样式保持不变：
         *
         * 1、当调节手机系统"显示大小"【调大】的时候，相应的dpi会变大【dp = (dpi/160) * px】,此时dp就会变大，所以相应的UI布局就会变大。
         * 2、当调节手机系统"分辨率"【调小】的时候，相应的dpi会变小【比如由480-->320】。如果此时使用技术手段使dpi保持不变，那么相同的dp就会占用更多的px，所以UI布局就会变大。
         *
         * @param context
         */
        @SuppressLint("NewApi")
        public static void setDefaultDisplay(Context context) {
            if (Build.VERSION.SDK_INT > 23) {
                Configuration origConfig = context.getResources().getConfiguration();
                //获取手机出厂时默认的densityDpi【注释1】
                origConfig.densityDpi = getDefaultDisplayDensity();

//                Log.d(TAG, "densityDpi: " + origConfig.densityDpi);
                context.getResources().updateConfiguration(origConfig, context.getResources().getDisplayMetrics());
            }
        }

        public static int getDefaultDisplayDensity() {
            try {
                Class clazz = Class.forName("android.view.WindowManagerGlobal");
                Method method = clazz.getMethod("getWindowManagerService");
                method.setAccessible(true);
                Object iwm = method.invoke(clazz);
                Method getInitialDisplayDensity = iwm.getClass().getMethod("getInitialDisplayDensity", int.class);
                getInitialDisplayDensity.setAccessible(true);
                Object densityDpi = getInitialDisplayDensity.invoke(iwm, Display.DEFAULT_DISPLAY);
                return (Integer)densityDpi;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

    }

