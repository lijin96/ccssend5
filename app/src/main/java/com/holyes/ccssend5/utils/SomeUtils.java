package com.holyes.ccssend5.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ccssend5.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.holyes.ccssend5.lib.ShowMessage;
import com.holyes.ccssend5.lib.SqliteDataHelper;
import com.holyes.ccssend5.lib.SysUserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 * @ClassName: SomeUtils
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/10 10:02
 */
public class SomeUtils {
    /**
     * @author hzm
     * 将传入的json字符串按类模板解析成对象
     * @param json 需要解析的json字符串
     * @param cls 类模板
     * @return 解析好的对象
     */
    public static <T> T getObj(String json,Class<T> cls){
        Gson gson = new Gson();
        T bean = (T) gson.fromJson(json, cls);
        return bean;
    }
    /**
     * 将传入的对象解析成json字符串
     * @param bean 需要解析的对象
     * @return 解析完成的json字符串
     */
    public static <T> String getJsonString(T bean){
        Gson gson = new Gson();
        String json = gson.toJson(bean, bean.getClass());
        return json;
    }

    /**
     * 截取json字符串，
     * 把{"KID":"0123456789","PItem":[{"PID":"HL9521001","NUM":"2"},{"PID":"HL9521002","NUM":"3"}]}
     * 截取成[{"PID":"HL9521001","NUM":"2"},{"PID":"HL9521002","NUM":"3"}]
     * 以备getObjList方法使用
     *
     */
    public static String getLastJsonString(String jsonData){
        String lastString =jsonData.substring(jsonData.indexOf("["),jsonData.indexOf("]")+1);
        return lastString;
    }
    /**
     * 将获取到的json字符串转换为对象集合进行返回
     * @param jsonData 需要解析的json字符串
     * @param cls 类模板
     * @return
     */
    public static <T> List<T> getObjList(String jsonData, Class<T> cls){
        List<T> list = new ArrayList<T>();
        if (jsonData.startsWith("[") && jsonData.endsWith("]")) {//当字符串以“[”开始，以“]”结束时，表示该字符串解析出来为集合
            //截取字符串，去除中括号
            jsonData = jsonData.substring(1, jsonData.length() -1);
            //将字符串以"},"分解成数组
            String[] strArr = jsonData.split("\\},");
            //分解后的字符串数组的长度
            int strArrLength = strArr.length;
            //遍历数组，进行解析，将字符串解析成对象
            for (int i = 0; i < strArrLength; i++) {
                String newJsonString = null;
                if (i == strArrLength -1) {
                    newJsonString = strArr[i];
                } else {
                    newJsonString = strArr[i] + "}";
                }
                T bean = getObj(newJsonString, cls);
                list.add(bean);
            }
        }
        if (list == null || list.size() == 0) {
            return null;
        }
        return list;
    }

    /**
     * 通过产品id来取得型号色号
     * @param context
     * @param product_id 产品id
     * @return
     */
    public static String getModelColors(Context context,String product_id){
        String str="型号,色号";
        List<Map<String, Object>>  list= SqliteDataHelper.getHelper(context).QueryDbList(String.format("select modelm,colors from product where  product_id = '%1$s'" ,product_id),null);
        if(list.size()>0){
            str=list.get(0).get("modelm")+","+list.get(0).get("colors");
        }
        return str;
    }

