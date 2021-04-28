package com.holyes.ccssend5.myview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.ccssend5.R;

/**
 * @ClassName: MessageBox
 * @Description: 消息弹出窗口
 * @Author: lijin
 * @Date: 2021/3/10 9:54
 */
public class MessageBox {

        public enum MessageBoxButtons
        {
            //重试，忽略
            AbortRetryIgnore,
            //ok
            OK,
            //确定，取消
            OKCancel,
            //重试,取消
            RetryCancel,
            //确定，否定
            YesNo,
            //确定，否定，取消
            YesNoCancel,

        }

        public enum ResultDiloag
        {
            first,
            sencond,
            third
        }

        AlertDialog.Builder builder;
        public static ResultDiloag result = null;



        public MessageBox(Context context)
        {

        }


        /**
         * 消息弹出窗口
         * @param context 上下文菜单
         * @param message 消息
         */
        public  MessageBox(Context context,String message )
        {

            builder = new AlertDialog.Builder(context);

            builder.setIcon(R.drawable.ccs);
            builder.setTitle("温馨提示");
            builder.setMessage(message);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    result =  ResultDiloag.first;
                }
            });

        }


        /**
         * 消息弹出窗口
         * @param context 上下文菜单
         *  @param icoid ico资源，默认为ccs
         * @param message 消息
         */

        public  MessageBox(Context context,int icoid,String message )
        {

            builder = new AlertDialog.Builder(context);

            builder.setIcon(icoid);
            builder.setTitle("温馨提示");
            builder.setMessage(message);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    result =  ResultDiloag.first;
                }
            });
        }

        /**
         * 消息弹出窗口
         * @param context 上下文菜单
         * @param icoid ico资源
         * @param title 标题
         * @param message 消息
         */

        public  MessageBox(Context context,int icoid,String title,String message )
        {
            builder = new AlertDialog.Builder(context);

            builder.setIcon(icoid);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    result =  ResultDiloag.first;
                }
            });

        }


        /**
         * 消息弹出窗口
         * @param context 上下文菜单
         * @param icoid ico资源
         * @param title 标题
         * @param message 消息
         * @param dd 按钮类型
         */

        public  MessageBox(Context context,int icoid,String title,String message,MessageBoxButtons dd )
        {


            builder = new AlertDialog.Builder(context);

            builder.setIcon(icoid);
            builder.setTitle(title);
            builder.setMessage(message);
            switch(dd)
            {
                case AbortRetryIgnore:
                    builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.first;
                        }
                    });
                    builder.setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.sencond;
                        }
                    });
                    break;

                case RetryCancel:
                    builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.first;
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.sencond;
                        }
                    });
                    break;
                case OKCancel:
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.first;
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.sencond;
                        }
                    });
                    break;
                case YesNo:
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.first;
                        }
                    });
                    builder.setNegativeButton("否定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.sencond;
                        }
                    });
                    break;

                case YesNoCancel:
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.first;
                        }
                    });
                    builder.setNegativeButton("否定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.sencond;
                        }
                    });
                    builder.setNeutralButton("取消",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.third;
                        }
                    });
                    break;
                default :  //默认OK
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            result =  ResultDiloag.first;
                        }
                    });
                    break;

            }
        }

        /**
         * 消息显示
         * @author zhang
         * @return ResultDiloag first,	sencond,	third
         */
        public ResultDiloag Show()
        {

            builder.create().show();
            return result;
        }
    }



