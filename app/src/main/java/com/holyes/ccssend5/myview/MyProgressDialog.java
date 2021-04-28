package com.holyes.ccssend5.myview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ccssend5.R;

/**
 * @ClassName: MyProgressDialog
 * @Description: 自定义等待框
 * @Author: lijin
 * @Date: 2021/3/10 9:54
 */
public class MyProgressDialog {

        static Dialog loadingDialog;

//	 /**当对话框取消的时候会触发这个事件*/
//    public interface OnMyProgressDialogClose{
//        public void OnClose(String name);
//    }
//
//    public static OnMyProgressDialogClose OnMyprogressDialog_Close;

        /**
         * 显示一个等待框
         *
         * @param context 上下文环境
         * @param isCancel 是否能用返回取消
         * @param isRight true文字在右边false在下面
         */
        public static void show(Context context, boolean isCancel, boolean isRight) {

            if(loadingDialog != null)
            {
                return;
            }
            else
            {
                creatDialog(context, "", isCancel, isRight);
            }
        }




        /**
         * 显示一个等待框
         *
         * @param context 上下文环境
         * @param msg 等待框的文字
         * @param isCancel 是否能用返回取消
         * @param isRight true文字在右边false在下面
         */
        public static void show(Context context, String msg, boolean isCancel, boolean isRight) {
            if(loadingDialog != null)
            {
                return;
            }
            else
            {
                creatDialog(context, msg, isCancel, isRight);
            }

        }

        private static void creatDialog(Context context, String msg, boolean isCancel, boolean isRight) {

            LinearLayout.LayoutParams wrap_content = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams wrap_content0 = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout main = new LinearLayout(context);
            main.setBackgroundColor(Color.WHITE);
            if (isRight) {
                main.setOrientation(LinearLayout.HORIZONTAL);
                wrap_content.setMargins(10, 0, 35, 0);
                wrap_content0.setMargins(35, 25, 0, 25);
            } else {
                main.setOrientation(LinearLayout.VERTICAL);
                wrap_content.setMargins(10, 5, 10, 15);
                wrap_content0.setMargins(35, 25, 35, 0);
            }
            main.setGravity(Gravity.CENTER);
            ImageView spaceshipImage = new ImageView(context);
            spaceshipImage.setImageResource(R.drawable.loading);
            TextView tipTextView = new TextView(context);
            tipTextView.setText("请稍候...");
            // 加载旋转动画
            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,
                    R.anim.loading_animation);
            // 使用ImageView显示动画
            spaceshipImage.startAnimation(hyperspaceJumpAnimation);
            if (msg != null && !"".equals(msg))
                tipTextView.setText(msg);// 设置加载信息,否则加载默认值


            loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog
            loadingDialog.setCancelable(isCancel);// 是否可以用返回键取消
            main.addView(spaceshipImage, wrap_content0);
            main.addView(tipTextView, wrap_content);
            LinearLayout.LayoutParams fill_parent = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
            loadingDialog.setContentView(main, fill_parent);// 设置布局
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {

//			loadingDialog.dismiss();
                    loadingDialog = null;

                }
            });

            loadingDialog.show();

        }
        /**
         * 关闭对话框
         *
         */
        public  static void close()
        {
            if(loadingDialog == null)
            {
                return;
            }
            else if(loadingDialog!= null && loadingDialog.isShowing())
            {

                loadingDialog.dismiss();


                loadingDialog = null;
            }

        }
        /**
         * 关闭对话框
         *
         */
        public  static void close(Context context)
        {
            if(loadingDialog == null)
            {
                return;
            }
            else if(loadingDialog!= null && loadingDialog.isShowing())
            {

                loadingDialog.dismiss();


                loadingDialog = null;
            }

        }



    }
