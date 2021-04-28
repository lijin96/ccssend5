package com.holyes.ccssend5.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SqliteDataHelper
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:29
 */
public class SqliteDataHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "scan.db";
        public static final int DATABASE_VERSION = 19;
        private static SQLiteDatabase database = null;
        private static boolean ifopen = false;
        private static Context myContext;
        private static String ErrorMsg = "";

        public SqliteDataHelper(Context context){
            //第一版 数据库
            super(context,DATABASE_NAME,null, DATABASE_VERSION);
        }


        private static SqliteDataHelper instance;

        public static  SqliteDataHelper getHelper(Context context)
        {
            if(instance == null)
            {
                instance = new SqliteDataHelper(context);
                open();
            }

            return instance;
        }


        /**
         * 打开数据库，默认是打开的
         */
        public static void open() {

            if (ifopen) {

                return;
            }
            database = instance.getWritableDatabase();

            if(null != database)
                ifopen = true;
        }

        /**
         * 关闭数据库
         */
        public static void closedb() {
            if (!ifopen) {

                return;
            }

            ifopen = false;

            database.close();
        }

        /**
         ** 删除数据 String sql = "delete from person where id =?"; params = {"2"};
         **/
        public static boolean DeleteDB(String sql, Object[] params) {
            boolean flag = false;
            try {
                database.execSQL(sql, params);
                flag = true;
            } catch (Exception e) {
                ErrorMsg = "数据库错误："+e.getMessage()+sql;
            }
            return flag;
        }

        /**
         * 批量操作数据库
         * @author hzm
         * @version 创建时间：2016-12-16 下午2:05:51
         * @param sqlList 执行操作的语句集合
         */
        public void BatchOperation(List<String> sqlList) {
            //		SQLiteDatabase db = getWritableDatabase();
            database = instance.getWritableDatabase();
            database.beginTransaction();
            try {
                for (String sql : sqlList) {
                    execSQL(sql);
                }
                // 设置事务标志为成功，当结束事务时就会提交事务
                database.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 结束事务
                database.endTransaction();
                //			database.close();
            }
        }

        /**
         * 删除品牌总公司账号信息
         * */
        public boolean CleanDataBase()
        {
            boolean flag = false;
            try {

                //			this.execSQL("delete from brandinfo");
                this.execSQL("delete from newdirect");
                this.execSQL("delete from newscandate");
                this.execSQL("delete from tpeino");
                this.execSQL("delete from newtpurcheck");
                this.execSQL("delete from packmealset");

                this.execSQL("delete from packdressboxscan");

                this.execSQL("delete from menus");
                this.execSQL("delete from newcompany");
                this.execSQL("delete from newproduct");
                this.execSQL("delete from allots");
                this.execSQL("delete from productbarcode");
                this.execSQL("delete from suser");
                this.execSQL("delete from newuser");
                this.execSQL("delete from newstock");
                this.execSQL("delete from newsupplier");
                this.execSQL("delete from newchangecode");

                this.execSQL("delete from agentinfor");//分销

                this.execSQL("delete from newretail");//零售
//			this.execSQL("delete from distributor");//分销，测试时用的，放弃
                this.execSQL("delete from storeinfor");//零售分销
                this.execSQL("delete from packingscan");//装盒入库扫描

                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return flag;
        }


        /**
         * 删除品牌代理商公司信息
         * */
        public boolean CleanStoreinfor()
        {
            boolean flag = false;
            try {
                this.execSQL("delete from storeinfor");//零售分销
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return flag;
        }

        /**
         * 删除数据 "person"," id > ?", new String[]{"5"}
         *
         * return 影响删除的条数
         * */
        public static int DeleteDB(String table, String whereClause, String[] whereArgs) {

            int flag = -1;
            try {
                flag = database.delete(table, whereClause, whereArgs);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return flag;

        }

        public   void execSQL(String sql) throws Exception {
            // SQLiteDatabase arg0 = SQLiteWork.this.getWritableDatabase();

            try {
                database.execSQL(sql);
            } catch (SQLException ex) {
                throw new Exception(ex.getMessage() + sql);
            }catch(Exception ex)
            {
                throw new Exception(ex.getMessage() + sql);
            }

        }

        public void execSQL(String sql, String[] value) throws Exception {
            // SQLiteDatabase arg0 = SQLiteWork.this.getWritableDatabase();

            try {
                database.execSQL(sql, value);
            } catch (SQLException ex) {
                throw new Exception(ex.getMessage() + value);
            }

        }

        public   String execSQLString(String sql) throws Exception {

            // SQLiteDatabase arg0 = SQLiteWork.this.getWritableDatabase();
            String rest = "";

            Cursor cursor = database.rawQuery(sql, null);
            try {

                if (cursor != null && cursor.getCount() > 0 && cursor.getColumnCount() > 0) {

                    cursor.moveToFirst();

                    rest = cursor.getString(0);

                    if (rest == null)
                        rest = "";
                }
            } catch (SQLException ex) {
                throw new Exception(ex.getMessage() + sql);
            } finally {
                if (null != cursor) {
                    cursor.close();
                    cursor = null;
                }
            }

            return rest;
        }

        public int execSQLInt(String sql) throws Exception {

            // SQLiteDatabase arg0 = SQLiteWork.this.getWritableDatabase();
            int rest = 0;

            Cursor cursor = database.rawQuery(sql, null);

            try {

                if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {

                    cursor.moveToFirst();

                    rest = cursor.getInt(0);//cursor.getString(0).toString();

                }
            } catch (SQLException ex) {
                rest = 0;
                //			throw new Exception(ex.getMessage() + sql);
            } finally {
                if (null != cursor) {
                    cursor.close();
                    cursor = null;
                }

            }

            return rest;
        }

        public String execSQLString(String sql, String[] selectargs)
                throws Exception {

            // SQLiteDatabase arg0 = SQLiteWork.this.getWritableDatabase();
            String rest = "";

            Cursor cursor = database.rawQuery(sql, selectargs);
            try {

                if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
                    cursor.moveToFirst();

                    rest = cursor.getString(0).toString();
                }
            } catch (SQLException ex) {
                throw new Exception(ex.getMessage() + sql);
            } finally {
                if (null != cursor) {
                    cursor.close();
                    cursor = null;
                }

            }

            return rest;
        }

        /** 多条数据查询数据查询 */
        public static List<Map<String, String>> exeselect(String sql) {

            List<Map<String, String>> list = new ArrayList<Map<String, String>>();

            // SQLiteDatabase database = null;

            Cursor cursor = null;

            try {
                // String sql = "select * from  person where id =?";

                cursor = database.rawQuery(sql, null);

                int colums = cursor.getColumnCount();

                while (cursor.moveToNext()) {
                    Map<String, String> map = new HashMap<String, String>();
                    for (int i = 0; i < colums; i++) {
                        String cols_name = cursor.getColumnName(i);

                        String cols_value = cursor.getString(cursor
                                .getColumnIndex(cols_name));

                        if (cols_value == null) {
                            cols_value = "";
                        }

                        map.put(cols_name, cols_value);
                    }

                    list.add(map);
                }

            } catch (Exception e) {

            } finally {
                if (null != cursor) {
                    cursor.close();
                }
            }

            return list;
        }

        /** 数据查询 */
        public static Map<String, String> exeselect(String sql, String[] selectionArgs) {

            Map<String, String> map = new HashMap<String, String>();

            // SQLiteDatabase database = null;

            Cursor cursor = null;

            try {
                // String sql = "select * from  person where id =?";

                cursor = database.rawQuery(sql, selectionArgs);

                int colums = cursor.getColumnCount();

                while (cursor.moveToNext()) {
                    for (int i = 0; i < colums; i++) {
                        String cols_name = cursor.getColumnName(i);

                        String cols_value = cursor.getString(cursor
                                .getColumnIndex(cols_name));

                        if (cols_value == null) {
                            cols_value = "";
                        }

                        map.put(cols_name, cols_value);
                    }
                }

            } catch (Exception e) {

            } finally {
                if (null != cursor) {
                    cursor.close();
                }
            }

            return map;
        }



        public Cursor getCursor(String sql) throws Exception {
            // SQLiteDatabase arg0 = SQLiteWork.this.getWritableDatabase();
            try {

                return database.rawQuery(sql, null);
            } catch (SQLException ex) {
                throw new Exception(ex.getMessage() + sql);
            }

        }

        /**
         * 获取数据版本号
         * @return
         */
        public  String getVer()
        {
            try {
                return execSQLString("select ver from sysinfo", null);
            } catch (Exception e) {
                return "";
            }
        }

        public   String initDatabase() throws Exception{
            String sql = "";
            String strError = "";

            try
            {
                sql = "Create table  if not exists brandinfo(businessid nvarchar(20),brandname nvarchar(30),serverccip nvarchar(20),servernetip nvarchar(20),port nvarchar(20),logurl nvarchar(32),softType nvarchar(20),username nvarchar(20),pwd nvarchar(20),IfRemember nvarchar(5 ),enterpriseid nvarchar(20))";
                execSQL(sql);

            }catch(Exception ex)
            {
                strError += "创建品牌资料表:" + ex.getMessage() + "\r\n";

            }

            // 创建新的扫描单据表
            try {
                sql = "Create table if not exists newscandate(goodsid nvarchar(20),modelm nvarchar(20),colors nvarchar(20),curcount nvarchar(5))";
                execSQL(sql);

            } catch (Exception ex) {
                strError += "创建扫描单据表:" + ex.getMessage() + "\r\n";
            }


            // 创建配货单（验收单）单据表
            try {
                sql = "Create table if not exists tpeino(peigoodlno nvarchar(30),saplno nvarchar(20),company_na nvarchar(20)," +
                        "stock_name nvarchar(20),company_id nvarchar(20),stock_id nvarchar(20))";
                execSQL(sql);

            } catch (Exception ex) {
                strError += "创建配货单（验收单）:" + ex.getMessage() + "\r\n";
            }
            // 创建有单单据的配货单明细表（Select_product_id_P）
            try {
                //goodsid 产品编号，goodsdescription产品描述，modelm型号，colors色号，noscanqty未扫描数量
                sql = "Create table if not exists newtpeinomx(goodsid nvarchar(20),goodsdescription nvarchar(30),modelm nvarchar(20)," +
                        "colors nvarchar(10),noscanqty nvarchar(5),amount nvarchar(10))";//增加一个要单号中要发的数量amount
                execSQL(sql);

            } catch (Exception ex) {
                strError += "有单入库的配货单明细表:" + ex.getMessage() + "\r\n";
            }
            // 创建品检表
            try {
                sql = "Create table if not exists newtpurcheck(purchecklno nvarchar(30),saplno nvarchar(20),supplier_na nvarchar(20)," +
                        "stock_name nvarchar(20),supplier_id nvarchar(20),stock_id nvarchar(20))";
                execSQL(sql);

            } catch (Exception ex) {
                strError += "创建品检单:" + ex.getMessage() + "\r\n";
            }

            // 创建功能菜单列表
            try {
                //创建第五版的功能菜单列表

                sql = "Create table if not exists menus (menucode nvarchar(8),menuname nvarchar(20)," +
                        " parentcode nvarchar(6),showstatus nvarchar(4),procedurename nvarchar(30))";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "创建功能列表:" + ex.getMessage() + "\r\n";
            }

            // 创建新代理商表if not exists
            try {
                sql = "Create table if not exists newcompany(agentid nvarchar(30),"
                        + "agentname nvarchar(10),"
                        + "link nvarchar(30)," + "tel nvarchar(30),"
                        + "corpaddr nvarchar(100),"
                        + "uprecndate datetime)";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "创建往来单位表:" + ex.getMessage() + "\r\n";
            }

            //创建新产品表
            try {
                sql = "Create table if not exists newproduct(goodsid nvarchar(20),"
                        + "goodsdescription nvarchar(20),"
                        + "brandname nvarchar(32)," + "modelm nvarchar(32),"
                        + "productyear nvarchar(32),"
                        + "colors nvarchar(12)," + "prodtype nvarchar(32),"
                        + "uprecndate datetime)";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "产品信息表创建失败:" + ex.getMessage() + "\r\n";
            }
            //创建新零售表
            try {

                sql = "Create table if not exists newretail(traderid nvarchar(20),"
                        + "tradername nvarchar(20),"
                        + "link nvarchar(32)," + "tel nvarchar(32),"
                        + "corpaddr nvarchar(12)," + "provicename nvarchar(32),"
                        + "cityname nvarchar(12)," + "agentid nvarchar(32),"
                        + "agentname nvarchar(12),"+ "uprecndate datetime)";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "零售店表创建失败:" + ex.getMessage() + "\r\n";
            }

//		try {
//			//创建分销店表
//			sql = "Create table if not exists distributor (distributorid nvarchar(20)," +
//					"distributorname nvarchar(20),link nvarchar(10),tel nvarchar(20),corpaddr nvarchar(30),traderid nvarchar(20),uprecndate datetime)";
//			execSQL(sql);
//		}catch (Exception ex){
//			strError += "分销店表创建失败:" + ex.getMessage() + "\r\n";
//		}

            try {
                //创建分销店表
                sql = "Create table if not exists storeinfor (storeid nvarchar(30)," +
                        "storename nvarchar(25),link nvarchar(10),tel nvarchar(15) ," +
                        "corpaddr nvarchar(30),traderid nvarchar(20),uprecndate datetime)";
                execSQL(sql);
            }catch (Exception ex){
                strError += "分销店表创建失败:" + ex.getMessage() + "\r\n";
            }

            try {
                //创建直营分销店表
                sql = "Create table if not exists agentinfor (storeid nvarchar(30)," +
                        "storename nvarchar(25),link nvarchar(10),tel nvarchar(15) ," +
                        "corpaddr nvarchar(30),traderid nvarchar(20),uprecndate datetime)";
                execSQL(sql);
            }catch (Exception ex){
                strError += "直营分销店表创建失败:" + ex.getMessage() + "\r\n";
            }

            //创建新直营表
            try {

                sql = "Create table if not exists newdirect(traderid nvarchar(20),"
                        + "tradername nvarchar(20),"
                        + "link nvarchar(32)," + "tel nvarchar(32),"
                        + "corpaddr nvarchar(32)," + "provicename nvarchar(32),"
                        + "cityname nvarchar(12)," + "agentid nvarchar(32),"
                        + "agentname nvarchar(12),"+ "uprecndate datetime)";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "直营店表创建失败:" + ex.getMessage() + "\r\n";
            }
            //创建调拨单表
            try {
                sql = "Create table if not exists allots(allotlno nvarchar(32),"
                        + "saplno nvarchar(32),"
                        + "outstockname nvarchar(32)," + "instockname nvarchar(32),"
                        + "noallotqty nvarchar(32)," + "outstockid nvarchar(32),"
                        + "instockid nvarchar(32))";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "调拨单表创建失败:" + ex.getMessage() + "\r\n";
            }

            try {

                sql = "Create table if not exists productbarcode(product_id nvarchar(20),"
                        + "barcode nvarchar(20),"
                        + "modelm nvarchar(32),"
                        + "colors nvarchar(12),"
                        + "product_name nvarchar(32),"
                        + "uprecndate datetime)";

                execSQL(sql);
            } catch (Exception ex) {
                strError += "创建商品条码表失败:" + ex.getMessage() + "\r\n";
            }

            // 创建用户表
            try {

                sql = "Create table if not exists suser(suser_id nvarchar(30),"
                        + "suser_pass nvarchar(10)," + "suser_name nvarchar(20),"
                        + "parentna nvarchar(20)," + "remark nvarchar(12),"
                        + "parentid nvarchar(30))";

                execSQL(sql);
            } catch (Exception ex) {
                strError += "用户表创建失败:" + ex.getMessage() + "\r\n";
            }

            // 创建新用户表
            try {

                sql = "Create table if not exists newuser(suser_id nvarchar(30),"
                        + "suser_pass nvarchar(10)," + "suser_name nvarchar(20),"
                        + "parentna nvarchar(20)," + "remark nvarchar(12),"
                        + "parentid nvarchar(30))";

                execSQL(sql);
            } catch (Exception ex) {
                strError += "用户表创建失败:" + ex.getMessage() + "\r\n";
            }



            // 创建套餐选择表
            try {

                sql = "Create table if not exists packmealset(PackId nvarchar(8),PackName nvarchar(36))";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "套餐选择表创建失败:" + ex.getMessage() + "\r\n";
            }

            // 创建套餐扫描明细表
            try {

                sql = "Create table if not exists packdressboxscan(goodsid nvarchar(20),brandname nvarchar(20),goodstype nvarchar(20)," +
                        "seriesname nvarchar(20),modelm nvarchar(20),colors nvarchar(20),packnum nvarchar(5),scannum nvarchar(5),Barcode nvarchar(20))";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "创建套餐扫描明细表创建失败:" + ex.getMessage() + "\r\n";
            }





            // 创建新仓库表
            try {

                sql = "Create table if not exists newstock(stock_id nvarchar(8),stock_name nvarchar(36),uprecndate datetime)";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "仓库资料表创建失败:" + ex.getMessage() + "\r\n";
            }

            // 创建新供应商资料表
            try {
                sql = "Create table if not exists newsupplier(supplier_id nvarchar(20),supplier_name nvarchar(60),uprecndate nvarchar(32))";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "供应商表创建失败:" + ex.getMessage() + "\r\n";
            }

            //创建产品换标扫描表//oldcodetype为0或1，0物流码，1防伪码
            try {
                sql = "Create table if not exists newchangecode(newcode nvarchar(20),oldcode nvarchar(20),oldcodetype nvarchar(4))";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "产品换标扫描表创建失败:" + ex.getMessage() + "\r\n";
            }

            //装盒入库扫描表：产品id,型号，色号，条码，扫描时间，临时盒标，正式盒标
            try {
                sql = "create table if not exists packingscan (product_id nvarchar(20)," +
                        "modelm nvarchar(20),colors nvarchar(20),barcode nvarchar(20)," +
                        "tempboxcode nvarchar(20),boxcode nvarchar(20),scantime datetime)";
                execSQL(sql);
            } catch (Exception ex) {
                strError += "装盒入库扫描表创建失败:" + ex.getMessage() + "\r\n";
            }

            return strError;
        }

        /**
         * 插入数据 String sql = "insert into person(name,address,sex)values(?,?,?)";
         * new Object[]{"","",""}
         * */

        public   boolean InsertDb(String sql, Object[] params) {

            boolean flag = false;
            try {
                database.execSQL(sql, params);
                flag = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            // finally
            // {
            // if(database != null)
            // {
            // database.close();
            // }
            // }
            return flag;
        }

        /**
         * 插入数据 InsertDb("person", null, value);
         *
         * ContentValues value = new ContentValues(); value.put("name", "jack");
         * value.put("address", "英国"); value.put("sex", "男");
         * */
        public   boolean InsertDb(String table, String nullColumnHack,
                                  ContentValues value) {

            long flag = -1;
            try {
                flag = database.insert(table, null, value);

                return (flag != -1 ? true : false);

            } catch (Exception e) {

            }
            return false;
        }

        @Override
        public void onCreate(SQLiteDatabase arg1)// SQLiteDatabase arg0
        {
            database = arg1;

            try {
                initDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {

            // 添加品牌
            database = db;

            if (currentVersion <= oldVersion) {
                return;
            }

            db.beginTransaction();
            try {
                // db.execSQL("ALTER TABLE company ADD COLUMN brand nvarchar(30)");
                db.execSQL("ALTER TABLE newtpeinomx ADD COLUMN amount nvarchar(10)");//增加一个单号数量字段

                initDatabase();

                database.setTransactionSuccessful();
            } catch (Throwable ex) {

            } finally {
                database.endTransaction();

            }

        }


        /**
         * 查询返回一个list
         * */
        public   List<Map<String, String>> query(boolean distinct, String table,
                                                 String[] columns, String selection, String[] selectionArgs,
                                                 String groupBy, String having, String orderBy, String limit) {

            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            Map<String, String> map = new HashMap<String, String>();

            SQLiteDatabase database = null;

            Cursor cursor = null;

            try {
                // String sql = "select * from  person where id =?";

                cursor = database.query(distinct, table, columns, selection,
                        selectionArgs, groupBy, having, orderBy, limit);

                int colums = cursor.getColumnCount();

                while (cursor.moveToNext()) {
                    for (int i = 0; i < colums; i++) {
                        String cols_name = cursor.getColumnName(i);

                        String cols_value = cursor.getString(cursor
                                .getColumnIndex(cols_name));

                        if (cols_value == null) {
                            cols_value = "";
                        }

                        map.put(cols_name, cols_value);
                    }

                    list.add(map);
                }

            } catch (Exception e) {

            } finally {
                if (null != cursor) {
                    cursor.close();
                }
            }

            return list;
        }

        /**
         * 查询多条记录 String sql = "select * from  person";
         * */

        public   List<Map<String, Object>> QueryDbList(String sql,
                                                       String[] selectionArgs) {

            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(sql, selectionArgs);

                int colums = cursor.getColumnCount();
                while (cursor.moveToNext()) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    for (int i = 0; i < colums; i++) {
                        String cols_name = cursor.getColumnName(i);

                        String cols_value = cursor.getString(cursor
                                .getColumnIndex(cols_name));

                        if (cols_value == null)
                        {
                            cols_value = "";
                        }
                        map.put(cols_name, cols_value);
                    }

                    list.add(map);

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
            return list;
        }

        /**
         * @param table
         *            数据表名
         * @param selection
         *            列名
         * @param selectionArgs
         *            条件
         * */
        public   List<Map<String, String>> QueryDbList(String table,
                                                       String[] columns, String selection, String[] selectionArgs) {

            List<Map<String, String>> list = new ArrayList<Map<String, String>>();

            Cursor cursor = null;
            try {
                cursor = database.query(false, table, columns, selection,
                        selectionArgs, null, null, null, null);

                int colums = cursor.getColumnCount();

                while (cursor.moveToNext()) {
                    Map<String, String> map = new HashMap<String, String>();

                    for (int i = 0; i < colums; i++) {
                        String cols_name = cursor.getColumnName(i);

                        String cols_value = cursor.getString(cursor
                                .getColumnIndex(cols_name));

                        if (cols_value == null) {
                            cols_value = "";
                        }

                        map.put(cols_name, cols_value);
                    }

                    list.add(map);
                }

            } catch (Exception e) {

                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
            return list;
        }

        /**
         * 查询单条记录
         *
         * */
        public   Map<String, String> QueryDbMap(String table, String selection,
                                                String[] selectionArgs) {

            Map<String, String> map = new HashMap<String, String>();

            Cursor cursor = null;
            try {

                cursor = database.query(true, table, null, selection,
                        selectionArgs, null, null, null, "1");

                int colums = cursor.getColumnCount();

                while (cursor.moveToNext()) {
                    for (int i = 0; i < colums; i++) {
                        String cols_name = cursor.getColumnName(i);

                        String cols_value = cursor.getString(cursor
                                .getColumnIndex(cols_name));

                        if (cols_value == null) {
                            cols_value = "";
                        }

                        map.put(cols_name, cols_value);

                    }

                }

            } catch (Exception e) {

                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            return map;
        }

        /**
         *
         * 查询数据 String sql = "select * from  person where id =?";
         *
         *
         * */
        public   Map<String, String> QueryDbMap(String sql, String[] selectionArgs) {

            Map<String, String> map = new HashMap<String, String>();
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(sql, selectionArgs);
                int colums = cursor.getColumnCount();
                while (cursor.moveToNext()) {
                    for (int i = 0; i < colums; i++) {
                        String cols_name = cursor.getColumnName(i);

                        String cols_value = cursor.getString(cursor
                                .getColumnIndex(cols_name));

                        if (cols_value == null) {
                            cols_value = "";
                        }
                        map.put(cols_name, cols_value);
                    }
                }
            } catch (Exception e) {

                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }

            }
            return map;
        }



        /**
         *
         * 查询数据 String sql = "select * from  person where id =?";
         *
         *
         * */
        public   boolean IsExist (String sql, String[] selectionArgs) {

            Map<String, String> map = new HashMap<String, String>();
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(sql, selectionArgs);
                return cursor.getCount() > 0 ;

            } catch (Exception e)
            {

                e.printStackTrace();
            } finally
            {
                if (cursor != null) {
                    cursor.close();
                }

            }
            return false;
        }



        /**
         * 更新数据
         *
         * ContentValues value = new ContentValues(); value.put("name", "jack");
         * value.put("address", "英国"); value.put("sex", "男");
         *
         * whereClause "id = ?" whereArgs new String[]{"4"}
         *
         * 返回影响的数据条数
         * */
        public int UpdateDb(String table, ContentValues values, String whereClause,
                            String[] whereArgs) {

            int flag = -1;
            try {
                flag = database.update(table, values, whereClause, whereArgs);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return flag;
        }

        /**
         * 更新数据 String sql =
         * "update person set name = ?,address = ?,sex = ? where id = ?";
         *
         * */
        public   boolean UpdateDb(String sql, Object[] params) {

            boolean flag = false;
            try {
                open();

                database.execSQL(sql, params);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return flag;
        }


        public   boolean UpdateDb(String sql,List<Object[] > list) throws Exception
        {
            for (Object[] objects : list) {

                if(!UpdateDb(sql,objects))
                {
                    return false;
                }

            }

            return true;
        }

        /**
         * 判断某张表是否存在
         * @param tabName 表名
         * @return
         */
        public boolean tabIsExist(String tabName){
            boolean result = false;
            if(tabName == null){
                return false;
            }
            Cursor cursor = null;
            try {
                String sql = "select count(*) as c from sqlite_master where type ='table' and name ='+tabName.trim()+' ";
                cursor = database.rawQuery(sql, null);
                if(cursor.moveToNext())
                {
                    int count = cursor.getInt(0);
                    if(count>0){
                        result = true;
                    }
                }
            } catch (Exception e) {
//			Log.e("main", "tabIsExist:"+e.getMessage());
            }
            return result;
        }

    }

