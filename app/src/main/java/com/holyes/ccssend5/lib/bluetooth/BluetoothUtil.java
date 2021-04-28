package com.holyes.ccssend5.lib.bluetooth;

import com.example.ccssend5.R;

/**
 * @ClassName: BluetoothUtil
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:25
 */
public class BluetoothUtil {
        public static final int BoxTagModel = R.raw.lab_jb;


        public static final int MESSAGE_STATE_CHANGE = 1;
        public static final int MESSAGE_READ = 2;
        public static final int MESSAGE_WRITE = 3;
        public static final int MESSAGE_DEVICE_NAME = 4;
        public static final int MESSAGE_TOAST = 5;

        // Key names received from the BluetoothService Handler
        public static final String DEVICE_NAME = "device_name";
        public static final String TOAST = "toast";

        // Intent request codes
        public static final int REQUEST_CONNECT_DEVICE = 1;

    }

