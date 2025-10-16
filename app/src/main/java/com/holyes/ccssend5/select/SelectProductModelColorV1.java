package com.holyes.ccssend5.select;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ccssend5.R;
import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SortListMapComparator;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.Loading;
import com.holyes.ccssend5.utils.DisplayUtil;
import com.holyes.ccssend5.utils.SomeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectProductModelColor
 * @Description: 选择产品型号和色号
 * @Author: lijin
 * @Date: 2021/3/10 9:58
 */
public class SelectProductModelColorV1 extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Context context;
    private Spinner spinnerProductName, spinnerProductYear, spinnerProductModel, spinnerProdType;//筛选的spinner
    private SimpleAdapter adapter, spinnerNameAdapter, spinnerYearAdapter,
            spinnerModelAdapter, spinnerProdTypeAdapter;
    private View alertView;//AlertDialog的布局view
    private LayoutInflater inflater;
    private SysUserInfo sysUserInfo;
    private AlertDialog alertDialog;
    private Loading loading = null;

    private ListView listView;//列数据的listview
    private Button btn_cancel, btn_ok, select_model_btn_filter, select_model_btn_next;
    private EditText et_search;
    private TextView tv_count, tv_Allcount, tv_selected_model_colors, tv_dialog_title, tv_redownload;//统计数据条数，已选型号色号

    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> quantitieslist = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> listProductName, listProductYear, listProductModel, listProdType;//总list,品牌list,年份list,型号list
    private Map<String, Object> addMap = new HashMap<String, Object>();
    private Map<String, Object> itemMap;//listview的item

    private String pagegoodsid = "", pagemodelm = "", pagecolors = "";

    private String etStr = "", goodsid = "", modelm = "", colors = "";
    private String brandname = "", productyear = "", prodtype = "";//用在Spinner的sql作搜索条件
    private String sql, searchSql = "";//查找全部，模糊查找，筛选条件
    //	filterSql

    private boolean isConfirmFresh = true;//是否确认刷新，在弹出dialog后要点击确定或者取消后才可以刷新数据，
    //否则即使输入模糊条件都不会筛选。
    volatile boolean isPaused = false;// 暂停标志
    private int selectedBrandPosition = 0, selectedYearPosition = 0,
            selectedModelPosition = 0, selectedProdTypePosition = 0;//已经选中的筛选条件的序号，退出界面前下次点击筛选时还是这个

    private Thread reDownloadThread = null;

    private int Nextpage=0;

    int numdata=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_product_model_color);
        context = this;
        sysUserInfo = new SysUserInfo(this);
        //		sysUserInfo.SaveConfigString("searchProductSql", "");
        listView = (ListView) findViewById(R.id.select_model_listview);
        listView.setOnItemClickListener(this);
        btn_cancel = (Button) findViewById(R.id.select_model_btn_cancel);
        btn_ok = (Button) findViewById(R.id.select_model_btn_ok);
        tv_redownload = (TextView) findViewById(R.id.tv_redownload);

        btn_cancel.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        et_search = (EditText) findViewById(R.id.select_model_et_search);
        tv_count = (TextView) findViewById(R.id.select_model_tv_count);
        tv_Allcount = (TextView) findViewById(R.id.select_model_allcount);
        tv_selected_model_colors = (TextView) findViewById(R.id.selected_model_tv);

        select_model_btn_filter = (Button) findViewById(R.id.select_model_btn_filter);
        select_model_btn_filter.setOnClickListener(this);
        select_model_btn_next = (Button) findViewById(R.id.select_model_btn_next);
        select_model_btn_next.setOnClickListener(this);

        tv_redownload.setOnClickListener(this);

        SomeUtils.addTextViewUnderline(tv_redownload, "");
        //		sql="select * from newproduct where brandname like '%%"+brandname+"%%' and " +
        //				"productyear like '%%"+productyear+"%%' and "+
        //				"modelm like '%%"+modelm+"%%' and "+
        //				"prodtype like '%%"+prodtype+"%%' ";
        //		list=SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(sql, null);
        //		Collections.sort(list, new SortListMapComparator("goodsid"));
        //		adapter=new SimpleAdapter(context, list, R.layout.new_select_product_model_color_listview_item,
        //				new String[]{"goodsid","goodsdescription","prodtype"},
        //				new int[]{R.id.tv_goodsid,R.id.tv_product_descr,R.id.tv_prodtype});
        //		listView.setAdapter(adapter);
        //		tv_count.setText("共计 "+list.size()+" 条");

        listProductName = new ArrayList<Map<String, Object>>();
        listProductYear = new ArrayList<Map<String, Object>>();
        listProductModel = new ArrayList<Map<String, Object>>();
        listProdType = new ArrayList<Map<String, Object>>();
        addMap.put("brandname", "全部");
        addMap.put("productyear", "全部");
        addMap.put("modelm", "全部");
        addMap.put("prodtype", "全部");

