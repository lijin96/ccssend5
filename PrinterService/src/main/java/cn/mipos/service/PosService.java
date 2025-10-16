package cn.mipos.service;

import android.content.Context;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

import static cn.mipos.service.ServiceError.ERR_OKAY;

/**
 * MiPos 对外提供的服务
 * 1. 打印机;
 * 2. iBeacon 功能;
 * 3. 来电显示功能;
 * 4. 开钱箱;
 * 5. 无线通讯;
 *
 * @author jarry
 * @version 1.0
 * @since 2016/9/1
 *
 * 1. 删除iBeacon功能;
 * 2. 删除来电显示功能;
 * 3. 移除电源识别控制(已经在驱动重新实现);
 * @version 1.1
 * @since 2017/5/10
 */
public class PosService extends Thread  {

    private static final String TAG = "PosService";

    private static final String PATH_CASH_MIPOS_RK312X = "/sys/class/led_case/gpio/case";

    private static final String PATH_CASH_MIPOS_A63 = "/sys/bus/platform/devices/att_test/test";

    private static final String PATH_CASH_M10_PX30 = "/sys/devices/platform/cashbox/cashbox_control";

    private static final String PATH_LED_MIPOS_RK312X = "/sys/class/led_case/gpio/led";

    private static final String PATH_LED_MIPOS_A63 = "/sys/bus/platform/devices/dodoleds/test";

    private static final String PATH_LED_M10_PX30 = "/sys/class/leds/led_red/brightness";

    private static final int STATE_CASH_OPEN = 0;

    private static final int STATE_CASH_CLOSE = 1;

    private static final int STATE_LED_OPEN = 1;

    private static final int STATE_LED_CLOSE = 0;

    private static final int STATE_LED_OPEN_M10_PX30 = 0;

    private static final int STATE_LED_CLOSE_M10_PX30 = 1;

    private static final int STATE_CASH_OPEN_M10_PX30 = 1;

    private static final int STATE_CASH_CLOSE_M10_PX30 = 0;

    public static final int LED_STATUS_1    = 1;        //无纸
    public static final int LED_STATUS_2    = 2;
    public static final int LED_STATUS_3    = 3;
    public static final int LED_STATUS_4    = 4;
    public static final int LED_STATUS_5    = 5;        //其他错误

    public static int ledStatus = 0;
    private static final int cashboxOpenTime = 100;
    public static PosService instance = null;

    private Thread ledThread = null;

    /**
     * 加载Jni 动态库
     */
    static {
        System.loadLibrary("mipos_service_v2");
    }

    public PosService(Context context) {
        Log.i("PosService", "init");

        _init();
        instance = this;

        startStatusLed();

        setCashBoxPower(true);
        setCashBoxOpen(false);
        setLed(false);
    }

    protected void finalize() throws Throwable {
        Log.i("PosService", "deinit");
        setCashBoxPower(false);
        setCashBoxOpen(false);
        ledStatus = 0;
        ledThread.interrupt();
        setLed(false);
        _deinit();
        instance = null;
        super.finalize();
    }

    /**
     * 启动状态显示LED线程
     */
    public void startStatusLed()
    {
        ledThread = new Thread()
        {
            @Override
            public void run()
            {
                while(!isInterrupted())
                {
                    try {
                        sleep(1000);
                        if(ledStatus > 0)
                        {
                            setLed(true);
                            sleep(1000);
                            setLed(false);
                            sleep(750);
                            for (int i = 0; i < ledStatus; i++) {
                                sleep(250);
                                setLed(true);
                                sleep(250);
                                setLed(false);
                            }
                        }
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            }
        };

        ledThread.start();
    }


    /**
     * 打印点阵数据, 每行数据 384 个点 48 字节长,
     * 每个点物理理宽度高度及高度为 0.125mm, 每8个点1毫米
     * 最长 4000 行, 4000 * 0.125 = 50 厘米高度
     * 最大数据为 length = 48 * 4000 = 192000 字节
     *
     * @param buffer
     * @param length
     * @return 错误代码
     */
    public int printerPrint(byte[] buffer, int length)
    {
        Log.d("PosService","printerPrint");

        int ret = ServiceError.ERR_PRINTER_RS_DATA_ERR;
        if(length > 192000){
            return ret;
        }

        ret = _printer_print(buffer,length);
        setLedStatus(ret);
        Log.d("PosService","printerPrint finish:" + ret);

        return ret;
    }

    public int printerWrite(byte[] buffer, int length)
    {
        Log.d("PosService","printerWrite");

        int ret = ServiceError.ERR_PRINTER_RS_DATA_ERR;
        if(length > 192000){
            return ret;
        }

        ret = _printer_write(buffer,length);
        setLedStatus(ret);
        Log.d("PosService","printerWrite finish:" + ret);

        return ret;
    }

    /**
     * 查询打印机上一个工作状态
     *
     * @return printStatus 打印机任务状态
     */
    public PrinterStatus printerStatus()
    {
        Log.i("PosService","printerStatus");
        return _printer_status();
    }

    /**
     * 走空白纸.
     * @param length 毫米
     * @return 错误代码
     */
    public int printerFeedPaper(int length)
    {
        int ret = -1;
        if(instance == null) return ret;
        ret = _printer_feed_paper(length);

        setLedStatus(ret);
        return ret;
    }

    /**
     * 打印机电源模式(电源适配器或电池), 由Android应用监听系统消息, 如果电源改变时, 将修改打印机的电源使用情况.
     * @param mode 电源模式
     * @return 错误代码
     */
    public static int printerPowerMode(int mode) {
        if(instance == null) return -1;
        return _printer_power_mode(mode);
    }

    /**
     * 设置打印机加热参数, 内部使用的方法.
     *
     * @param params 100个点以内,每多少个点用多久的加热时间
     * @param type
     * @return 错误代码
     */
    public int printerHeatParams(int[] params, int type)
    {
        if(params.length != 100) return 0;

        byte[] buffer = integersToBytes(params);

        if(buffer == null || buffer.length != 400) return 0;

        return _printer_heat_params(buffer, type, buffer.length);
    }

    /**
     * 设置打印机电机运行参数, 内部使用的方法.
     *
     * @param batteryDly    使用电池时, 步进电机延时, 0 为无效
     * @param expowerDly    使用电源时, 步进电机延时, 0 为无效
     * @param batteryPoints 使用电池时, 单次打印点数, 0 为无效
     * @param expowerPoints 使用电源时, 单次打印点数, 0 为无效
     * @return 错误代码
     */
    public int printerSpeedParams(int batteryDly, int expowerDly, int batteryPoints,int expowerPoints)
    {
        return _printer_speed_params(batteryDly,expowerDly,batteryPoints,expowerPoints);
    }

    /**
     * iBeacon 设置信息
     *
     * @param uuid
     * @param major
     * @param minor
     * @param mPower
     * @param interval
     * @return 错误代码
     */
    public int ibeaconSetInfo(byte[] uuid, int major, int minor, int mPower, int interval)
    {
        return _ibeacon_set_info(uuid,major,minor,mPower,interval);
    }

    /**
     * iBeacon 配置信息
     *
     * @param uuid      唯一识别码
     * @param major     主要ID
     * @param minor     次要ID
     * @param mPower    测量功率, 在一米处测量到的实际功率
     * @param interval  发射间隔
     * @return 错误代码
     */
    public int ibeaconGetInfo(byte[] uuid, int major, int minor, int mPower, int interval)
    {
        return _ibeacon_get_info(uuid,major,minor,mPower,interval);
    }

    /**
     * 轮询是否有来电,由Java的线程轮询.
     * @return 电话号码
     */
    public String queryCallId()
    {
        return _query_caller_id();
    }

    /**
     * 打开钱箱
     */
    public void openCashBox()
    {
        setCashBoxOpen(true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setCashBoxOpen(false);
            }
        }, cashboxOpenTime);
    }

