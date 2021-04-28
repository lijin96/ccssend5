package com.holyes.ccssend5.dao;

import android.annotation.SuppressLint;
import android.content.Context;

import com.holyes.ccssend5.entity.PackingScan;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: PackingScanDao
 * @Description: 装盒入库数据库操作类
 * @Author: lijin
 * @Date: 2021/3/6 14:13
 */
public class PackingScanDao {

        /**
         * 插入装盒入库数据
         * @param context
         * @param product_id 产品id
         * @param modelm 型号
         * @param colors 色号
         * @param barcode 条码
         * @param tempboxcode 临时盒标
         * @param scantime 扫描时间
         */
        public static void insertPackingInstock(Context context,String product_id,String modelm,
                                                String colors,String barcode,String tempboxcode,String scantime)
        {
            SqliteDataHelper sDataHelper = new SqliteDataHelper(context);
            String sql = String.format("insert into packingscan (product_id,modelm,colors,barcode," +
                    "tempboxcode,scantime) values ('%1$s','%2$s','%3$s','%4$s','%5$s'," +
                    "'%6$s')",product_id,modelm,colors,barcode,tempboxcode,scantime);
            try {
                sDataHelper.execSQL(sql);
            } catch (Exception e) {
                ShowMessage.Show(context, "插入装盒数据报错"+e.getMessage());
            }
        }

        /**
         * 改变装盒数据的盒标，已经完成装盒的数据就改变它的盒标码
         * @param context
         * @param boxcode 盒标码
         * @param tempBoxcode 临时盒标码
         */
        public static void updatePackingByCode(Context context,String boxcode,String tempBoxcode)
        {
            SqliteDataHelper sDataHelper = new SqliteDataHelper(context);
            String sql=String.format("update packingscan set boxcode='%1$s' where " +
                    " tempboxcode = '%2$s'",boxcode,tempBoxcode);
            try {
                sDataHelper.execSQL(sql);
            } catch (Exception e) {
                ShowMessage.Show(context, "修改盒标报错"+e.getMessage());
            }

        }

        /**
         * 删除，剔除产品码
         * @param context
         * @param barcode 产品码,如果为null就删除全部的扫描数据，否则只删除对应条码数据。
         */
        public static boolean deletePackingByCode(Context context,String barcode)
        {

            SqliteDataHelper sDataHelper = new SqliteDataHelper(context);
            String sql="";
            if(barcode==null)
            {
                sql=String.format("delete from packingscan");
            }else{
                sql=String.format("delete from packingscan where barcode = '%1$s'",barcode);
            }
            try {
                sDataHelper.execSQL(sql);
                return true;
            } catch (Exception e) {
                ShowMessage.Show(context, "剔除产品报错"+e.getMessage());
                return false;
            }
        }

        /**
         * 通过产品id查找产品的品牌或者系列
         * @param context
         * @param product_id 产品id
         * @param kind 如果为null就查品牌，否则就查系列
         * @return
         */
        public static String getProductBrandOrSeries(Context context,String product_id,String kind)
        {
            SqliteDataHelper sqliteDataHelper = new SqliteDataHelper(context);
            String sql ="";

            if(kind==null||kind.isEmpty())
            {
                //品牌brand
                sql = "select brandname from newproduct where goodsid ='"+product_id+"'";
            }else{
                //系列series
                sql =  "select prodtype from newproduct where goodsid ='"+product_id+"'";
            }
            String result ="";
            try {
                result = sqliteDataHelper.execSQLString(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;

        }

        /**
         * 获得扫描数量
         * @param context
         * @param product_id 产品代号，如果为空就是查询全部的数量
         * @param isAllData 是否查全部数据，如果为true就查已扫描装盒的数据，如果为false就只查已经入库的数据
         * @return
         */
        public static int getScanCount(Context context,String product_id,boolean isAllData)
        {
            SqliteDataHelper sqliteDataHelper = new SqliteDataHelper(context);
            String sql = "";
            if(product_id==null||product_id.isEmpty())
            {
                sql= "select count(*) from packingscan ";
                if(!isAllData)
                {
                    sql+=" where boxcode <> ''";
                }
            }else{
                sql= "select count(*) from packingscan where product_id ='"+product_id+"'";
                if(!isAllData)
                {
                    sql+=" and boxcode <> ''";
                }
            }

            int number = 0;
            try {
                number = sqliteDataHelper.execSQLInt(sql);
            } catch (Exception e) {
                number = 0;
                e.printStackTrace();
            }
            return number;

        }

        /**
         * 按型号色号查找装盒入库的数据明细
         * @param context
         * @param lsv_etStr 关键字，如果不为空，模糊查找的时候用
         * @return
         */
        public static List<Map<String, Object>> getPackModelColorDetail(Context context, String lsv_etStr)
        {
            SqliteDataHelper sDataHelper = new SqliteDataHelper(context);
            String sql = "select product_id,modelm,colors,count(barcode) as curcount from packingscan GROUP BY product_id";
            if(lsv_etStr!=null&&!lsv_etStr.isEmpty())
            {
                sql="select product_id,modelm,colors,count(barcode) as curcount from packingscan where " +
                        "product_id like '%%"+lsv_etStr+"%%' or " +
                        "modelm like '%%"+lsv_etStr+"%%' or " +
                        "colors like '%%"+lsv_etStr+"%%' GROUP BY product_id";
            }
            List<Map<String, Object>> list = sDataHelper.QueryDbList(sql, null);
            return list;
        }

        /**
         * 查找条码是否在本地数据中，true表示存在，false表示不存在
         * @param context
         * @param barcode
         * @param isInStocked 是否已经入库了的，true表示已经入库，boxcode不为空了，false相反
         * @return
         */
        public static boolean barcodeExistLocal(Context context,String barcode,boolean isInStocked)
        {
            SqliteDataHelper sqliteDataHelper = new SqliteDataHelper(context);
            String sql = "select barcode from packingscan where barcode ='"+barcode+"'" ;
            if(!isInStocked)
            {
                sql+=" and boxcode is null ";
            }
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
         * 查找条码值
         * @param context
         * @param tbarcode
         * @return
         * private String product_id;
        private String modelm;
        private String colors;
        private String barcode;
        private String tempboxcode;
        private String boxcode;
        private String scantime;
         */
        public static PackingScan getPackingByBarcode(Context context, String tbarcode)
        {
            SqliteDataHelper sDataHelper = new SqliteDataHelper(context);
            String sql = "select * from packingscan where barcode = '"+tbarcode+"'";
            Map<String, String> map = sDataHelper.QueryDbMap(sql, null);
            String product_id,modelm,colors,barcode,tempboxcode,boxcode,scantime;
            product_id = map.get("product_id").toString();
            modelm = map.get("modelm").toString();
            colors = map.get("colors").toString();
            barcode = map.get("barcode").toString();
            tempboxcode = map.get("tempboxcode").toString();
            boxcode = map.get("boxcode").toString();
            scantime = map.get("scantime").toString();
            PackingScan pScan = new PackingScan(product_id, modelm, colors,
                    barcode, tempboxcode, boxcode, scantime);
            return pScan;

        }


    }