//        initSpinner();
        try {
            numdata=SqliteDataHelper.getHelper(getApplicationContext()).execSQLInt("select count(*) from newproduct");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            numdata=0;
            e.printStackTrace();
        }
        tv_Allcount.setText("合计" + numdata + "条");

        if (!sysUserInfo.ReadConfigString("searchProductSql").isEmpty()) {
            searchSql = sysUserInfo.ReadConfigString("searchProductSql");
            freshListView(sysUserInfo.ReadConfigString("searchProductSql"));
            btn_cancel.setText("取消");
        } else {
            //弹出筛选dialog
            if (sysUserInfo.getEnterpriseId().equals("05")) {
                isConfirmFresh = true;
                freshListView("select * from newproduct order by productyear desc limit 0,200");
                btn_cancel.setText("取消");

            } else {
                isConfirmFresh = true;
//                showFilterAlertDialog();
                freshListView("select * from newproduct limit ?,200");
            }
        }
        et_search.addTextChangedListener(new EditTextWatch());

        loading = new Loading(SelectProductModelColorV1.this, "请稍候，下载中...", new Loading.OnLoadingback() {
            @Override
            public void back(String name) {
                if (reDownloadThread != null && reDownloadThread.isAlive()) {
                    isPaused = true;
                    reDownloadThread.interrupt();
                    ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage, "下载被中止了");
                    reDownloadThread = null;//>>>
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        sysUserInfo.SaveConfigString("searchProductSql", searchSql);
    }

    /**
     * edittext监听
     */
    class EditTextWatch implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if ("全部".equals(brandname)) {
                brandname = "";
            }
            if ("全部".equals(productyear)) {
                productyear = "";
            }
            if ("全部".equals(modelm)) {
                modelm = "";
            }
            if ("全部".equals(prodtype)) {
                prodtype = "";
            }

            etStr = et_search.getText().toString().trim();
            if (!isConfirmFresh) {
                return;
            }
            if (etStr.isEmpty()) {
                Nextpage=0;
                sql = "select * from newproduct where brandname like '%%" + brandname + "%%' and " +
                        "productyear like '%%" + productyear + "%%' and " +
                        "modelm like '%%" + modelm + "%%' and " +
                        "prodtype like '%%" + prodtype + "%%' limit ?,200";
                freshListView(sql);
            } else {
                isConfirmFresh = true;
                Nextpage=0;
                searchSql = "select  * from newproduct  where " +
                        "brandname like '%%" + brandname + "%%' and " +
                        "productyear like '%%" + productyear + "%%' and " +
                        "modelm like '%%" + modelm + "%%' and " +
                        "prodtype like '%%" + prodtype + "%%' and " +
                        "(goodsid like '%%" + etStr + "%%' or " +
                        "goodsdescription like '%%" + etStr + "%%' or " +
                        "prodtype like '%%" + etStr + "%%' or " +
                        "productyear like '%%" + etStr + "%%') limit ?,200";

                freshListView(searchSql);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    }


    /**
     * 初始化Spinner
     */
    public void initSpinner() {
        inflater = LayoutInflater.from(context);
        alertView = inflater.inflate(R.layout.select_model_filter, null);
        spinnerProductName = (Spinner) alertView.findViewById(R.id.spinner_product_name_filfter);
        spinnerProductYear = (Spinner) alertView.findViewById(R.id.spinner_product_year_filfter);
        spinnerProductModel = (Spinner) alertView.findViewById(R.id.spinner_product_model_filfter);
        spinnerProdType = (Spinner) alertView.findViewById(R.id.spinner_prodtype_filfter);

        spinnerProductName.setOnItemSelectedListener(new SpinnerProductNameListener());
        spinnerProductYear.setOnItemSelectedListener(new SpinnerProductYearListener());
        spinnerProductModel.setOnItemSelectedListener(new SpinnerProductModelListener());
        spinnerProdType.setOnItemSelectedListener(new SpinnerProdTypeListener());

        spinnerProductName.setPrompt("\t\t\t\t\t\t品牌选择");
        spinnerProductYear.setPrompt("\t\t\t\t\t\t年份选择");
        spinnerProductModel.setPrompt("\t\t\t\t\t\t型号选择");
        spinnerProdType.setPrompt("\t\t\t\t\t\t品类选择");

        //适配产品品牌spinner
        listProductName.clear();
        listProductName.add(addMap);
        listProductName.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select brandname from newproduct group by brandname order by brandname", null));
        spinnerNameAdapter = new SimpleAdapter(context, listProductName, R.layout.select_model_filter_spinner_item, new String[]{"brandname"}, new int[]{R.id.tv_spinner_item});
        spinnerProductName.setAdapter(spinnerNameAdapter);
        spinnerProductName.setSelection(selectedBrandPosition, true);
        et_search.setText("");

        //适配品列类spinner
        listProdType.clear();
        listProdType.add(addMap);
        listProdType.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select prodtype from newproduct group by prodtype order by prodtype ", null));
        spinnerProdTypeAdapter = new SimpleAdapter(context, listProdType, R.layout.select_model_filter_spinner_item, new String[]{"prodtype"}, new int[]{R.id.tv_spinner_item});
        spinnerProdType.setAdapter(spinnerProdTypeAdapter);
        spinnerProdType.setSelection(selectedProdTypePosition, true);

        //设置dialog的title
        tv_dialog_title = new TextView(context);
        tv_dialog_title.setPadding(10, 10, 10, 10);
        tv_dialog_title.setGravity(Gravity.CENTER);
        tv_dialog_title.setText("产品资料选择");
        tv_dialog_title.setTextColor(Color.parseColor("#33B5E5"));
        tv_dialog_title.setTextSize(22);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_model_btn_cancel:
                if (btn_cancel.getText().toString().equals("取消")) {
                    tv_selected_model_colors.setText("（已选：）");
                    goodsid = "";
                    modelm = "";
                    colors = "";

                    pagecolors="";
                    pagemodelm="";
                    pagecolors="";

                    freshListView("select * from newproduct limit ?,200");

                    searchSql = "";
                    et_search.setText("");
                    btn_cancel.setText("返回");
                }
                //返回
                else {
                    finish();
                }

                break;
            case R.id.select_model_btn_ok:
                if ((pagemodelm + pagecolors).isEmpty()) {
                    SomeUtils.showToask(context, "未选择");
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("goodsid", pagegoodsid);
                    intent.putExtra("modelm", pagemodelm);
                    intent.putExtra("colors", pagecolors);
                    setResult(RESULT_OK, intent);
                    finish();

                }
                break;

            case R.id.select_model_btn_filter:
                isConfirmFresh = false;
                //初始化spinner
                initSpinner();
                //弹出筛选dialog
                showFilterAlertDialog();
                break;
            case R.id.tv_redownload:
                doDownloadProduct();
                break;

            case R.id.select_model_btn_next:
                if (numdata==list.size()){
                    Toast.makeText(context, "数据已全部加载", Toast.LENGTH_SHORT).show();
                }else{
//	                if (Nextpage==0){
//	                    Nextpage=1;
//	                }
                    Nextpage=Nextpage+200;
//	                searchSql = "select * from newproduct where brandname like  '%" + brandname + "%' and " +
//	                        "productyear like  '%" + productyear + "%' and modelm like  '%" + modelm + "%' " +
//	                        "and prodtype  like '%" + prodtype + "%' limit ?,200";
                    freshListView(searchSql);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 弹出筛选dialog
     */
    public void showFilterAlertDialog() {
        alertDialog = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT)
                .setCustomTitle(tv_dialog_title)
                .setView(alertView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //				if(!brandname.equals("全部")&&!productyear.equals("全部")&&!modelm.equals("全部")){
                        //					filterSql="select brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype from newproduct  where "+
                        //							" brandname='"+brandname +"' and productyear='"+productyear+"' and modelm='"+modelm+"'";
                        //				}else if(brandname.equals("全部")&&!productyear.equals("全部")&&!modelm.equals("全部")){
                        //					filterSql="select brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype from newproduct  where"+
                        //							" productyear='"+productyear+"' and modelm='"+modelm+"'";
                        //				}else if(!brandname.equals("全部")&&productyear.equals("全部")&&!modelm.equals("全部")){
                        //					filterSql="select brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype from newproduct  where"+
                        //							" brandname='"+brandname +"' and modelm='"+modelm+"'";
                        //				}else if(!brandname.equals("全部")&&!productyear.equals("全部")&&modelm.equals("全部")){
                        //					filterSql="select brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype from newproduct  where"+
                        //							" brandname='"+brandname +"' and productyear='"+productyear+"'";
                        //				}else if(brandname.equals("全部")&&productyear.equals("全部")&&!modelm.equals("全部")){
                        //					filterSql="select brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype from newproduct  where"+
                        //							" modelm='"+modelm+"'";
                        //				}else if(brandname.equals("全部")&&!productyear.equals("全部")&&modelm.equals("全部")){
                        //					filterSql="select brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype from newproduct  where"+
                        //							" productyear='"+productyear+"'";
                        //				}else if(!brandname.equals("全部")&&productyear.equals("全部")&&modelm.equals("全部")){
                        //					filterSql="select brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype from newproduct  where"+
                        //							" brandname='"+brandname+"'";
                        //				}else if(brandname.equals("全部")&&productyear.equals("全部")&&modelm.equals("全部")){
                        //					filterSql="select  brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype from newproduct ";
                        //				}

                        isConfirmFresh = true;
                        ///
                        if ("全部".equals(brandname)) {
                            brandname = "";
                        }
                        if ("全部".equals(productyear)) {
                            productyear = "";
                        }
                        if ("全部".equals(modelm)) {
                            modelm = "";
                        }
                        if ("全部".equals(prodtype)) {
                            prodtype = "";
                        }
                        Nextpage=0;
                        searchSql = "select * from newproduct where brandname like  '%" + brandname + "%' and " +
                                "productyear like  '%" + productyear + "%' and modelm like  '%" + modelm + "%' " +
                                "and prodtype  like '%" + prodtype + "%' limit ?,500";
                        freshListView(searchSql);
                        btn_cancel.setText("取消");
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isConfirmFresh = true;
                    }
                })
                .create();

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * 刷新产品列表
     *
     * @param sqls 根据sql查找数据刷新
     */
    public void freshListView(String sqls) {
        if (!isConfirmFresh) {
            return;
        }
        if (Nextpage==0){
            list.clear();
        }
        List<Map<String, Object>> dataList=new ArrayList<Map<String,Object>>();
        dataList=SqliteDataHelper.getHelper(getApplicationContext()).findPart(sqls, Nextpage);
        if(dataList.size()==0){
            Toast.makeText(context, "数据已全部加载", Toast.LENGTH_SHORT).show();
        }else{
            list.addAll(dataList);
            //把集合数据先排序
            Collections.sort(list, new SortListMapComparator("goodsid"));
            adapter = new SimpleAdapter(context, list, R.layout.new_select_product_model_color_listview_item,
                    new String[]{"goodsid", "goodsdescription", "prodtype"},
                    new int[]{R.id.tv_goodsid, R.id.tv_product_descr, R.id.tv_prodtype});
            listView.setAdapter(adapter);
            tv_count.setText("当前" + list.size() + " 条");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        itemMap = (Map<String, Object>) adapterView.getItemAtPosition(position);
        pagegoodsid = (String) itemMap.get("goodsid");
        pagemodelm = (String) itemMap.get("modelm");
        pagecolors = (String) itemMap.get("colors");
        tv_selected_model_colors.setText("（已选：" + pagemodelm + "-" + pagecolors + "）");
        btn_cancel.setText("取消");
    }

    //品牌Spinner监听
    class SpinnerProductNameListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView1, View view1, int position1,
                                   long id1) {
            Map<String, Object> map = (Map<String, Object>) adapterView1.getItemAtPosition(position1);
            brandname = (String) map.get("brandname");

            selectedBrandPosition = position1;

            //选择了品牌后就要对年份数据进行更新
            listProductYear.clear();
            listProductYear.add(addMap);
            if (brandname.equals("全部")) {
                listProductYear.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select productyear from newproduct group by productyear order by productyear desc", null));
            } else {
                listProductYear.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select productyear from newproduct where brandname  =  '" + brandname + "' group by productyear order by productyear desc", null));
            }
            spinnerYearAdapter = new SimpleAdapter(context, listProductYear, R.layout.select_model_filter_spinner_item, new String[]{"productyear"}, new int[]{R.id.tv_spinner_item});
            spinnerProductYear.setAdapter(spinnerYearAdapter);
            if (selectedYearPosition >= listProductYear.size()) {
                selectedYearPosition = 0;
            }
            spinnerProductYear.setSelection(selectedYearPosition, true);
            spinnerYearAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView1) {
        }

    }

    //年份Spinner监听
    class SpinnerProductYearListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView2, View view2, int position2,
                                   long id2) {
            Map<String, Object> map = (Map<String, Object>) adapterView2.getItemAtPosition(position2);
            productyear = (String) map.get("productyear");

            selectedYearPosition = position2;

            //选择了年份后就要对产品型号更新
            listProductModel.clear();
            listProductModel.add(addMap);
            if (brandname.equals("全部") && productyear.equals("全部")) {
                listProductModel.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select modelm from newproduct group by modelm order by modelm ", null));
            } else if (!brandname.equals("全部") && productyear.equals("全部")) {
                listProductModel.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select modelm from newproduct where brandname  = '" + brandname + "' group by modelm order by modelm ", null));
            } else if (brandname.equals("全部") && !productyear.equals("全部")) {
                listProductModel.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select modelm from newproduct where productyear='" + productyear + "'  group by modelm order by modelm ", null));
            } else {
                listProductModel.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select modelm from newproduct where brandname  = '" + brandname + "' and productyear='" + productyear + "'  group by modelm order by modelm ", null));
            }
            spinnerModelAdapter = new SimpleAdapter(context, listProductModel, R.layout.select_model_filter_spinner_item, new String[]{"modelm"}, new int[]{R.id.tv_spinner_item});
            spinnerProductModel.setAdapter(spinnerModelAdapter);
            if (selectedModelPosition >= listProductModel.size()) {
                selectedModelPosition = 0;
            }
            spinnerProductModel.setSelection(selectedModelPosition, true);
            spinnerModelAdapter.notifyDataSetChanged();

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView2) {
        }

    }

    //型号Spinner监听
    class SpinnerProductModelListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView3, View view3, int position3,
                                   long id3) {
            Map<String, Object> map = (Map<String, Object>) adapterView3.getItemAtPosition(position3);
            modelm = (String) map.get("modelm");
            selectedModelPosition = position3;
            //选择型号不影响其他筛选条件
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView3) {
        }

    }

    //品类Spinner监听
    class SpinnerProdTypeListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView4, View view4, int position4,
                                   long id3) {
            Map<String, Object> map = (Map<String, Object>) adapterView4.getItemAtPosition(position4);
            prodtype = (String) map.get("prodtype");
            selectedProdTypePosition = position4;
            //选择品类不影响其他筛选条件
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView3) {
        }

    }

    /**
     * 获取点击事件,是否隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    public void doDownloadProduct() {
        loading.Show();
        reDownloadThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    //把旧数据删除
                    SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newproduct");
                    //重新下载
                    reDownloadProduct();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        reDownloadThread.start();
    }

    public void reDownloadProduct() throws NumberFormatException, Exception {

        String maxDataTime = getMaxUprecndate("newproduct");
        int companycount = Integer.parseInt(AccessWeb.getHelper(getApplicationContext()).GetDownLoadGoodsRecord(maxDataTime));
        int companycurcount = 0;
        setProgressBarMax(companycount);

        List<Map<String, Object>> map;
        String BrandName, GoodsId, GoodsDescription, Modelm, Colors, GoodsYear, ProdType, Uprecndate;
        for (maxDataTime = getMaxUprecndate("newproduct"); companycurcount < companycount; maxDataTime = getMaxUprecndate("newproduct")) {
            map = AccessWeb.getHelper(getApplicationContext()).GetDownLoadGoodsInfor(maxDataTime);
            //			Log.i("main", "下载产品---最大时间："+maxDataTime+"----这次下载的数量="+map.size());
            if (map.size() == 0) {
                return;
            }
            for (Map<String, Object> map2 : map) {
                if (isPaused)
                    return;
                BrandName = (String) map2.get("BrandName");
                GoodsId = (String) map2.get("GoodsId");
                GoodsDescription = (String) map2.get("GoodsDescription");
                Modelm = (String) map2.get("Modelm");
                Colors = (String) map2.get("Colors");
                GoodsYear = (String) map2.get("GoodsYear");
                ProdType = (String) map2.get("ProdType");
                Uprecndate = (String) map2.get("Uprecndate");

                String sqlInsert = "insert into newproduct("
                        + "brandname, goodsid , goodsdescription, modelm ,colors,productyear, prodtype ,uprecndate)"
                        + " values(?,?,?,?,?,?,?,?)";
                String sqlUpdate = "update newproduct "
                        + "set goodsdescription =?,brandname=?,modelm = ? ,colors = ?,productyear = ?,prodtype = ?, uprecndate = ? where goodsid=? ";
                try {
                    String rest = "";
                    rest = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select goodsid from newproduct where goodsid=?", new String[]{GoodsId});
                    if (rest == "") {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlInsert,
                                new String[]{BrandName, GoodsId, GoodsDescription, Modelm, Colors,
                                        GoodsYear, ProdType, Uprecndate});
                    } else {
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sqlUpdate,
                                new String[]{GoodsDescription, BrandName, Modelm, Colors,
                                        GoodsYear, ProdType, Uprecndate, GoodsId});
                    }

                    setProgressBarValue(companycurcount++);
                    showTip("下载产品资料..." + companycurcount + "/" + companycount);
                } catch (Exception e) {
                    throw new Exception("新增或修改产品错误：" + e.getMessage());
                }

            }

        }
        if (!isPaused) // 正常
        {
            ShowMessage.ShowMsg(handler, ShowMessage.HandSuccess, "下载完成");
        } else // 线程被停止a
        {
            ShowMessage.ShowMsg(handler, ShowMessage.HandSuccess, "");
        }
    }

    private void showTip(String msg) {
        ShowMessage.ShowMsg(handler, 5, msg);
    }

    private void setProgressBarValue(int value) {
        ShowMessage.ShowMsg(handler, 6, String.valueOf(value));
    }

    private void setProgressBarMax(int value) {
        ShowMessage.ShowMsg(handler, ShowMessage.HandInitProgress, String.valueOf(value));
    }

    /**
     * 获取最大的uprecndate
     *
     * @param table 表名
     * @return
     */
    public String getMaxUprecndate(String table) {
        String maxUprecndate = "";
        try {
            String sqlSelect = String.format(
                    "select max(uprecndate) from %1$s", table);
            Cursor cursor = SqliteDataHelper.getHelper(getApplicationContext())
                    .getCursor(sqlSelect);
            cursor.moveToFirst();
            if (cursor != null && cursor.getCount() > 0) {
                maxUprecndate = cursor.getString(0);
            }
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
            if (maxUprecndate == null) {
                maxUprecndate = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            maxUprecndate = "";
        }
        return maxUprecndate;
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 5:
                    loading.setTipText(msg.obj.toString());
                    break;
                case 6:
                    loading.SetProgressValue(Integer.parseInt(msg.obj.toString()));
                    break;
                case ShowMessage.HandSuccess: //
                    if (loading != null)
                        loading.Close();
                    if (!msg.obj.toString().isEmpty())
                        ShowMessage.Show(SelectProductModelColorV1.this, msg.obj.toString());
                    loading.setTipText("请稍候，下载中...");
                    Nextpage=0;
                    sql = "select * from newproduct  limit ?,200";
                    selectedBrandPosition = 0;
                    selectedYearPosition = 0;
                    selectedModelPosition = 0;
                    selectedProdTypePosition = 0;
                    freshListView(sql);
                    initSpinner();
                    break;
                default:
                    break;
            }
        }

        ;
    };

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

