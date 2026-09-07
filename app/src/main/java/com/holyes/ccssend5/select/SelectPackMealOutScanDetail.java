package com.holyes.ccssend5.select;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.dao.PackDressBoxDao;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SortListMapComparator;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 套餐装盒出货：整盒提交成功前的本次扫描明细（型号色号、数量、条码）
 */
public class SelectPackMealOutScanDetail extends Activity {

    public static final String EXTRA_DATA_CHANGED = "scan_detail_changed";

    private ListView listview;
    private Context mContext;
    private EditText et_search;
    private TextView tv_total;
    private TextView btn_delete;

    private List<Map<String, Object>> slist = new ArrayList<Map<String, Object>>();
    private int selectedIndex = -1;
    private boolean dataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.activity_packmeal_outscan_detail);
        mContext = this;
        listview = findViewById(R.id.listView1);
        tv_total = findViewById(R.id.tv_total);
        et_search = findViewById(R.id.et_search);
        btn_delete = findViewById(R.id.btn_delete);

        loadData("");
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedIndex = -1;
                loadData(et_search.getText().toString().trim());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
                listview.setSelection(position);
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
                confirmDeleteBarcode(getBarcodeAt(position));
                return true;
            }
        });

//        btn_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (selectedIndex < 0 || selectedIndex >= slist.size()) {
//                    ShowMessage.Show(mContext, "请先点击选中要删除的条码，或长按列表项删除");
//                    return;
//                }
//                confirmDeleteBarcode(getBarcodeAt(selectedIndex));
//            }
//        });
    }

    private String getBarcodeAt(int position) {
        if (position < 0 || position >= slist.size()) {
            return "";
        }
        Object bc = slist.get(position).get("barcode");
        return bc == null ? "" : bc.toString();
    }

    private void confirmDeleteBarcode(final String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
                .setTitle("删除条码")
                .setMessage("确认删除物流码【" + barcode + "】？\n删除后将同步减少已扫数量。")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDeleteBarcode(barcode);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void doDeleteBarcode(String barcode) {
        String goodsid = PackDressBoxDao.removeOutScanLineWithDecrement(mContext, barcode);
        if (goodsid == null) {
            ShowMessage.Show(mContext, "未找到该条码或删除失败");
            return;
        }
        dataChanged = true;
        selectedIndex = -1;
        ShowMessage.Show(mContext, "已删除条码【" + barcode + "】");
        loadData(et_search.getText().toString().trim());
    }

    private void loadData(String keyword) {
        slist = PackDressBoxDao.queryOutScanLines(mContext, keyword);
        if (slist == null) {
            slist = new ArrayList<Map<String, Object>>();
        }
        if (slist.isEmpty()) {
            listview.setAdapter(null);
            tv_total.setText("（共 0 条）");
            return;
        }
        initListView(slist);
    }

    private void initListView(List<Map<String, Object>> mList) {
        Collections.sort(mList, new SortListMapComparator("model_color"));
        SimpleAdapter adapter = new SimpleAdapter(SelectPackMealOutScanDetail.this, mList,
                R.layout.list_packmeal_outscan_detail,
                new String[]{"model_color", "curcount", "barcode"},
                new int[]{R.id.txt_model_color, R.id.txt_curcount, R.id.txt_barcode});
        listview.setAdapter(adapter);
        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    private void finishWithResultIfNeeded() {
        if (dataChanged) {
            Intent data = new Intent();
            data.putExtra(EXTRA_DATA_CHANGED, true);
            setResult(RESULT_OK, data);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        finishWithResultIfNeeded();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null) {
            Configuration configuration = resources.getConfiguration();
            if (configuration != null && configuration.fontScale != 1.0f) {
                configuration.fontScale = 1.0f;
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
