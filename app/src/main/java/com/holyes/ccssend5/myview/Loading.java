package com.holyes.ccssend5.myview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ccssend5.R;

/**
 * @ClassName: Loading
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 9:54
 */
public class Loading {

        private Dialog dialog=null;
        private TextView txtTip;
        private ProgressBar probar = null;
        private Handler hand;


        /**当对话框取消的时候会触发这个事件*/
        public interface OnLoadingback{
            public void back(String name);
        }
        public OnLoadingback loadingback;


        private class handShowMsg extends Handler {
            @Override
            public void handleMessage(Message msg) {

                switch(msg.what)
                {
                    case 0:
                        loadingback.back("true");
                        break;
                }
                super.dispatchMessage(msg);
            }
        }

        public Loading(OnLoadingback loadingback)
        {
            this.loadingback = loadingback;
        }

        public void Show()
        {
            if(dialog!=null)
                dialog.show();
        }

        public void Close(){
            if(dialog!=null)
                dialog.dismiss();
        }
        public  Loading(Context context)
        {
            Create(context,"正在加载...",null);
        }

        public  Loading(Context context,String msg,OnLoadingback loadingback){
            this.loadingback = loadingback;

            hand = new handShowMsg();
            Create(context,msg,null);
        }

        public  Loading(Context context,String msg){
            Create(context,msg,null);
        }

        public  Loading(Context context, String msg, View.OnClickListener click){
            Create(context,msg,click);
        }

        public void Create(Context context, String msg, View.OnClickListener click){

            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loading);
            dialog.setCancelable(true);

            dialog.setCanceledOnTouchOutside(false);
            //>>>>>>>
            if(dialog!=null)
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {

                        if(dialog!=null)
                            loadingback.back("true");
                    }
                });

            if(click==null){
                ((Button)dialog.findViewById(R.id.btnEsc)).setVisibility(View.GONE);
                //((Button)dialog.findViewById(R.id.btnEsc)).setOnClickListener(new btnEsc_Click());
            }else
            {
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {

                        loadingback.back("true");
                    }
                });

                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });


                ((Button)dialog.findViewById(R.id.btnEsc)).setOnClickListener(click);
            }
            probar =(ProgressBar)dialog.findViewById(R.id.progressBar2);

            txtTip = (TextView)dialog.findViewById(R.id.txtTip);

            txtTip.setText(msg);

            ImageView spaceshipImage = (ImageView)dialog.findViewById(R.id.imgLoading);

            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,R.anim.loading_animation);
            // 使用ImageView显示动画
            spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        }

        public void SetProgressIsShow(boolean IsShow){
            if(IsShow){
                probar.setVisibility(View.VISIBLE);
            }else{
                probar.setVisibility(View.GONE);
            }
        }

        public void setTipText(String msg){
            txtTip.setText(msg);
        }

        public void SetProgressValue(int progress)
        {
            probar.setProgress(progress);
            probar.setSecondaryProgress(progress);

        }

        public boolean onKeyShortcut(int keyCode, KeyEvent event) {
            loadingback.back("true");
            return true;
        }

        public void initProgress(int max)
        {
            if(max==0)
            {
                probar.setVisibility(View.GONE);
                probar.setProgress(0);
                return;
            }
            probar.setIndeterminate(false);
            probar.setVisibility(View.VISIBLE);
            probar.setMax(max);
            probar.setProgress(0);
        }


        public boolean onKey(int keyCode, KeyEvent event) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    loadingback.back("true");

                    //QuitApp();
                    return true;
                case KeyEvent.KEYCODE_MINUS:

            }
            return false;
        }
    }

