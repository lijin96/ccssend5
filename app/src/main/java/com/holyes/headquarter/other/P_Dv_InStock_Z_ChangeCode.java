package com.holyes.headquarter.other;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.entity.ChangeCodePara;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.MySound;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.select.QueryChangeCodeDetail;
import com.holyes.ccssend5.utils.PrintUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_InStock_Z_ChangeCode
 * @Description: 产品换标
 * @Author: lijin
 * @Date: 2021/3/10 14:00
 */
public class P_Dv_InStock_Z_ChangeCode extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SqliteDataHelper sqliteDataHelper;
    private SysUserInfo sysUserInfo;
    private MySound sound;
    private PrintUtil printBillSend;
    private ChangeCodePara codePara;

    private EditText et_old_code, et_new_code;
    private TextView tv_old_mess, tv_old_code, tv_new_code, tv_result, tv_error,tv_information;
    private RadioGroup radioGroup;
    private Button btn_list, btn_print, btn_exit;

    private List<Map<String, Object>> scanDataList;

    private final String Lsc_Logistics = "0";//物流码
    private final String Lsc_Security = "1";//防伪码
    private final int HandRequestFocus = 3;
    private String changeCodeType = "", OldCode = "", NewBarcode = "", result, sql;

    private String  product_id = "",modelm = "",colors = "";

    private int nScanQty=0;
    private TextView tv_totalqty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_dv_instock_z_changecode);
        mContext = this;
        accWeb = new AccessWeb(getApplicationContext());
        codePara = new ChangeCodePara();
        sound = MySound.getMySound(this);
        sqliteDataHelper = new SqliteDataHelper(mContext);
        printBillSend = new PrintUtil();
        sysUserInfo = new SysUserInfo(mContext);

        et_old_code = (EditText) findViewById(R.id.et_old_code);
        et_new_code = (EditText) findViewById(R.id.et_new_code);
        tv_old_mess = (TextView) findViewById(R.id.tv_old_mess);
        tv_old_code = (TextView) findViewById(R.id.tv_old_code);
        tv_new_code = (TextView) findViewById(R.id.tv_new_code);
        tv_result = (TextView) findViewById(R.id.tv_result);
        tv_error = (TextView) findViewById(R.id.tv_error);
        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        btn_list = (Button) findViewById(R.id.btn_list);
        btn_print = (Button) findViewById(R.id.btn_print);
        btn_exit = (Button) findViewById(R.id.btn_exit);


        tv_information=findViewById(R.id.tv_information);//换标成功显示的型号色号

        tv_totalqty=findViewById(R.id.tv_totalqty);
        tv_totalqty.setText("当前累计换标数："+nScanQty);

        et_old_code.setOnKeyListener(new EtBarodeOnkeyListener());
        et_new_code.setOnKeyListener(new EtBarodeOnkeyListener());
        radioGroup.setOnCheckedChangeListener(new RadioGroupCheckedListener());
        btn_list.setOnClickListener(new BtnListClickListener());
        btn_exit.setOnClickListener(new BtnExitClickListener());
        btn_print.setOnClickListener(new BtnPrintClickListener());

        changeCodeType = Lsc_Logistics;
        scanDataList = new ArrayList<Map<String, Object>>();
        //清空之前扫描的数据
        try {
            sqliteDataHelper.execSQL("delete from newchangecode");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class BtnPrintClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //添加换标数据测试
//			String sql1="insert into newchangecode(newcode,oldcode,oldcodetype)values('1234567990123456','9976543210123456','"+"物流码"+"')";
//			String sql2="insert into newchangecode(newcode,oldcode,oldcodetype)values('1234567880123456','8876543210123456','"+"防伪码"+"')";
//			String sql3="insert into newchangecode(newcode,oldcode,oldcodetype)values('1234567770123456','7776543210123456','"+"防伪码"+"')";
//			String sql4="insert into newchangecode(newcode,oldcode,oldcodetype)values('1234567660123456','6676543210123456','"+"物流码"+"')";
//			ArrayList<String> sqlList = new ArrayList<String>();
//			sqlList.add(sql1);
//			sqlList.add(sql2);
//			sqlList.add(sql3);
//			sqlList.add(sql4);
//			sqliteDataHelper.BatchOperation(sqlList);

            String sql = "select * from newchangecode";
            scanDataList = sqliteDataHelper.QueryDbList(sql, null);

            if (scanDataList.size() == 0) {
                ShowMessage.Show(mContext, "还未有成功换标的扫描数据！");
            } else {
                printBillSend.printChangeCode("             产品换标", scanDataList, sysUserInfo.getUserid());
            }

        }

    }

    class BtnExitClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (SomeUtils.isDoubleClick(mContext, true)) {
                finish();
            }
        }

    }

    class BtnListClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, QueryChangeCodeDetail.class);
            startActivity(intent);
        }

    }

    //输入框监听
    class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    if (v == et_new_code) {


                        if (et_new_code.getText().toString().trim().indexOf("=") != -1||et_new_code.getText().toString().trim().indexOf("http") != -1) {
                            //包含
                            NewBarcode = SomeUtils.InterceptCode(mContext, et_new_code.getText().toString().trim());
                        } else {
                            //不包含
                            NewBarcode = SomeUtils.UpdatefirstString(mContext,et_new_code.getText().toString().trim());
                        }


                        if (!SomeUtils.isAllNumber(mContext, NewBarcode)) {
                            MySound.errorSound();
                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + NewBarcode + "】");
                            et_new_code.setText("");
                            return true;
                        }
                        et_new_code.setText("");
                        tv_new_code.setText("新标物流码：" + NewBarcode);
                        tv_old_code.setText(tv_old_mess.getText().toString());
                        return true;//截止监听事件，不再后续
                    } else if (v == et_old_code) {

                        if (et_old_code.getText().toString().trim().indexOf("=") != -1||et_old_code.getText().toString().trim().indexOf("http") != -1) {

                            //包含
                            OldCode = SomeUtils.InterceptCode(mContext, et_old_code.getText().toString().trim());
                        } else {
                            //不包含
                            OldCode = SomeUtils.UpdatefirstString(mContext,et_old_code.getText().toString().trim());
                        }

                        if (!SomeUtils.isAllNumber(mContext, OldCode)) {
                            MySound.errorSound();
                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + OldCode + "】");
                            et_old_code.setText("");
                            return true;
                        }

                        et_old_code.setText("");
                        tv_old_code.setText(tv_old_mess.getText().toString() + OldCode);

                        access_send(NewBarcode, OldCode);
                        return false;//不截止监听事件，跳到下一个
                    }


                }
            }

            return false;
        }
    }

    class RadioGroupCheckedListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            //防伪码
            if (checkedId == R.id.radiobtn_security) {
                changeCodeType = Lsc_Security;
                tv_old_mess.setText("旧标防伪码：");
            }
            //物流码
            else {
                changeCodeType = Lsc_Logistics;
                tv_old_mess.setText("旧标物流码：");
            }
            tv_old_code.setText(tv_old_mess.getText().toString());
            et_old_code.setHint("请扫描" + tv_old_mess.getText().toString().replace("：", ""));


        }


    }


    public void access_send(final String newBarcode, final String oldBarcode) {
        if (newBarcode == null || newBarcode.isEmpty()) {
            ShowMessage.ShowMsg(handler, HandRequestFocus, "请先扫描新标物流码,谢谢！");
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    accWeb.mWebId = newBarcode;
                    //				   NewBarcode：新物流码(必传)
                    //                 OldCode：旧的防伪码或旧的物流码(必传，此值由换码类型来决定，如果换码类型为1，则输入的是旧的防伪码；如果换码类型为0，则输入旧的物流码)
                    //                 ChangeCodeType：换码类型(必传,值为：0或1)
                    codePara.setNewBarcode(newBarcode);
                    codePara.setOldCode(oldBarcode);
                    codePara.setChangeCodeType(changeCodeType);

                    result = accWeb.P_Dv_Scan("P_Dv_InStock_Z_ChangeCode", codePara.toJson());



                    String rest[] = result.split(",",-1);
                    if (rest.length>2){

                        product_id = rest[0].trim();
                        modelm = rest[1].trim();
                        colors = rest[2].trim();

                    }



                    if (result.length()>0) {
                        nScanQty++;
                        if (changeCodeType.equals("0")) {
                            sql = "insert into newchangecode(newcode,oldcode,oldcodetype)values('" + newBarcode + "','" + oldBarcode + "','" + "物流码" + "')";

                        } else {
                            sql = "insert into newchangecode(newcode,oldcode,oldcodetype)values('" + newBarcode + "','" + oldBarcode + "','" + "防伪码" + "')";
                        }
                        sqliteDataHelper.execSQL(sql);
//						SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(sql,null);
                    }
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");

                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                            e.getMessage());
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();
                    tv_result.setText("成功");
                    tv_result.setTextColor(Color.GREEN);
                    tv_error.setText("");
                    tv_information.setText("型号色号:"+modelm+colors);
                    et_new_code.requestFocus();
                    tv_totalqty.setText("当前累计换标数："+nScanQty);
                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    tv_result.setText("失败");
                    tv_result.setTextColor(Color.RED);
                    tv_error.setText(msg.obj.toString());
                    ShowMessage.Show(mContext, msg.obj.toString());
                    et_new_code.requestFocus();
                    tv_information.setText("");
                    break;

                case HandRequestFocus:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    //这里开启一个延迟线程把光标移到上面，不然可能不起作用。
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            et_new_code.requestFocus();
                        }
                    }, 200);
                    break;
                default:
                    break;
            }

        }

        ;
    };

    /**
     * 获取点击事件,是否隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    //设置字体为默认大小，不随系统字体大小改而改变
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null) {
            Configuration configuration = resources.getConfiguration();
            if (configuration != null && configuration.fontScale != 1.0f) {
                configuration.fontScale = 1.0f;//这里只设置字体，故不使用下面注释的方法
//	                configuration.setToDefaults();
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

