package com.holyes.ccssend5.select;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.myview.MyProgressDialog;
import com.holyes.ccssend5.utils.DisplayUtil;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectInventory
 * @Description: 回收扫描单据下载
 *               盘点补扫描单
 *              （这里后面要增加搜索功能）
 * @Author: lijin
 * @Date: 2021/3/10 9:57
 */
public class SelectInventory extends Activity {


        private ListView listview;

        private Handler hand;
        private Thread threadgetdate;
        List<Map<String, Object>> list;
        Map<String, Object> item;
        private TextView tv_count;

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            DisplayUtil.setDefaultDisplay(this);
            setContentView(R.layout.select_inventory);
            listview = (ListView) findViewById(R.id.listView1);
            tv_count = (TextView) findViewById(R.id.tv_count);

            hand = new handShowMsg();

            listview.setOnItemClickListener(new listViewClick());

            MyProgressDialog.show(this, "正在下载数据...", true, false);

            threadgetdate = new Thread(new thdgatedate());

            threadgetdate.start();
        }

        private class btn_peightclick implements View.OnClickListener {
            @Override
            public void onClick(View v) {


                MyProgressDialog.show(SelectInventory.this, "正在下载数据...", true, false);
                threadgetdate = new Thread(new thdgatedate());
                threadgetdate.start();
            }};

        private class thdgatedate implements Runnable {

            @Override
            public void run() {

                try {

//				list = AccessWeb.getHelper(getApplicationContext()).DowLoadFillOrdersDX();

                    ShowMessage.ShowMsg(hand, 1, "success");

                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        }

        private class handShowMsg extends Handler {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0: // 显示错误提示显示
                        ShowMessage.Show(SelectInventory.this, msg.obj.toString());
                        MyProgressDialog.close();
                        break;
                    case 1:

                        MyProgressDialog.close();

                        if(list!=null)
                        {
                            tv_count.setText("（共 "+list.size()+" 条）");
                        }
                        if(list.size()==0)
                        {
                            ShowMessage.ShowMsg(hand, 0, "抱歉，当前没有数据可下载");
                            break;
                        }
                        SimpleAdapter adapter = new SimpleAdapter(SelectInventory.this,
                                list, R.layout.list_inventory, new String[] {"Company_na", "ApplyNo" },
                                new int[] { R.id.txt_list1, R.id.txt_list2});

                        listview.setAdapter(adapter);


                        // listview.refreshDrawableState();

                        break;
                    default:
                        // MyProgressDialog.close();
                        // ShowMessage.Show(Xundian.this,msg.obj.toString());
                        break;
                }
            }
        }

        private class listViewClick implements AdapterView.OnItemClickListener {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {


                ListView listView = (ListView) parent;

                item = (Map<String, Object>) listView.getItemAtPosition(position);

                Intent intent = new Intent(SelectInventory.this, SelectSureConfirm.class);

                intent.putExtra("title", "确定要扫描当前回收单：" + item.get("ApplyNo") + "的产品吗？");

                startActivityForResult(intent, 0);
            }

        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                if (requestCode == 0) {

                    Intent intent = new Intent();
                    intent.putExtra("Company_na", (String) item.get("Company_na"));
                    intent.putExtra("ApplyNo", (String) item.get("ApplyNo"));

                    setResult(RESULT_OK, intent);
                    finish();
                }
            } else {
                // 取消不做任何操作
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
    }

