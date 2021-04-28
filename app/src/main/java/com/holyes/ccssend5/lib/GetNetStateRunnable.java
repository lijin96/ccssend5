package com.holyes.ccssend5.lib;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @ClassName: GetNetStateRunnable
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:27
 */
public class GetNetStateRunnable implements  Runnable{
    public Boolean State  = false;
    private Context mContext;
    GetNetState callback ;
    Handler mHandler ;
    Timer timer;

    public Boolean getState() {
        return State;
    }
    public void setState(Boolean state) {
        State = state;
    }
    public GetNetStateRunnable(Boolean tState,Context context) {
        this.State =  tState;
        this.mContext = context;
        timer = new Timer();
    }
    public void SetCallBack(final GetNetState tcallback)
    {
        this.callback = tcallback;
        // 用于子线程与主线程通信的Handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 0){
                    callback.netState_on();
                }else{
                    callback.netState_off();
                }
            }
        };
    }

    @Override
    public void run() {

        testConnect();

    }

    /**
     * 检查网络
     * @author hzm
     * @version 创建时间：2017-6-13 下午3:28:01
     */
    public void testConnect()
    {
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
//				Log.i("main", "getState----"+getState());
                if(getState()){
                    testConnect();
                }else{

                }
            }
        }, 2500);//2.5秒检查一下网络
        if(getState())
        {
            if( AccessWeb.getHelper(mContext).CheckServerState())
            {
                sendMsg(0);
            }
            else
            {
                sendMsg(1);
            }
        }
    }

    private void sendMsg(int what)
    {
        Message mgs = mHandler.obtainMessage();
        mgs.what = what;
        mHandler.sendMessage(mgs);
    }

    public interface GetNetState{
        void netState_on();
        void netState_off();
    }


}
