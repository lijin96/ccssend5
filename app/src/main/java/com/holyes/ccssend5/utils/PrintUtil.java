package com.holyes.ccssend5.utils;

import android.app.Activity;
import android.content.Context;
import android.device.PrinterManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.holyes.ccssend5.lib.SortListMapComparator;
import com.lvrenyang.io.Pos;
import com.lvrenyang.io.base.COMIO;
import com.lvrenyang.io.base.IOCallBack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.mipos.printer.IPrinter;
import cn.mipos.printer.MiposPrinter;

import tspl.HPRTPrinterHelper;


/**
 * @ClassName: PrintUtil
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:02
 */
public class PrintUtil implements IOCallBack {
    private int subStringIndex = 15;//换行截取的索引

    private Activity mActivity;
    private String name="";
    private String[] marks;
    private String OrderNum="";
    List<Map<String, Object>> list;

    private IPrinter printer;

    public PrintUtil() {
        //		printer = new PrinterManager();

    }



    ExecutorService es = Executors.newScheduledThreadPool(30);
    Pos mPos = new Pos();
    COMIO mCom = new COMIO();


    /**
     * 产品换标打印
     * @param title

     * @param slist
     * @param username
     */
    public void printChangeCode(String title,
                                List<Map<String, Object>> slist,  String username) {
        //把集合数据先排序
        int i = 0;
        PrinterManager printer = new PrinterManager();
        printer.prn_open();
        printer.prn_setupPage(384, -1);
        int ret = printer.prn_drawTextEx(title, 0, 0, -1, -1, "arial", 38,
                0, 0, 0);


        ret += printer.prn_drawTextEx(
                "-------------------------------------", 0, ret, -1, -1,
                "arial", 25, 0, 0, 0);
        ret +=printer.prn_drawTextEx("旧标类型：旧标条码", 0, ret, -1, -1, "arial",
                25, 0, 0, 0);
        ret += printer.prn_drawTextEx(
                "新物流码：新标条码", 0, ret, -1, -1,
                "arial", 25, 0, 0, 0);
        ret += printer.prn_drawTextEx(
                "-------------------------------------", 0, ret, -1, -1,
                "arial", 25, 0, 0, 0);

        String oldcodetype = "",oldCode = "",newCode="";
        for (Map<String, Object> map : slist)
        {
            i++;
            //旧码
            oldcodetype = map.get("oldcodetype").toString();
            oldCode = map.get("oldcode").toString();
            newCode = map.get("newcode").toString();

            ret += printer.prn_drawTextEx(i+"旧"+oldcodetype+"："+oldCode, 0, ret, -1, -1, "arial",
                    25, 0, 0, 0);
            ret += printer.prn_drawTextEx(
                    "新物流码："+newCode, 0, ret, -1, -1,
                    "arial", 25, 0, 0, 0);

            if (ret >= 1024) // 20行就打印
            {
                printer.prn_printPage(0);
                printer.prn_clearPage();
                ret = 0;
            }
        }

        ret += printer.prn_drawTextEx(
                "-------------------------------------", 0, ret, -1, -1,
                "arial", 25, 0, 0, 0);
        ret += printer.prn_drawTextEx("合计：" + slist.size() + "      打单：" + username,
                0, ret, -1, -1, "arial", 25, 0, 0, 0);
        SimpleDateFormat sDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        ret += printer.prn_drawTextEx(
                "日期时间：" + sDateFormat.format(new java.util.Date()), 0, ret,
                -1, -1, "arial", 25, 0, 0, 0);
        ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                0, 0);
        ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                0, 0);
        ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                0, 0);
        ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                0, 0);
        printer.prn_printPage(0);
        printer.prn_close();
    }

    /**
     * 打印功能
     * @title 打单抬头
     * @mark 定义在标题下面的内容，每一行为一个数组元素
     * @slist 型号色号数据
     * @sum 总计数量
     * @username 打单人
     * //ret +：表示打印之后换行
     * */
    public void prints(String title, String[] mark,
                       List<Map<String, Object>> slist,  String username) {
        //把集合数据先排序
        Collections.sort(slist, new SortListMapComparator("modelm"));

        PrinterManager printer = new PrinterManager();
        printer.prn_open();
        printer.prn_setupPage(384, -1);
        int ret = printer.prn_drawTextEx(title, 0, 0, -1, -1, "arial", 38,
                0, 0, 0);


        for (int i = 0; i < mark.length; i++)
        {
            //如果含有“单”字就认为是单据项，都是字母或数字，一行的长度可以设置为25，
            //否则认为都是其他客户或者店名之类的都是汉字，长度要短点
            if(mark[i].contains("单"))
            {
                subStringIndex = 25;//有
            }else{
                subStringIndex = 15;
            }
            //如果超过截取字符串长度就截取换行打印
            if(mark[i].length()>subStringIndex)
            {
                String str1="",str2="";
                str1 = mark[i].substring(0, subStringIndex);
                str2 = mark[i].substring(subStringIndex, mark[i].length());
                //如果还超过截取字符串长度就再截取换行打印
                if(str2.length()>subStringIndex)
                {
                    //截两次打印3行
                    String str3 = "",str4="";
                    str3 = str2.substring(0, subStringIndex);
                    str4 = str2.substring(subStringIndex, str2.length());
                    ret +=printer.prn_drawTextEx(str1, 0, ret, -1, -1, "arial",
                            25, 0, 0, 0);
                    ret +=printer.prn_drawTextEx(str3, 0, ret, -1, -1, "arial",
                            25, 0, 0, 0);
                    ret +=printer.prn_drawTextEx(
                            str4, 0, ret, -1, -1,"arial", 25, 0, 0, 0);

                }else{
                    //截一次打印两行
                    ret +=printer.prn_drawTextEx(str1, 0, ret, -1, -1, "arial",
                            25, 0, 0, 0);
                    ret +=printer.prn_drawTextEx(
                            str2, 0, ret, -1, -1,"arial", 25, 0, 0, 0);
                }

            }else{
                ret +=printer.prn_drawTextEx(mark[i], 0, ret, -1, -1, "arial",
                        25, 0, 0, 0);
            }
        }


        ret += printer.prn_drawTextEx(
                "-------------------------------------", 0, ret, -1, -1,
                "arial", 25, 0, 0, 0);
        printer.prn_drawTextEx(" 型号-色号 ", 0, ret, -1, -1, "arial",
                25, 0, 0, 0);
        ret += printer.prn_drawTextEx("数量", 300, ret, -1, -1, "arial", 25,
                0, 0, 0);
        ret += printer.prn_drawTextEx(
                "-------------------------------------", 0, ret, -1, -1,
                "arial", 25, 0, 0, 0);

        String modelm_colors = "",modelm_colors2="";
        int sum = 0;
        for (Map<String, Object> map : slist)
        {
            modelm_colors = map.get("modelm").toString() + "-"
                    + map.get("colors").toString();
            //如果型号色号长度超过16就要截取再分两行换行打印
            if(modelm_colors.length()>16)
            {
                modelm_colors2 = modelm_colors.substring(16, modelm_colors.length());
                modelm_colors = modelm_colors.substring(0, 16);
                ret +=printer.prn_drawTextEx(modelm_colors, 0, ret, -1, -1, "arial",
                        25, 0, 0, 0);
                printer.prn_drawTextEx(
                        modelm_colors2, 0, ret, -1, -1,"arial", 25, 0, 0, 0);
            }
            //否则直接
            else{
                printer.prn_drawTextEx(modelm_colors, 0, ret, -1, -1, "arial",
                        25, 0, 0, 0);
            }

            ret += printer.prn_drawTextEx(map.get("curcount").toString(),
                    300, ret, -1, -1, "arial", 25, 0, 0, 0);
            sum = sum + Integer.valueOf(map.get("curcount").toString());
//			if (ret >= 1024) // 20行就打印
//			{
//				printer.prn_printPage(0);
//				printer.prn_clearPage();
//				ret = 0;
//			}
        }

        ret += printer.prn_drawTextEx(
                "-------------------------------------", 0, ret, -1, -1,
                "arial", 25, 0, 0, 0);
        ret += printer.prn_drawTextEx("合计：" + sum + "      打单:" + username,
                0, ret, -1, -1, "arial", 25, 0, 0, 0);
        SimpleDateFormat sDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        ret += printer.prn_drawTextEx(
                "日期时间：" + sDateFormat.format(new java.util.Date()), 0, ret,
                -1, -1, "arial", 25, 0, 0, 0);
        ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                0, 0);
        ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                0, 0);
        ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                0, 0);
        ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                0, 0);
        printer.prn_printPage(0);
        printer.prn_close();

    }

    //汉印盒标打印
