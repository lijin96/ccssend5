package com.holyes.ccssend5.dao;

import android.content.Context;
import android.widget.TextView;

import com.holyes.ccssend5.lib.SqliteDataHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ScanDataDao
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:13
 */
public class ScanDataDao {

        /**
         * 根据扫描获得的结果修改数据库和界面
         * @param context 上下文
         * @param tv_model_colors 型号和色号文本控件
         * @param tv_curqty 当前扫描数量文本控件
         * @param tv_totalqty 总共扫描数量文本控件
         * @param tv_billno 入库单号文本控件
//         * @param nScanCount 当前扫描的序号
         * @param curcount 当前扫描的数量
         * @param goodsid 产品编号
         * @param modelm 型号
         * @param colors 色号
         * @param mBillNo 单号
         * @throws Exception
         */
        public static void updateDataAndUi(Context context, TextView tv_model_colors, TextView tv_curqty,
                                           TextView tv_totalqty, TextView tv_billno, String curcount,
                                           String goodsid, String modelm, String colors, String mBillNo) throws Exception
        {
            //没有扫描的数量,总的扫描数量
            int noscanqty = 0;
            String totalsacnqty = "0";
            //如果数据库里有该产品了就修改，没有就插入
            if(SqliteDataHelper.getHelper(context).execSQLInt("select sum(curcount) from newscandate where  goodsid = '"+goodsid+"' GROUP BY goodsid ") > 0)
            {
                //如果返回的数据大于本地存储的数据，那就修改数据
                if (Integer.parseInt(curcount)>SqliteDataHelper.getHelper(context).execSQLInt("select sum(curcount) from newscandate where  goodsid = '"+goodsid+"' GROUP BY goodsid ") ) {
                    SqliteDataHelper.getHelper(context).execSQL(String.format("update newscandate set  curcount = '%1$s' where    goodsid = '%2$s'" ,curcount,goodsid));
                }
            }
            else
            {
                SqliteDataHelper.getHelper(context).execSQL(String.format("insert into newscandate(goodsid,modelm,colors,curcount)values('%1$s','%2$s','%3$s','%4$s')",goodsid,modelm,colors,curcount));
            }

            //改变配货单明细表对应扫描的型号色号的数量
            //		noscanqty = SqliteDataHelper.getHelper(context).execSQLInt("select noscanqty from newtpeinomx where  goodsid = '"+goodsid+"'");
            //		SqliteDataHelper.getHelper(context).execSQL(String.format("update newtpeinomx set  noscanqty = '%1$s' where  goodsid = '%2$s'" ,String.valueOf(noscanqty-1),goodsid));
            //		//如果剩余数量为0，证明已经发完该型号，则在配货单明细里面删除该型号。
            //		if((noscanqty-1)==0)
            //		{
            //			SqliteDataHelper.getHelper(context).execSQL("delete from newtpeinomx where goodsid = '"+goodsid+"'");
            //		}

            int amount = SqliteDataHelper.getHelper(context).execSQLInt("select amount from newtpeinomx where  goodsid = '"+goodsid+"'");
            SqliteDataHelper.getHelper(context).execSQL(String.format("update newtpeinomx set  noscanqty = '%1$s' where  goodsid = '%2$s'" ,
                    String.valueOf(amount-Integer.parseInt(curcount)),goodsid));
            //如果剩余数量为0，证明已经发完该型号，则在配货单明细里面删除该型号。
            if((amount-Integer.parseInt(curcount))==0)
            {
                SqliteDataHelper.getHelper(context).execSQL("delete from newtpeinomx where goodsid = '"+goodsid+"'");
            }

            totalsacnqty = SqliteDataHelper.getHelper(context).execSQLString("select sum(curcount) from newscandate ");
            tv_model_colors.setText(modelm + "-" + colors+"\n"+"("+goodsid+")");
            tv_curqty.setText(curcount);
            tv_totalqty.setText(totalsacnqty);
            if(tv_billno!=null)
                tv_billno.setText(mBillNo);
        }