    /**
     * iBeacon 开关
     *
     * @param command
     * @return 错误代码
     */
    public int ibeaconControl(int command)
    {
        return _ibeacon_control(command);
    }

    /**
     * 控制钱箱开关电源
     * @param on
     */
    private void setCashBoxPower(boolean on)
    {
    }

    /**
     * 控制钱箱开关
     * @param on
     */
    public void setCashBoxOpen(boolean on)
    {
        Log.d(TAG,"setCashBoxOpen = " + on);
        writeFile(PATH_CASH_MIPOS_RK312X, on? STATE_CASH_OPEN : STATE_CASH_CLOSE);
        echoFile(PATH_CASH_MIPOS_A63, on? STATE_CASH_OPEN : STATE_CASH_CLOSE);
        writeFile(PATH_CASH_M10_PX30, on? STATE_CASH_OPEN_M10_PX30 : STATE_CASH_CLOSE_M10_PX30);
    }

    /**
     * 设置LED灯
     * @param on
     */
    public void setLed(boolean on)
    {
        writeFile(PATH_LED_MIPOS_RK312X, on? STATE_LED_OPEN : STATE_LED_CLOSE);
        echoFile(PATH_LED_MIPOS_A63, on? STATE_LED_OPEN : STATE_LED_CLOSE);
        writeFile(PATH_LED_M10_PX30, on? STATE_LED_OPEN_M10_PX30 : STATE_LED_CLOSE_M10_PX30);
    }

    private void echoFile(String path, int state){
        String cmd = String.format("echo %s > %s", state, path);
        Log.d("echoFile", "echoFile cmd:" + cmd);
        ShellUtils.execCommand(cmd, false);
    }

    private void writeFile(String path, int state){
        Log.d("writeFile", "writeFile path:" + path + "  state:" + state);
        FileWriter wr=null;
        try {
            wr = new FileWriter(path);
            wr.write(String.valueOf(state));
            wr.flush();
        } catch (Exception e) {
            //e.printStackTrace();
        }finally {
            if(wr != null){
                try {
                    wr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 设置LED状态
     * @param errorCode
     */
    public void setLedStatus(int errorCode)
    {
        if(errorCode < ERR_OKAY)
        {
            switch (errorCode)
            {
                case ServiceError.ERR_PRINTER_RS_OUT_PAPER:
                    ledStatus = LED_STATUS_1;
                    break;
                default:
                    ledStatus = LED_STATUS_5;
            }
        } else
        {
            ledStatus = 0;
        }
    }

    /**
     * 将int 转为字节数组
     * @param values
     * @return 字节数组
     */
    byte[] integersToBytes(int[] values)
    {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for(int i=0;i<100;i++) buffer.putInt(values[i]);
        buffer.flip();
        return buffer.array();
    }

    /* JNI 库 */
    private static native int _init();
    private static native int _deinit();
    private static native int _printer_print(byte[] buffer, int length);
    private static native int _printer_write(byte[] buffer, int length);
    private static native PrinterStatus _printer_status();
    private static native int _printer_feed_paper(int length);
    private static native int _printer_power_mode(int mode);
    private static native int _printer_heat_params(byte[] buffer, int type, int length);
    private static native int _printer_speed_params(int batteryDly, int expowerDly, int batteryPoints,int expowerPoints);
    private static native int _ibeacon_set_info(byte[] uuid, int major, int minor, int mPower, int interval);
    private static native int _ibeacon_get_info(byte[] uuid, int major, int minor, int mPower, int interval);
    private static native int _ibeacon_control(int command);
    private static native String _query_caller_id();

}