//    public void printHanSealPackBox(){
//        //当前蓝牙是否连接
//        if (HPRTPrinterHelper.IsOpened()){
//
//        }else{
//            Toast.makeText(mActivity, "未连接蓝牙", Toast.LENGTH_SHORT).show();
//        }
//    }



    /**
     * 打印测试，这里打印出来的数据是长数据，会换行打印
     */
    public void printTest()
    {
        String[] mark = new String[3];
        mark[0] = "零售单：DF-BillNo12345";
        mark[1] = "分销单：LF-BillNo12345-87654321";
        mark[2] = "分销店：广东省深圳市龙华新区民治街道泰明工业区一栋三层B区分销测试店";

        List<Map<String, Object>> slist = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
//		 map.put("modelm", "B67100");
//		 map.put("colors", "P01");
//		 map.put("curcount", "5");
//		 map.put("product_id", "B67100-P01");
//		 slist.add(0, map);

        for (int i = 0; i < 5; i++) {
            map = new HashMap<String, Object>();
            map.put("modelm", "B67100"+i);
            map.put("colors", "B67100-P01");
            map.put("curcount", "3"+i);
            map.put("product_id", "B67100-P012");
            slist.add(map);
        }

        prints("          分销店单据", mark, slist, "0001");
    }



    public void returnprint( final Activity activity,final String title,
                       final List<Map<String, Object>> slist,  final String username )
    {
//        Log.d("main print=", slist.toString());
        printer = new MiposPrinter(activity.getApplicationContext());
        mActivity=activity;

        String modelm_colors = "";
        int sum = 0;
        String scandate="";//打印时间
        try {
            printer.connect();
            printer.setFont(IPrinter.FONT_MEDIUM);
            printer.writeCenter(title.trim());
            printer.setFont(IPrinter.FONT_NORMAL);
//            for (int i = 0; i < mark.length; i++) {
//                printer.writeln(mark[i]);
//            }
            printer.writeln("退货单："+slist.get(0).get("oddno").toString());
            printer.writeln("零售商："+slist.get(0).get("custname").toString());

            printer.writeLine();
            printer.writeln("型号-色号", "数量");
            printer.writeLine();
            for (Map<String, Object> map : slist)
            {
                modelm_colors = map.get("modelm").toString() + "-"
                        + map.get("colors").toString();

                printer.writeln(modelm_colors, map.get("curcount").toString());

                sum = sum + Integer.valueOf(map.get("curcount").toString());

                if (map.containsKey("scandate")){
                    scandate=map.get("scandate").toString();
                }
            }
            printer.writeLine();
            printer.writeln("打单："+username,"合计："+sum);

            if (TextUtils.isEmpty(scandate)) {
                SimpleDateFormat sDateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                scandate=sDateFormat.format(new java.util.Date());
//            printer.writeln("日期时间：", sDateFormat.format(new java.util.Date()));
            }
            printer.writeln("日期时间：", scandate);
            printer.writeLine();
            //printer.writeCenter("打印Bitmap");
            //printer.printBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            //printer.printImage("/sdcard/test.png", 72, 72);
            printer.cut();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity,  e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            printer.disconnect();
        }
        Toast.makeText(mActivity, "打印成功", Toast.LENGTH_SHORT).show();
    }




    public void print( final Activity activity,final String title, final String[] mark,
                       final	List<Map<String, Object>> slist,  final String username )
    {
//        Log.d("main print=", slist.toString());
        printer = new MiposPrinter(activity.getApplicationContext());
        mActivity=activity;

        String modelm_colors = "";
        int sum = 0;
        String scandate="";//打印时间
        try {
            printer.connect();
            printer.setFont(IPrinter.FONT_MEDIUM);
            printer.writeCenter(title.trim());
            printer.setFont(IPrinter.FONT_NORMAL);
            for (int i = 0; i < mark.length; i++) {
                printer.writeln(mark[i]);
            }
            printer.writeLine();
            printer.writeln("型号-色号", "数量");
            printer.writeLine();
            for (Map<String, Object> map : slist)
            {
                modelm_colors = map.get("modelm").toString() + "-"
                        + map.get("colors").toString();

                printer.writeln(modelm_colors, map.get("curcount").toString());

                sum = sum + Integer.valueOf(map.get("curcount").toString());

                if (map.containsKey("scandate")){
                    scandate=map.get("scandate").toString();
                }
            }
            printer.writeLine();
            printer.writeln("打单："+username,"合计："+sum);

            if (TextUtils.isEmpty(scandate)) {
                SimpleDateFormat sDateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                scandate=sDateFormat.format(new java.util.Date());
//            printer.writeln("日期时间：", sDateFormat.format(new java.util.Date()));
            }
            printer.writeln("日期时间：", scandate);
            printer.writeLine();
            //printer.writeCenter("打印Bitmap");
            //printer.printBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            //printer.printImage("/sdcard/test.png", 72, 72);
            printer.cut();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity,  e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            printer.disconnect();
        }
        Toast.makeText(mActivity, "打印成功", Toast.LENGTH_SHORT).show();
    }


    public void printLens( final Activity activity,final String title, final String[] mark,
                           final	List<Map<String, Object>> slist,  final String username )
    {
        printer = new MiposPrinter(activity.getApplicationContext());
        mActivity=activity;
        String goodsid = "";//产品id
        String tmirror_cyl = "";//球柱镜
//        String tcylinder = "";//柱镜
        int sum = 0;
        String scandate="";//打印时间
        try {
            printer.connect();
            printer.setFont(IPrinter.FONT_MEDIUM);
            printer.writeCenter(title.trim());
            printer.setFont(IPrinter.FONT_NORMAL);
            for (int i = 0; i < mark.length; i++) {
                printer.writeln(mark[i]);
            }
            printer.writeLine();
            printer.writeln("产品ID", "");
            printer.writeln("球镜 柱镜", "数量");
            printer.writeLine();
            for (Map<String, Object> map : slist)
            {

                //如果产品id和上一次一样的就不打印，不一样的就赋值打印
                if (!goodsid.equals(map.get("product_id").toString())){
                    goodsid=map.get("product_id").toString();
                    printer.writeln(goodsid, "");
                }

                tmirror_cyl = map.get("Diopter").toString() + " "
                        + map.get("Astigmatism").toString();

                printer.writeln(tmirror_cyl, map.get("Num").toString());

                sum = sum + Integer.valueOf(map.get("Num").toString());
                scandate=map.get("Scandate").toString();
            }
            printer.writeLine();
            printer.writeln("打单："+username,"合计："+sum);

            SimpleDateFormat sDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");

//            printer.writeln("日期时间：", sDateFormat.format(new java.util.Date()));
            printer.writeln("日期时间：", scandate);
            printer.writeLine();
            //printer.writeCenter("打印Bitmap");
            //printer.printBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            //printer.printImage("/sdcard/test.png", 72, 72);
            printer.cut();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity,  e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            printer.disconnect();
        }
        Toast.makeText(mActivity, "打印成功", Toast.LENGTH_SHORT).show();
    }





    public void printT9( final Activity activity,final String title, final String[] mark,
                         final	List<Map<String, Object>> slist,  final String username )
    {

        mActivity=activity;
        OrderNum=title.trim();
        name=username;
        list=new ArrayList<Map<String,Object>>(slist);

        marks=new String[mark.length];

        for (int i = 0; i < mark.length; i++) {
            marks[i]=mark[i];
        }

        mPos.Set(mCom);
        mCom.SetCallBack(this);
        try {
            Toast.makeText(mActivity, "连接中...", Toast.LENGTH_SHORT)
                    .show();
            es.submit(new Runnable() {
                @Override
                public void run() {
                    mCom.Open("/dev/ttyS3", 9600, 0);
                }
            });
        } catch (Exception ex) {
            Toast.makeText(mActivity, ex.toString(), Toast.LENGTH_SHORT).show();
            return;
        }


    }



    public int PrintTicket(Context ctx, String title, String[] mark,
                           List<Map<String, Object>> slist, String username)
    {
        int bPrintResult = 0;

        String modelm_colors = "";
        int sum = 0;

        byte[] status = new byte[1];
        if (mPos.POS_RTQueryStatus(status, 1, 3000, 2) && ((status[0] & 0x12) == 0x12)) {
            if ((status[0] & 0x08) == 0) {
                if(mPos.POS_QueryStatus(status, 3000, 2)) {

                    if(!mPos.GetIO().IsOpened()){
                        return bPrintResult;
                    }
                    mPos.POS_FeedLine();
                    mPos.POS_S_Align(1);
                    mPos.POS_TextOut("------------------------------", 0, 0, 0, 0, 0x00, 0);
                    mPos.POS_FeedLine();
                    mPos.POS_TextOut(title.trim(), 0, 0,  0, 0, 0x00, 0x08);
                    mPos.POS_FeedLine();
                    mPos.POS_S_Align(0);
                    for (int i = 0; i < mark.length; i++)
                    {
                        mPos.POS_TextOut(mark[i].trim(), 0, 0, 0, 0,0x00, 0);
                        mPos.POS_FeedLine();
                    }
                    mPos.POS_S_Align(1);
                    mPos.POS_TextOut("------------------------------", 0, 0, 0, 0, 0x00, 0);
                    mPos.POS_FeedLine();
                    mPos.POS_S_Align(0);
                    mPos.POS_TextOut("型号-色号", 0, 0, 0, 0, 0x00, 0);
                    mPos.POS_TextOut("数量", 0, 300, 0, 0, 0x00, 0);
                    mPos.POS_FeedLine();

                    mPos.POS_S_Align(0);
                    for (Map<String, Object> map : slist)
                    {
                        modelm_colors = map.get("modelm").toString() + "-"
                                + map.get("colors").toString();
                        mPos.POS_TextOut(modelm_colors, 0, 0, 0, 0, 0x00, 0);
                        mPos.POS_TextOut(map.get("curcount").toString(), 0, 320, 0, 0, 0x00, 0);
                        mPos.POS_FeedLine();
                        sum = sum + Integer.valueOf(map.get("curcount").toString());
                    }
                    mPos.POS_FeedLine();
                    mPos.POS_TextOut("合计："+sum, 0, 0, 0, 0, 0x00, 0);
                    mPos.POS_FeedLine();

                    mPos.POS_TextOut("打单："+username, 0, 0, 0, 0, 0x00, 0);
                    mPos.POS_FeedLine();

                    SimpleDateFormat sDateFormat = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss");

                    mPos.POS_TextOut("日期时间：" + sDateFormat.format(new java.util.Date()), 0, 0, 0, 0, 0x00, 0);
                    mPos.POS_FeedLine();

                    mPos.POS_S_Align(1);
                    mPos.POS_TextOut("------------------------------", 0, 0, 0, 0, 0x00, 0);
                    mPos.POS_FeedLine();
                    mPos.POS_FeedLine();
                    mPos.POS_FeedLine();

                    mPos.POS_Beep(1, 5);//蜂鸣器

//					bPrintResult = mPos.POS_TicketSucceed(0, 10000);

                } else {
                    bPrintResult = -8;
                }
            } else {
                bPrintResult = -4;
            }
        } else {
            bPrintResult = -7;
        }

        return bPrintResult;
    }


    public static String ResultCodeToString(int code) {
        switch (code) {
            case 0:
                return "打印成功";
            case -1:
                return "连接断开";
            case -2:
                return "写入失败";
            case -3:
                return "读取失败";
            case -4:
                return "打印机脱机";
            case -5:
                return "打印机缺纸";
            case -7:
                return "实时状态查询失败";
            case -8:
                return "查询状态失败";
            case -6:
            default:
                return "未知错误";
        }
    }


    /* (non-Javadoc)
     * @see com.lvrenyang.io.base.IOCallBack#OnClose()
     */
    @Override
    public void OnClose() {
        // TODO Auto-generated method stub

    }


    /* (non-Javadoc)
     * @see com.lvrenyang.io.base.IOCallBack#OnOpen()
     */
    @Override
    public void OnOpen() {
        // TODO Auto-generated method stub
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                es.submit(new Runnable() {
                    @Override
                    public void run() {

//						String[] mark = new String[3];
//						mark[0] = "零售单：DF-BillNo12345";
//						mark[1] = "分销单：LF-BillNo12345-87654321";
//						mark[2] = "分销店：广东省深圳市龙华新区民治街道泰明工业区一栋三层B区分销测试店";
//
//						List<Map<String, Object>> slist = new ArrayList<Map<String, Object>>();
//
//						 Map<String, Object> map = new HashMap<String, Object>();
////						 map.put("modelm", "B67100");
////						 map.put("colors", "P01");
////						 map.put("curcount", "5");
////						 map.put("product_id", "B67100-P01");
////						 slist.add(0, map);
//
//						 for (int i = 0; i < 5; i++) {
//							 map = new HashMap<String, Object>();
//							 map.put("modelm", "B67100B67100B67100B67100"+i);
//							 map.put("colors", "B67100-P01");
//							 map.put("curcount", "3"+i);
//							 map.put("product_id", "B67100-P012");
//							 slist.add(map);
//						}


                        final int bPrintResult = PrintTicket(
                                mActivity,OrderNum,marks,list,name);


                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCom.Close();
                                // TODO Auto-generated method stub
                                Toast.makeText(
                                        mActivity.getApplicationContext(),
                                        (bPrintResult == 0) ? "打印成功" : "打印失败"
                                                + " "
                                                + ResultCodeToString(bPrintResult),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }


    /* (non-Javadoc)
     * @see com.lvrenyang.io.base.IOCallBack#OnOpenFailed()
     */
    @Override
    public void OnOpenFailed() {
        // TODO Auto-generated method stub

    }

}
