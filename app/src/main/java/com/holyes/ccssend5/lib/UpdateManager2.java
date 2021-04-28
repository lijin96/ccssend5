package com.holyes.ccssend5.lib;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ccssend5.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @ClassName: UpdateManager2
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:29
 */
public class UpdateManager2 {

        private Context mContext;

        //提示语
        // private String updateMsg = "有最新的软件包哦，亲快下载吧~";

        //返回的安装包url
        //  private String apkUrl = "http://www.4006889521.cn:9525/ccs2014_msmv8.apk";//"http://www.4006889521.cn:9525/ccs2014.apk";//"http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk";

        private Dialog noticeDialog;

        private Dialog downloadDialog;
        /* 下载包安装路径 */
        private   String savePath = "";//"/sdcard/";

        private   String saveFileName ="";// savePath + "ccs_dev.apk";

        /* 进度条与通知ui刷新的handler和msg常量 */
        private ProgressBar mProgress;


        private static final int DOWN_UPDATE = 1;

        private static final int DOWN_OVER = 2;

        private int progress;

        private Thread downLoadThread;

        public boolean interceptFlag = false;

        //	    public boolean isupdate = false;

        private Handler mHandler = new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DOWN_UPDATE:
                        mProgress.setProgress(progress);
                        break;
                    case DOWN_OVER:
                        if(downloadDialog.isShowing())
                        {
                            downloadDialog.dismiss();
                        }
                        installApk();
                        break;
                    default:
                        break;
                }
            };
        };

        public UpdateManager2(Context context) {

            this.mContext = context;
            savePath = "/sdcard/";//context.getApplicationContext().getFilesDir().getAbsolutePath();
            saveFileName = savePath + "/ccs_dev.apk";
        }

        //外部接口让主Activity调用
        public void checkUpdateInfo(String title,String message){
            showNoticeDialog(title,message);
        }
        private void showNoticeDialog(final String title,final String message){
            SysUserInfo sysinfo = new SysUserInfo(mContext);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            showDownloadDialog("下载中，请稍候。。。");
                        }
                    })
                    .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    })  ;

            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }


        public void showDownloadDialog(String title){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
            builder.setTitle(title);

            final LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.progress, null);
            mProgress = (ProgressBar)v.findViewById(R.id.progress);

            builder.setView(v);

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    interceptFlag = true;
                }
            });
            downloadDialog = builder.create();
            downloadDialog.show();
            downloadDialog.setCanceledOnTouchOutside(false);
            downloadApk();
        }

        private Runnable mdownApkRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SysUserInfo sysinfo = new SysUserInfo(mContext);

                    URL url = null;
                    if("01".equals(sysinfo.getSoftType()))
                    {
                        url = new URL("http://www.4006889521.cn/softupdate/Android/Dev/company/ccs_dev8N.apk");

                    }else{
                        url = new URL("http://www.4006889521.cn/softupdate/Android/Dev/company/ccs_dev54N.apk");
                    }

                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();

                    File file = new File(savePath);
                    if(!file.exists())
                    {
                        file.mkdir();
                    }
                    String apkFile = saveFileName;
                    File ApkFile = new File(apkFile);
                    FileOutputStream fos = new FileOutputStream(ApkFile);

                    int count = 0;
                    byte buf[] = new byte[1024];

                    do{
                        int numread = is.read(buf);
                        count += numread;
                        progress =(int)(((float)count / length) * 100);
                        //更新进度
                        mHandler.sendEmptyMessage(DOWN_UPDATE);
                        if(numread <= 0){
                            //下载完成通知安装
                            mHandler.sendEmptyMessage(DOWN_OVER);
                            break;
                        }
                        fos.write(buf,0,numread);
                    }while(!interceptFlag);//点击取消就停止下载.

                    fos.close();
                    is.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch(IOException e){
                    e.printStackTrace();
                }

            }
        };

        /**
         * 下载apk
         */

        private void downloadApk(){
            downLoadThread = new Thread(mdownApkRunnable);
            downLoadThread.start();
        }
        /**
         * 安装apk
         */
        private void installApk(){
            File apkfile = new File(saveFileName);
            if (!apkfile.exists()) {
                return;
            }

            Process p;
            int status;

            try {
                p = Runtime.getRuntime().exec("chmod 777 " +  saveFileName );

                status = p.waitFor();
                if (status == 0) {
                    //chmod succeed
                    Toast.makeText(mContext, "下载成功", Toast.LENGTH_LONG).show();
                } else {
                    //chmod failed
                    Toast.makeText(mContext, "下载失败", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
            catch (InterruptedException e) {

                e.printStackTrace();
            }

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
            mContext.startActivity(i);

        }

        /**
         * 卸载app
         * @param context
         * @param packageName 要卸载的app的包名，第四代的是com.holyes.ccs_dev8
         */
        public static void uninstallApp(Context context,String packageName)
        {
            Uri packageURI = Uri.parse("package:"+packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(uninstallIntent);
//		Log.i("main","卸载了---"+packageName);
        }

        public static void showUninstallDialog(Context context)
        {
            TextView tvTitle = new TextView(context);
            tvTitle.setPadding(10, 10, 10, 10);
            tvTitle.setGravity(Gravity.CENTER);
            tvTitle.setText("温馨提示");
            tvTitle.setTextColor(Color.WHITE);
            tvTitle.setTextSize(22);

            AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
            //设置自定义标题
            builder.setCustomTitle(tvTitle);
            builder.setMessage("第五代已经安装完成，是否卸载第四代的发货通？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNegativeButton("取消", null);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }


    }

