package com.holyes.ccssend5.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @ClassName: ImageUtils
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:01
 */
public class ImageUtils {
    /**
     * convert Bitmap to byte array
     *
     * @param b
     * @return
     */
    public static byte[] bitmapToByte(Bitmap b) {
        if (b == null) {
            return null;
        }

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }

    /**
     * convert byte array to Bitmap
     *
     * @param b
     * @return
     */
    public static Bitmap byteToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    /**
     * convert Drawable to Bitmap
     *
     * @param d
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable d) {
        return d == null ? null : ((BitmapDrawable)d).getBitmap();
    }


    /**
     * 下载远程的图片
     *
     * 只能在线程下执行（因为访问网络）
     * */
    public static Bitmap UrlToBitmap(String ur) throws Exception
    {
        Bitmap bm = null;


//    	HttpGet httpRequest = new HttpGet(ur);
//
//
//		//取得HttpClient 对象
//		       HttpClient httpclient = new DefaultHttpClient();
//		        try {
//		           //请求httpClient ，取得HttpRestponse
//		            HttpResponse httpResponse = httpclient.execute(httpRequest);
//		            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
//		                //取得相关信息 取得HttpEntiy
//		                HttpEntity httpEntity = httpResponse.getEntity();
//		               //获得一个输入流
//		                InputStream is = httpEntity.getContent();
//		                 bm = BitmapFactory.decodeStream(is);
//		                is.close();
//		            }
//
//		        } catch (ClientProtocolException e) {
//
//		            e.printStackTrace();
//		        } catch (IOException e) {
//
//		            e.printStackTrace();
//		        }
//
//		        return bm;
        URL url = null;
        try {
            url = new URL(ur);



            HttpURLConnection conn = null;

            conn = (HttpURLConnection)url.openConnection();

            conn.setConnectTimeout(5000);

            conn.connect();

            InputStream is = null;

            is = conn.getInputStream();

            Options opts = new Options();

            opts.inJustDecodeBounds = false;

            bm = BitmapFactory.decodeStream( is,null, opts);

            is.close();

        } catch (MalformedURLException e) {

            throw	new Exception("URL不正确 ："+ur);
        }
        catch (IOException e) {
            throw	new Exception("文件读写异常");
        }


        return bm;
    }



    /**
     * convert Bitmap to Drawable
     *
     * @param b
     * @return
     */
    public static Drawable bitmapToDrawable(Bitmap b) {
        return b == null ? null : new BitmapDrawable(b);
    }

    /**
     * convert Drawable to byte array
     *
     * @param d
     * @return
     */
    public static byte[] drawableToByte(Drawable d) {
        return bitmapToByte(drawableToBitmap(d));
    }

    /**
     * convert byte array to Drawable
     *
     * @param b
     * @return
     */
    public static Drawable byteToDrawable(byte[] b) {
        return bitmapToDrawable(byteToBitmap(b));
    }

    /**
     * get input stream from network by imageurl, you need to close inputStream yourself
     *
     * @param imageUrl
     * @param readTimeOutMillis
     * @return
     * @see ImageUtils getInputStreamFromUrl(String, int, boolean)
     */
    public static InputStream getInputStreamFromUrl(String imageUrl, int readTimeOutMillis) {
        return getInputStreamFromUrl(imageUrl, readTimeOutMillis, null);
    }

    /**
     * get input stream from network by imageurl, you need to close inputStream yourself
     *
     * @param imageUrl
     * @param readTimeOutMillis read time out, if less than 0, not set, in mills
     * @param requestProperties http request properties
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static InputStream getInputStreamFromUrl(String imageUrl, int readTimeOutMillis,
                                                    Map<String, String> requestProperties) {
        InputStream stream = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            HttpUtils.setURLConnection(requestProperties, con);
            if (readTimeOutMillis > 0) {
                con.setReadTimeout(readTimeOutMillis);
            }
            stream = con.getInputStream();
        } catch (MalformedURLException e) {
            closeInputStream(stream);
            throw new RuntimeException("MalformedURLException occurred. ", e);
        } catch (IOException e) {
            closeInputStream(stream);
            throw new RuntimeException("IOException occurred. ", e);
        }
        return stream;
    }

    /**
     * get drawable by imageUrl
     *
     * @param imageUrl
     * @param readTimeOutMillis
     * @return
     * @see ImageUtils getDrawableFromUrl(String, int, boolean)
     */
    public static Drawable getDrawableFromUrl(String imageUrl, int readTimeOutMillis) {
        return getDrawableFromUrl(imageUrl, readTimeOutMillis, null);
    }

    /**
     * get drawable by imageUrl
     *
     * @param imageUrl
     * @param readTimeOutMillis read time out, if less than 0, not set, in mills
     * @param requestProperties http request properties
     * @return
     */
    public static Drawable getDrawableFromUrl(String imageUrl, int readTimeOutMillis,
                                              Map<String, String> requestProperties) {
        InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOutMillis, requestProperties);
        Drawable d = Drawable.createFromStream(stream, "src");
        closeInputStream(stream);
        return d;
    }

    /**
     * get Bitmap by imageUrl
     *
     * @param imageUrl
     * @param readTimeOut
     * @return
     * @see ImageUtils getBitmapFromUrl(String, int, boolean)
     */
    public static Bitmap getBitmapFromUrl(String imageUrl, int readTimeOut) {
        return getBitmapFromUrl(imageUrl, readTimeOut, null);
    }

    /**
     * get Bitmap by imageUrl
     *
     * @param imageUrl
     * @param requestProperties http request properties
     * @return
     */
    public static Bitmap getBitmapFromUrl(String imageUrl, int readTimeOut, Map<String, String> requestProperties) {
        InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOut, requestProperties);
        Bitmap b = BitmapFactory.decodeStream(stream);
        closeInputStream(stream);
        return b;
    }

    /**
     * scale image
     *
     * @param org
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
        return scaleImage(org, (float)newWidth / org.getWidth(), (float)newHeight / org.getHeight());
    }

    /**
     * scale image
     *
     * @param org
     * @param scaleWidth sacle of width
     * @param scaleHeight scale of height
     * @return
     */
    public static Bitmap scaleImage(Bitmap org, float scaleWidth, float scaleHeight) {
        if (org == null) {
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(), matrix, true);
    }

    /**
     * close inputStream
     *
     * @param s
     */
    private static void closeInputStream(InputStream s) {
        if (s == null) {
            return;
        }

        try {
            s.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        }
    }




    /**
     *
     *保存Bitmap数据到指定路径
     *默认保存图片类型是.PNG
     *@param filePath 保存的图片路径
     *@param bm 要保存的图片数据
     *
     * */
    public static boolean SaveBitmapToFile(String filePath,Bitmap bm) throws FileNotFoundException
    {

        if(bm == null)
        {
            return false;
        }

        if(StringUtils.isEmpty(filePath))
        {
            return false;
        }

        File file = new File(filePath);

        FileUtils.makeDirs(file.getAbsolutePath());

        FileOutputStream fos = new FileOutputStream (filePath);
//    	 Runtime.getRuntime().exec("chmod 777 " + file1);

        bm.compress(Bitmap.CompressFormat.PNG, 100, fos);

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}

