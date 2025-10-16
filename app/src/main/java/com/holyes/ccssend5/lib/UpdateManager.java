package com.holyes.ccssend5.lib;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.ccssend5.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @ClassName: UpdateManager
 * @Description: 升级APK工具类
 * @Author: lijin
 * @Date: 2021/3/6 14:29
 */
public class UpdateManager {

        private Context mContext;

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
                        installAPK();
                        break;
                    default:
                        break;
                }
            };
        };

        public UpdateManager(Context context) {

            this.mContext = context;
            savePath = "/sdcard/";//context.getApplicationContext().getFilesDir().getAbsolutePath();
            saveFileName = savePath + "/ccs_dev.apk";
        }

        //外部接口让主Activity调用
        public void checkUpdateInfo(String title){
            showNoticeDialog(title);
        }
        @SuppressLint("NewApi")
        private void showNoticeDialog(final String title){
            SysUserInfo sysinfo = new SysUserInfo(mContext);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
            builder.setTitle(title);
            builder.setMessage(sysinfo.getUpdateMsg());
            builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    showDownloadDialog(title);
                }
            });
            builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });
            builder.show();
        }


        @SuppressLint("NewApi")
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

            downloadApk();
        }

        private Runnable mdownApkRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SysUserInfo sysinfo = new SysUserInfo(mContext);

                    URL url = new URL(sysinfo.getApkUrl());
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();

                    File file = new File(savePath);
                    if(!file.exists()){
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
//        private void installApk(){
//            File apkfile = new File(saveFileName);
//            if (!apkfile.exists()) {
//                return;
//            }
//
//            Process p;
//            int status;
//
//            try {
//                p = Runtime.getRuntime().exec("chmod 777 " +  saveFileName );
//
//                status = p.waitFor();
//                if (status == 0) {
//                    //chmod succeed
//                    Toast.makeText(mContext, "chmod succeed", Toast.LENGTH_LONG).show();
//                } else {
//                    //chmod failed
//                    Toast.makeText(mContext, "chmod failed", Toast.LENGTH_LONG).show();
//                }
//            } catch (IOException e) {
//
//                e.printStackTrace();
//            }
//            catch (InterruptedException e) {
//
//                e.printStackTrace();
//            }
//
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
//            mContext.startActivity(i);
//
//        }

    private void installAPK() {
        String fileName = saveFileName;
//        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File apkFile = new File(fileName);
        if (!apkFile.exists()){
            Toast.makeText(mContext,"安装包文件不存在",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //安装完成后，启动app
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(mContext, "com.example.ccssend5.fileprovider", apkFile);//第二个参数要和Mainfest中<provider>内的android:authorities 保持一致
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }



    }

