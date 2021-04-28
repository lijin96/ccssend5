package com.holyes.ccssend5.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.holyes.ccssend5.lib.SqliteDataHelper;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: SharePrefenceUtils
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:02
 */
public class SharePrefenceUtils {

        public static int MODE = Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE;

        public static void saveString(Context context,String key,String value)
        {
            SharedPreferences preferences = context.getSharedPreferences("ccs4",MODE );
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.commit();
        }

        public static String getString(Context context,String key)
        {
            SharedPreferences preferences = context.getSharedPreferences("ccs4",MODE);
            return preferences.getString(key, "");
        }

        public static String getCCS4String(Context context,String key)
        {
            String brandInfos = "";
            try {
                Context otherAppsContext = context.createPackageContext("com.holyes.ccs_dev8", Context.CONTEXT_IGNORE_SECURITY);
                SharedPreferences sharedPreferences = otherAppsContext.getSharedPreferences("ccs4", MODE);
                brandInfos = sharedPreferences.getString(key, "");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return brandInfos;
        }


        public static void saveBrandInfos(Context context,String brandInfos)
        {
            saveString(context, "brandInfos", brandInfos);
        }

        public static String getBrandInfos(Context context)
        {
            //获取第四代的品牌信息
            return getCCS4String(context, "brandInfos");
        }

        public static void copyCcs4Brands(Context context,String brandInfosStr)
        {
            List<String> sqlList = new ArrayList<String>();
            //00,HOLYES,test.4006889521.cn,9521,52,00,003501,a12345678,true
            String[] brandsArray = brandInfosStr.split(";;;");
            String[] brandInfosArray;
            String businessid,brandname,serverccip,servernetip,port,logurl,
                    softtype,username,pwd,IfRemember,enterpriseid,sql="";
            for(String brandInfos:brandsArray)
            {
                brandInfosArray = brandInfos.split(",");
                businessid = brandInfosArray[0];
                //如果该品牌已经存在就不重新添加了。
                if(isExistBrand(context, businessid))
                {
                    continue;
                }

                brandname = brandInfosArray[1];
                serverccip = brandInfosArray[2];
                servernetip = brandInfosArray[2];//同serverccip
                port = brandInfosArray[3];
                softtype = brandInfosArray[4];
                logurl = "";//因为第四代没有保存有图片url，所以这里空
                username = brandInfosArray[6];
                pwd = brandInfosArray[7];
                IfRemember = brandInfosArray[8];
                enterpriseid = brandInfosArray[5];
                sql= String.format("insert into brandinfo(businessid,brandname,serverccip,servernetip,port," +
                                "logurl,softtype,username,pwd,IfRemember,enterpriseid)" +
                                "values('%1$s','%2$s','%3$s','%4$s','%5$s','%6$s','%7$s','%8$s','%9$s','%10$s','%11$s')",
                        businessid,brandname,serverccip,servernetip,port,logurl,softtype,username,pwd,IfRemember,enterpriseid);
                sqlList.add(sql);

//			//复制品牌图标
                createIconByBitmap(businessid,brandInfosArray[9]);

            }

            if(sqlList.size()>0)
            {
                SqliteDataHelper.getHelper(context).BatchOperation(sqlList);
            }

        }

        /**
         * 某新品是否已经添加有了，有了就不再copy添加
         * @param context
         * @param businessid 品牌代号
         * @return
         */
        public static boolean isExistBrand(Context context,String businessid)
        {
            try {
                if(SqliteDataHelper.getHelper(context).execSQLInt("select count(*) from brandinfo where businessid = '"+businessid+"'") > 0)
                {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }


        public static void getAndCopyCcs4Brand(Context context)
        {
            //获取copy的品牌信息
            String brandInfos = getBrandInfos(context);
            if(brandInfos.isEmpty())
            {
                return;
            }
            //复制品牌信息和图标
            copyCcs4Brands(context,brandInfos);
            //卸载第四代的
            SomeUtils.uninstallApp(context, "com.holyes.ccs_dev8");
        }

        public static Bitmap stringToBitmap(String string)
        {
            //将字符串转换成Bitmap类型
            Bitmap bitmap=null;
            try {
                byte[]bitmapArray;
                bitmapArray=Base64.decode(string, Base64.DEFAULT);
                bitmap= BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        public static void createIconByBitmap(String businessid,String bitmapStr)
        {
            if(bitmapStr.isEmpty())
            {
                return;
            }
            Bitmap bitmap = stringToBitmap(bitmapStr);
            String  savePath = "/data/data/com.holyes.ccsdevs5/";
            String Image_icon =savePath + businessid  + ".png";
            try {
                ImageUtils.SaveBitmapToFile(Image_icon,bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

//	public static void downloadBrandIcon(final String businessid)
//	{
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					String imageUrl = "http://www.4006889521.cn/softupdate/Android/Dev/image/p"+businessid+".png";
//					Bitmap bitmap = ImageUtils.UrlToBitmap(imageUrl);
//					String  savePath = "/data/data/com.holyes.ccsdevs5/";
//					String Image_icon =savePath + businessid  + ".png";
//					ImageUtils.SaveBitmapToFile(Image_icon, bitmap);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}).start();
//	}
    }