        /**
         * 根据扫描获得的结果修改数据库和界面
         * @param context 上下文
         * @param tv_model_colors 型号和色号文本控件
         * @param tv_curqty 当前扫描数量文本控件
         * @param tv_totalqty 总共扫描数量文本控件
         * @param tv_billno 入库单号文本控件
         * @param nScanCount 当前扫描的序号
         * @param curcount 当前扫描的数量
         * @param goodsid 产品编号
         * @param modelm 型号
         * @param colors 色号
         * @param mBillNo 单号
         * @throws Exception
         */
        //	public static void updateDataAndUi(Context context,String curcount,
        //			String goodsid,String modelm,String colors,String mBillNo) throws Exception
        //			{
        //		//没有扫描的数量,总的扫描数量
        //		int noscanqty = 0;
        //		String totalsacnqty = "0";
        //		//如果数据库里有该产品了就修改，没有就插入
        //		Log.d("main", curcount+"--"+goodsid);
        //		if(SqliteDataHelper.getHelper(context).execSQLInt("select count(*) from newscandate where  goodsid = '"+goodsid+"' limit 1 ") > 0)
        //		{
        //			SqliteDataHelper.getHelper(context).execSQL(String.format("update newscandate set  curcount = '%1$s' where    goodsid = '%2$s'" ,curcount,goodsid));
        //		}
        //		else
        //		{
        //			SqliteDataHelper.getHelper(context).execSQL(String.format("insert into newscandate(goodsid,modelm,colors,curcount)values('%1$s','%2$s','%3$s','%4$s')",goodsid,modelm,colors,curcount));
        //		}
        //
        //		//改变配货单明细表对应扫描的型号色号的数量
        //		//		noscanqty = SqliteDataHelper.getHelper(context).execSQLInt("select noscanqty from newtpeinomx where  goodsid = '"+goodsid+"'");
        //		//		SqliteDataHelper.getHelper(context).execSQL(String.format("update newtpeinomx set  noscanqty = '%1$s' where  goodsid = '%2$s'" ,String.valueOf(noscanqty-1),goodsid));
        //		//		//如果剩余数量为0，证明已经发完该型号，则在配货单明细里面删除该型号。
        //		//		if((noscanqty-1)==0)
        //		//		{
        //		//			SqliteDataHelper.getHelper(context).execSQL("delete from newtpeinomx where goodsid = '"+goodsid+"'");
        //		//		}
        //
        //		int amount = SqliteDataHelper.getHelper(context).execSQLInt("select amount from newtpeinomx where  goodsid = '"+goodsid+"'");
        //		SqliteDataHelper.getHelper(context).execSQL(String.format("update newtpeinomx set  noscanqty = '%1$s' where  goodsid = '%2$s'" ,
        //				String.valueOf(amount-Integer.parseInt(curcount)),goodsid));
        //		//如果剩余数量为0，证明已经发完该型号，则在配货单明细里面删除该型号。
        //		if((amount-Integer.parseInt(curcount))==0)
        //		{
        //			SqliteDataHelper.getHelper(context).execSQL("delete from newtpeinomx where goodsid = '"+goodsid+"'");
        //		}
        //
        //		totalsacnqty = SqliteDataHelper.getHelper(context).execSQLString("select sum(curcount) from newscandate ");
        //
        //}

        /**
         * 查询合计
         * */
        public static String getAllNumber(Context context) {
            String totalsacnqty="";
            try {
                totalsacnqty = SqliteDataHelper.getHelper(context).execSQLString("select sum(curcount) from newscandate ");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return totalsacnqty;
        }
        /**
         * 查询当前型号的数量
         * */
        public static String getColorNum(Context context, String goodsid) {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            String num="";
            try {
                list = SqliteDataHelper.getHelper(context).QueryDbList(
                        "select curcount from newscandate where goodsid = '"+goodsid+"'", null);
                for (Map<String, Object> map : list) {
                    num=(map.get("curcount").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return num;
        }

    }