    /**
     * 弹出吐司
     * @param context
     * @param str 土司的内容
     */
    public static void showToask(Context context,String str)
    {

        Toast toast =  Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP , 0, 300);
        toast.show();
    }

    /**
     * 把EditText的光标移到末尾
     * @param editText
     */
    public static void moveFocus(EditText editText)
    {
        Editable editStr=editText.getText();
        Spannable spannable=editStr;
        Selection.setSelection(spannable, editStr.length());
    }

    /**
     * 判断是否连续点击了两次
     * @param context 上下文
     * @param isToask 是否弹出“再按一次退出”的土司
     * @return true:是双击；false：不是双击
     */
    public static boolean isTwoClick=false;
    public static boolean isDoubleClick(Context context,boolean isToask) {
        Timer tExit = null;
        if (!isTwoClick) {
            isTwoClick = true; // 准备退出
            if(isToask)
            {
                ShowMessage.Show(context, "再按一次退出");
            }
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isTwoClick = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
            return false;
        } else {
            return true;
        }
    }

    /**
     * 函数名称:hidePanMenu
     * 功能描述:右侧菜单显示、隐藏
     * panMenu:右侧菜单
     * panMain：主界面
     */
    public static void hidePanMenu(LinearLayout panMenu, LinearLayout panMain)
    {
        // 固定 main layout, 防止被左、右挤压变形
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) panMenu.getLayoutParams();
        RelativeLayout.LayoutParams lpMain = (RelativeLayout.LayoutParams) panMain.getLayoutParams();

        if(panMenu.getVisibility()== View.GONE)
        {
            //lpMain.width = dm.widthPixels;
            lpMain.leftMargin = -lp.width;
            lpMain.rightMargin = lp.width;
            panMain.setLayoutParams(lpMain);
            panMenu.setVisibility(View.VISIBLE);
        }
        else
        {
            //lpMain.width = dm.widthPixels;
            lpMain.leftMargin = 0;
            lpMain.rightMargin = 0;
            panMain.setLayoutParams(lpMain);
            panMenu.setVisibility(View.GONE);
        }
    }

    /**
     * 判定是否需要隐藏
     * @param v
     * @param ev
     * @return
     */
    public static boolean isNeedHideInput(View v, MotionEvent ev)
    {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top
                    && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
    /** 隐藏软键盘
     * @param token
     * @param context
     */
    public static void HideSoftInput(IBinder token, Context context)
    {
        if (token != null)
        {
            InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 先调用isNeedHideInput()判断是否需要隐藏，需要则调用HideSoftInput()隐藏键盘
     * @param context
     * @param ev
     */



    public static void isNeedHideAndDo(Context context,MotionEvent ev)
    {
        if(ev.getAction()==MotionEvent.ACTION_DOWN)
        {
            View view = ((Activity)context).getCurrentFocus();
            if(isNeedHideInput(view, ev))
            {
                HideSoftInput(view.getWindowToken(), context);
            }
        }

    }



    /**
     * 把List<Map<String, Object>>转换成String
     * @param mlist
     * @return
     */
    public String mapListToString(List<Map<String, Object>> mlist)
    {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject;
        for(Map<String, Object> map:mlist)
        {
            jsonObject = new JsonObject();
            Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, Object> entry = iterator.next();
                jsonObject.addProperty(entry.getKey(), entry.getValue().toString());
            }
            jsonArray.add(jsonObject);
        }

        return jsonArray.toString();
    }

    /**
     * 把String（json格式的）转换成List<Map<String, Object>>
     * @param
     * @return
     */
    public List<Map<String, Object>> stringToMapList(String jsonStr)
    {
        List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
        Map<String, Object> map;
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonStr);
            for(int i=0;i<jsonArray.length();i++)
            {
                map = new HashMap<String, Object>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONArray keys = jsonObject.names();
                if(keys!=null)
                {
                    for(int j=0;j<keys.length();j++)
                    {
                        String key = keys.getString(j);
                        String value = jsonObject.getString(key);
                        map.put(key, value);
                    }

                }
                dataList.add(map);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    /**
     * 把List<Map<String,String>>集合转换成String[]数组
     * @param list
     * @param key 集合里面的map中需要转换成数组的key字段的值
     * @return
     */
    public static String[] mapListToStringArray(List<Map<String,Object>> list,String key)
    {
        String array[] = new String[list.size()];
        for(int i=0;i<array.length;i++)
        {
            array[i] = list.get(i).get(key).toString();
        }
        return array;
    }

    /**
     * 把RelativeLayout从小变到大
     * @param context 上下文
     * @param relativeLayout 要改变大小的RelativeLayout
     * @param txtTip RFID扫描界面里面的错误提示信息文本
     * @param tv_big_txtTip 大布局里面的文本
     */
    public static boolean isSmallToBig = true;//是否是由小变大，控制只能是一种变化，不能同时变大又变小，
    //只能是一种变完再到另一种,true:可小变大；false:可大变小
    public static void smallToBig(Context context, final RelativeLayout relativeLayout,
                                  final TextView txtTip, final TextView tv_big_txtTip)
    {
        if(!isDoubleClick(context,false)||!isSmallToBig)
        {
            return;
        }
        Runnable runnable =new Runnable() {

            @Override
            public void run() {
                if(txtTip.getText().toString().equals("提示：（此处提示双击可变大）"))
                {
                    tv_big_txtTip.setText("双击隐藏");
                }else{
                    tv_big_txtTip.setText(txtTip.getText().toString());
                }
                txtTip.setEnabled(true);
                isSmallToBig = false;
            }
        };
        Handler handler = new Handler(context.getMainLooper());
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.small_to_big);
        relativeLayout.startAnimation(animation);
        relativeLayout.setVisibility(View.VISIBLE);
        txtTip.setEnabled(false);//点击后直到变化完否则不能继续开始同样的变化
        handler.postDelayed(runnable, 1000);
    }

    /**
     * 把 RelativeLayout从大变小到无
     * @param context 上下文
     * @param relativeLayout 要改变大小的relativeLayout
     */
    public static void bigToSmall(Context context,final RelativeLayout relativeLayout)
    {
        if(!isDoubleClick(context,false)||isSmallToBig)
        {
            return;
        }
        Runnable runnable =new Runnable() {

            @Override
            public void run() {
                relativeLayout.setEnabled(true);
                isSmallToBig = true;
            }
        };
        Handler handler = new Handler(context.getMainLooper());
        relativeLayout.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.big_to_small);
        relativeLayout.startAnimation(animation);
        relativeLayout.setEnabled(false);//点击后直到变化完否则不能继续开始同样的变化
        handler.postDelayed(runnable, 1600);
    }

    /**
     * 设置isSmallToBig变量的值，这个目前只在activity退出时调用，
     * 防止view变大了没变小就退出界面，变量值没有变回true,下次打开时不能从小变到大
     * @param isSmallToBig
     */
    public static void setSmallToBig(boolean isSmallToBig)
    {
        SomeUtils.isSmallToBig = isSmallToBig;
    }

    /**
     * 根据客户代号获取客户名称
     * @version 创建时间：2017-5-6 下午3:22:49
     * @param context
     * @param companyId
     * @return
     */
    public static String getCompanyNameById(Context context,String companyId) throws Exception
    {
        String sql = "select company_na from company where company_id = '"+companyId+"'";
        String companyName = SqliteDataHelper.getHelper(context).execSQLString(sql);
        return companyName;
    }

    /**
     * 扫描的产品是否在单据明细中
     * @param list 单据产品明细集合
     * @param textView 显示产品型号色号的textView
     * @return true 在单据明细中，false 不在
     */
    public static boolean isInBillProduct(List<Map<String, Object>> list,TextView textView)
    {
        String modelmAndColors = "",scanModel = textView.getText().toString().trim();

        for(Map<String, Object> map:list)
        {
            modelmAndColors = map.get("modelm")+"-"+map.get("colors");
            if(scanModel.equals(modelmAndColors))
            {
                return true;
            }
        }

        return false;

    }

    public static void goToSetting(Context context)
    {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings","com.android.settings.Settings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        context.startActivity(intent);
    }

    /**
     * 获取系统版本信息
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-11-28 下午3:09:14
     * @return
     */
    public static String getSoftVer(Context context)
    {
        try {
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName ;//+ "." + packInfo.versionCode
        } catch (Exception e) {
            return "错误版本信息";
        }
    }

    /**
     * 是否是从服务器返回的错误，true表示不是，false表示是从服务返回的，要不是从服务返回的就要对条码加*；
     * @param eMessage
     * @return
     */
    public static String  isNotFromServiceError(String eMessage)
    {
        String mStar = "";
        if(eMessage.contains("服务器："))
        {
            mStar = "*";
        }
        return mStar;
    }

    /**
     * 相当于按下返回键
     */
    public static void clickKeyBack()
    {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static String getListBarcode(List<String> list)
    {
        Gson gson=new Gson();
        return gson.toJson(list).toString();
    }

    /**
     * 读取模本文本，返回字符串
     * @param context
     * @return
     */
    public static String readFileString(Context context,int resourceId)
    {
        InputStream is = context.getResources().openRawResource(resourceId);//把文件转换为输入流
        StringBuffer response = new StringBuffer();                //创建StringBuffer实例
        //		BufferedReader br = new BufferedReader(new InputStreamReader(is));  //根据is创建缓冲字符输入流
        BufferedReader br = null ;
        try {
            InputStreamReader isr;
            isr = new InputStreamReader(is, "GBK");
            br = new BufferedReader(isr);

            String s = null;                            //创建s变量
            while ((s = br.readLine()) != null)
            {       //把这一行的值赋值给变量s，并判断是否有值
                response.append(s);                        //把值添加进StringBuffer
                response.append("\r\n");                 //再添加一个换行符
            }
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (IOException e) {          //catch异常处理
            e.printStackTrace();       //得到错误的实例， 调用方法在命令行打印程序出错的位置及原因
        } finally {              //finally try语句大多数情况下都会执行的代码块
            try {
                if (is != null) {      //如果文件输入流不为空
                    is.close();            //调用close函数关掉输入流
                }
                if (br != null) {      //如果缓冲字符输入流不为空
                    br.close();            //调用close函数关掉缓冲字符输入流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response.toString();
    }

    /**
     * 卸载app
     * @param context
     * @param packageName 要卸载的app的包名，第四代的是com.holyes.ccs_dev8
     */
    public static void uninstallApp(Context context,String packageName)
    {
        Uri packageURI = Uri.parse("package:"+packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
    }

    /**
     * 是否全数字,A开头的是盒标，不用判断这个18-03-19-1500
     * @param str
     * @return
     */
    public static boolean isAllNumber(Context context,String str)
    {
        //如果扫码的码是十三位，会先出现一个空的回车值

        if(str.startsWith("A"))
        {
            return true;
        }else if(str.startsWith("P"))
        {
            return true;
        }

//		else if (str.length()<16) {
//			return false;
//		}
        else {
            Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
            return pattern.matcher(str).matches();
        }
    }

    /**
     * 截取带网址的条码
     * */
    public static String InterceptCode(Context context,String str){
        String code ="";


//		String str1=str.substring(0, str.lastIndexOf("="));
        if (str.indexOf("=") != -1){
            code=str.substring(str.lastIndexOf("=")+1, str.length());
        }else if (str.indexOf("＝") != -1){
            code=str.substring(str.lastIndexOf("＝")+1, str.length());
        }else {
            code=str.substring(str.length()-16);
        }


        if (str2HexStr(code.substring(0,1)).equals("3F")){
            SysUserInfo sysUserInfo=new SysUserInfo(context);
            if (sysUserInfo.getEnterpriseId().equals("10")) {
                String newcode = code.substring(1);
                code = "1" + newcode;
            }
        }

//		String code=str.substring(str.length()-16,str.length());
        return code;
    }

        //普通条码截取
        public static String UpdatefirstString(Context context,String str){
            String code =str;
            SysUserInfo sysUserInfo=new SysUserInfo(context);
            if (sysUserInfo.getEnterpriseId().equals("10")) {
            if(!str.startsWith("A")){
                String firstcode = code.substring(1);
                code = "1" + firstcode;
            }
        }else if (sysUserInfo.getEnterpriseId().equals("83")){
            /**
             * 舒达迈的条码12位并且包含空格
             * */
            if (code.indexOf(" ") != -1) {
                code = AgentCode(context, code);
            }
        }
//		String code=str.substring(str.length()-16,str.length());
        return code;
    }

    /**
     * 字符串转换成为16进制(无需Unicode编码)
     * @param str 待转换的ASCII字符串
     * @author xxs
     * @return byte字符串 （每个Byte之间空格分隔）
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();//toCharArray() 方法将字符串转换为字符数组。
        StringBuilder sb = new StringBuilder(""); //StringBuilder是一个类，可以用来处理字符串,sb.append()字符串相加效率高
        byte[] bs = str.getBytes();//String的getBytes()方法是得到一个操作系统默认的编码格式的字节数组
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4; // 高4位, 与操作 1111 0000
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;  // 低四位, 与操作 0000 1111
            sb.append(chars[bit]);
            sb.append(' ');//每个Byte之间空格分隔
        }
        return sb.toString().trim();
    }

    /**
     * 舒达迈的条码12位并且包含空格
     * */

    public static String AgentCode(Context context,String str){
        String code ="";
        if (str.length()>12) {
            code=str.substring(str.length()-12,str.length());
        } else {
            return str;
        }
        return code;
    }

    /**
     * 获取随机数的扫描单号
     * */
    public static String RandomScanOrder(){
        // 创建一个随机数生成器

        // 生成一个17位的随机数
//        long randomNum = random.nextLong() % 100000000000000000L;
//        if (randomNum < 0) {
//            randomNum *= -1; // 如果是负数，取绝对值
//        }
   // 生成6位随机数
        Random random = new Random();
        int randomNum = random.nextInt(900000) + 100000; // 保证是6位的随机数

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
        String time = sDateFormat.format(new Date());

        // 将随机数转换为字符串，确保长度为6位
        String randomNumStr = String.valueOf(randomNum);

        return time+randomNumStr;
    }

    /**
     * 给textView加下划线
     * @param textView
     * @param color 字体颜色，如果为null或者空则默认设置#FFCC80
     */
    public static void addTextViewUnderline(TextView textView,String color)
    {
        SpannableString content = new SpannableString(textView.getText().toString());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        if(color==null||color.isEmpty())
        {
            content.setSpan(new ForegroundColorSpan(Color.parseColor("#FFCC80")), 0, content.length(), 0);
        }else{
            content.setSpan(new ForegroundColorSpan(Color.parseColor(color)), 0, content.length(), 0);
        }
        textView.setText(content);
    }



    /**
     * 判断字符串长度是否为16位
     * */
    public static boolean TextJudgmentSize(String data)
    {

        return true;

    }

    /**
     * 切换型号的提示
     * */
    public static void ShowAlertdialog(Context context,String data)
    {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        normalDialog.setTitle("切换型号");
        normalDialog.setMessage("当前切换到型号色号"+data);
        normalDialog.setPositiveButton("确定",
                null);
        normalDialog.setCancelable(false);
        normalDialog.show();
    }

    /**
     * 读取assets目录下的txt文件
     * */
    public static String readAssetsTxt(Context context, String fileName){
        try {
            InputStream is = context.getAssets().open(fileName+".txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer, "GBK");
            return text;
        } catch (IOException e) {
//	            throw new RuntimeException(e);
            e.printStackTrace();
        }
        return "读取错误，请检查文件名";
    }





}
