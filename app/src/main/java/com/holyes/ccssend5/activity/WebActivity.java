package com.holyes.ccssend5.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.myview.Loading;
import com.holyes.ccssend5.utils.DisplayUtil;

/**
 * @ClassName: WebActivity
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:12
 */
public class WebActivity extends Activity {

    private WebView mWebView;//显示在线word

    private Loading loading = null;//加载运行

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.activity_webview);
        initview();
    }

    private void initview() {

        loading = new Loading(WebActivity.this, "正在加载网页...", new Loading.OnLoadingback() {
            @Override
            public void back(String name) {
            }
        });
        loading.Show();
        mWebView = (WebView) findViewById(R.id.doc_webview);


        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (loading != null) {
                    loading.Close();
                }
            }
        });


        mWebView.setWebChromeClient(new WebChromeClient());

        // webview必须设置支持Javascript才可打开
        mWebView.getSettings().setJavaScriptEnabled(true);

        // 设置此属性,可任意比例缩放
        mWebView.getSettings().setUseWideViewPort(true);

        mWebView.loadUrl("http://emtong.com:9531/WxComments.aspx?ServiceNo=6B10DA1614C10EB0");
    }

    //设置字体为默认大小，不随系统字体大小改而改变
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null) {
            Configuration configuration = resources.getConfiguration();
            if (configuration != null && configuration.fontScale != 1.0f) {
                configuration.fontScale = 1.0f;//这里只设置字体，故不使用下面注释的方法
                //		                configuration.setToDefaults();
                resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            }
        }
        return resources;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

