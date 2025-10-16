package com.holyes.ccssend5.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.holyes.ccssend5.lib.AccessWeb;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;
import com.holyes.ccssend5.myview.MyProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: BillProductUtil
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:00
 */
public class BillProductUtil {

        private Context context;
        private SqliteDataHelper sqliteDataHelper;
        private SysUserInfo sysUserInfo;
        private String lsv_aim;

        private Handler handler;

        private final int HandShowLoading = 1;
        private final int HandCloseLoading = 2;

        public BillProductUtil(final Context context)
        {
            this.context = context;
            sqliteDataHelper = SqliteDataHelper.getHelper(context);
            sysUserInfo = new SysUserInfo(context);
            //置为false，后面今日单据明细界面就不删除明细数据了
            sysUserInfo.setIsDownload(false);

            handler=new Handler(context.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what)
                    {
                        case HandShowLoading:
                            MyProgressDialog.show(context, "请稍候。。。", true, true);
                            break;
                        case HandCloseLoading:
                            MyProgressDialog.close();
                            break;
                    }

                }
            };
        }
        public void  downloadBillProduct(final String peigoodNo)
        {
            new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        ShowMessage.ShowMsg(handler, HandShowLoading, "");
                        //把配货单明细表删除
                        sqliteDataHelper.execSQL("delete from newtpeinomx");
                        //下载数据
                        AccessWeb accWeb = new AccessWeb(context);
                        List<Map<String, Object>> dataList = accWeb.GetDowLoadAgentInvoiceDetail(peigoodNo);
                        //把数据插入数据库
                        if(dataList.size()>0)
                        {
                            List<String> sqlList=new ArrayList<String>();
                            String goodsid,goodsdescription,colors,modelm,noscanqty,amount,sql;
                            for(Map<String ,Object> map:dataList)
                            {
                                goodsid =(String) map.get("goodsid");
                                goodsdescription=(String)map.get("goodsdescription");
                                modelm=(String)map.get("modelm");
                                colors=(String)map.get("colors");
                                noscanqty = (String)map.get("noscanqty");
                                amount = noscanqty;
                                sql="insert into newtpeinomx(goodsid,goodsdescription,modelm,colors,noscanqty,amount)" +
                                        " values('"+goodsid+"','"+goodsdescription+"','"+modelm+"','"+colors+"','"+noscanqty+"','"+amount+"')";
                                sqlList.add(sql);
                            }
                            sqliteDataHelper.BatchOperation(sqlList);

                        }

                        ShowMessage.ShowMsg(handler, HandCloseLoading, "");
                    } catch (Exception e) {
                        ShowMessage.ShowMsg(handler, HandCloseLoading, "");
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        /**
         * 产品代号是否在配货单里面
         * @param goodsid
         * @return true表示存在，false不存在
         */
        public boolean productIsInBillNo(String goodsid)
        {
            try {
                String result = sqliteDataHelper.execSQLString("select goodsid from newtpeinomx where goodsid=?",
                        new String[] { goodsid});
                if(result==null||result.isEmpty())
                {
                    return false;
                }else{
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        /**
         * 通过产品id来取得型号色号
         * @param goodsid 产品id
         * @return modelColorsArray[0] 是modelm，modelColorsArray[1] 是colors
         */
        public String[] getModelColors(String goodsid)
        {
            String str="型号,色号";
            List<Map<String, Object>> list= sqliteDataHelper.QueryDbList(String.format("select modelm,colors from newproduct where  goodsid = '%1$s'" ,goodsid),null);
            if(list.size()>0)
            {
                str=list.get(0).get("modelm")+","+list.get(0).get("colors")+",ccs";//如果都为空就会有分割问题
            }
            String[] modelColorsArray = str.split(",");
            return modelColorsArray;
        }

    }
