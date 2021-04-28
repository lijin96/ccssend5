package com.holyes.ccssend5.select;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.utils.DisplayUtil;

/**
 * @ClassName: SelectSureConfirm
 * @Description: 是否确定选择
 * 内容(title)
 * @Author: lijin
 * @Date: 2021/3/10 9:59
 */
public class SelectSureConfirm extends Activity implements View.OnClickListener {

    private TextView txtmsg;
    private Button btn_ok, btn_exit;
    private ImageView iv_brand;

    private String lsv_imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.select_sure);

        txtmsg = (TextView) findViewById(R.id.textView1);
        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_exit = (Button) findViewById(R.id.btn_exit);
        iv_brand = (ImageView) findViewById(R.id.iv_brand);

        txtmsg.setText(getIntent().getStringExtra("title"));
        lsv_imgPath = getIntent().getStringExtra("imgPath");
        if (lsv_imgPath != null && !lsv_imgPath.isEmpty()) {
            Bitmap bm = BitmapFactory.decodeFile(lsv_imgPath);
            //将图片显示到ImageView中
            iv_brand.setImageBitmap(bm);
        }
        //
        btn_ok.setOnClickListener(this);
        btn_exit.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:

                setResult(RESULT_OK);
                finish();
                break;

            case R.id.btn_exit:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }

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

