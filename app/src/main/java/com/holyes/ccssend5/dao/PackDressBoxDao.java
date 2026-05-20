package com.holyes.ccssend5.dao;

import android.content.Context;

import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: PackDressBoxDao
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:13
 */
public class PackDressBoxDao {

        /**
         *产品数量是否扫描完成
         * @param context
         * @param goodsid 产品代号
         * @return
         */
        public static boolean queryNum(Context context, String goodsid)
        {
            SqliteDataHelper sqliteDataHelper = new SqliteDataHelper(context);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            int packnum=0,scannum=0;
            String sql  =  "select * from packdressboxscan where goodsid ='"+goodsid+"'";

            try {
                list =  SqliteDataHelper.getHelper(context).QueryDbList(sql,null);

                for (Map<String, Object> map : list) {
                    packnum=Integer.parseInt(map.get("packnum").toString());
                    scannum=Integer.parseInt(map.get("scannum").toString());

                }
                if(packnum==scannum){
                    return false;
                }else if (packnum>scannum) {
                    return true;
                }else if (packnum<scannum) {
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }



        /**
         * 按型号色号查找装盒入库的数据明细
         * @param context
         * @param lsv_etStr 关键字，如果不为空，模糊查找的时候用
         * @return
         */
        public static List<Map<String, Object>> getPackModelColorDetail(Context context,String lsv_etStr)
        {
            SqliteDataHelper sDataHelper = new SqliteDataHelper(context);
            String sql = "select goodsid,modelm,colors,sum(scannum) as curcount from packdressboxscan GROUP BY goodsid";
            if(lsv_etStr!=null&&!lsv_etStr.isEmpty())
            {
                sql="select goodsid,modelm,colors,sum(scannum) as curcount from packdressboxscan where " +
                        "goodsid like '%%"+lsv_etStr+"%%' or " +
                        "modelm like '%%"+lsv_etStr+"%%' or " +
                        "colors like '%%"+lsv_etStr+"%%' GROUP BY goodsid";
            }
            List<Map<String, Object>> list = sDataHelper.QueryDbList(sql, null);
            return list;
        }


        /**
         *产品数量和扫描数量是否相等
         * @param context
         * @return
         */
        public static boolean queryScanNum(Context context)
        {

            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            int packnum=0,scannum=0;
            String sql  =  "select sum(packnum) as packnum,sum(scannum) as scannum from packdressboxscan";

            try {
                list =  SqliteDataHelper.getHelper(context).QueryDbList(sql,null);

                for (Map<String, Object> map : list) {
                    packnum=Integer.parseInt(map.get("packnum").toString());
                    scannum=Integer.parseInt(map.get("scannum").toString());

                }
                if(packnum==scannum){
                    return true;
                }else if (packnum>scannum) {
                    return false;
                }else if (packnum<scannum) {
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }


        /**
         *产品总数
         * @return
         */
        public static String queryAllScanNum(Context context)
        {

            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            int packnum=0;
            String sql  =  "select sum(packnum) as packnum from packdressboxscan";

            try {
                list =  SqliteDataHelper.getHelper(context).QueryDbList(sql,null);

                for (Map<String, Object> map : list) {
                    packnum=Integer.parseInt(map.get("packnum").toString());

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return String.valueOf(packnum);
        }



        /**
         *当前条码是否存在本地
         * @param context
         * @param goodsid 产品代号
         * @return
         */
        public static boolean IsExistence (Context context,String goodsid,String code)
        {
            SqliteDataHelper sqliteDataHelper = new SqliteDataHelper(context);

            String sql  =  "select * from packdressboxscan where goodsid ='"+goodsid+"' and Barcode='"+code+"'";

            try {
                String result = sqliteDataHelper.execSQLString(sql);
                if(result.isEmpty())
                {
                    return true;
                }else{
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        /**
         * 改变产品的扫描数量
         * @param context
         */
        public static void updatePackingByNum(Context context,int num,String goodsid,String code)
        {
            SqliteDataHelper sDataHelper = new SqliteDataHelper(context);
            String amount=String.valueOf(num);
            String sql="";

            sql=String.format("update packdressboxscan set scannum='%1$s' where " +
                    " goodsid = '%2$s'",num,goodsid);

            try {
                sDataHelper.execSQL(sql);
            } catch (Exception e) {
                ShowMessage.Show(context, "修改扫描数量报错"+e.getMessage());
            }
        }

        /**
         * 清空产品的扫描数量
         * @param context
         */
        public static void emptyPackingByNum(Context context)
        {
            SqliteDataHelper sDataHelper = new SqliteDataHelper(context);
            String sql="";

            sql=String.format("update packdressboxscan set scannum='%1$s'","0");

            try {
                sDataHelper.execSQL(sql);
            } catch (Exception e) {
                ShowMessage.Show(context, "修改扫描数量报错"+e.getMessage());
            }
        }

        /**
         * 查询所有产品
         * @param context
         */
        public static List<Map<String,Object>> queryAllGoods(Context context)
        {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            String listsql="select * from packdressboxscan group by goodsid";
            list=SqliteDataHelper.getHelper(context).QueryDbList(listsql,null);
            return list;
        }


        /**
         * 查询产品的扫描数量
         * @param context
         */
        public static String  queryGoodsidNum(Context context,String goodsid)
        {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            String scannum="";
            String sql  =  "select * from packdressboxscan where goodsid ='"+goodsid+"'";

            try {
                list =  SqliteDataHelper.getHelper(context).QueryDbList(sql,null);

                for (Map<String, Object> map : list) {
                    scannum=map.get("scannum").toString();
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            return scannum;
        }




        /**
         * 查找产品id是否在本地数据中，true表示存在，false表示不存在
         * @param context
         * @param goodsid

         * @return
         */
        public static boolean queryGoodsid(Context context,String goodsid)
        {
            SqliteDataHelper sqliteDataHelper = new SqliteDataHelper(context);
            String sql = "select goodsid from packdressboxscan where goodsid ='"+goodsid+"'" ;

            try {
                String result = sqliteDataHelper.execSQLString(sql);
                if(result.isEmpty())
                {
                    return false;
                }else{
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 套餐内已扫数量合计（sum(scannum)）
         */
        public static int querySumScannum(Context context) {
            List<Map<String, Object>> list;
            String sql = "select sum(scannum) as scannum from packdressboxscan";
            try {
                list = SqliteDataHelper.getHelper(context).QueryDbList(sql, null);
                if (list == null || list.isEmpty()) {
                    return 0;
                }
                Object v = list.get(0).get("scannum");
                if (v == null || v.toString().isEmpty()) {
                    return 0;
                }
                return Integer.parseInt(v.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        /**
         * 当前产品在套餐明细中的计划数量 packnum（同一 goodsid 多行时取首行）
         */
        public static int queryPacknumForGoodsid(Context context, String goodsid) {
            List<Map<String, Object>> list;
            String sql = "select packnum from packdressboxscan where goodsid ='" + goodsid + "' limit 1";
            try {
                list = SqliteDataHelper.getHelper(context).QueryDbList(sql, null);
                if (list == null || list.isEmpty()) {
                    return 0;
                }
                Object v = list.get(0).get("packnum");
                if (v == null) {
                    return 0;
                }
                return Integer.parseInt(v.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }




    }

