package com.holyes.ccssend5.lib;

import android.content.Context;
import android.net.ConnectivityManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: AccessWeb
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:25
 */
public class AccessWeb {

    private Context mContext;

    private SysUserInfo sysUserInfo;

    public String mWebId = "",port="9521";//测试9520，客户的9521

    private String webservice_url = "http://test.4006889521.cn:9520/androidDv/Android.asmx";//测试服务，客户的端口是9521

    private String webdownload_url = "http://test.4006889521.cn:9520/androidDv/DownLoadWebService.asmx";//9520

    private String namespace ="http://www.holyes.net/ccsUserLogin/";


    public AccessWeb(Context content) {
        this.mContext = content;
        sysUserInfo = new SysUserInfo(content);
        updateurl();
    }

    private static AccessWeb instance;

    public static AccessWeb getHelper(Context context)
    {
        if(instance == null)
        {
            instance = new AccessWeb(context);
        }
        return instance;
    }

    /**
     * 更新url，这个方法主要是在输入更换网址的时候要更新网址，否则网址用的还是之前的那个，就会登录失败
     */
    public void updateurl() {
        //		if(sysUserInfo.getServerip().equals("test.4006889521.cn"))
        //		{
        //			port = "9520";
        //		}else{
        //			port = "9521";
        //		}
        port = sysUserInfo.getServerport();
        webservice_url = String.format(
                "http://%1$s:%2$s/androidDv/Android.asmx",
                sysUserInfo.getServerip(),port);
        webdownload_url = String.format(
                "http://%1$s:%2$s/androidDv/DownLoadWebService.asmx",
                sysUserInfo.getServerip(),port);
    }

    /**
     * 检查服务器网络状态
     * @return
     */
    int i= 0;
    public Boolean CheckServerState() {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        para.add(map);
        try {
            getWebResult("Connectline", para);
            //Log.i("main", "5Connectline-----"+i++);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
    /**
     * 获取下载产品记录数
     *
     * @throws Exception
     * */
    public String GetDownLoadGoodsRecord(
            String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadGoodsRecord", para);
    }
    /**
     * 获取下载产品资料
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDownLoadGoodsInfor(String cUpdate)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadGoodsInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("BrandName", jsonObject2.getString("BrandName"));
            map1.put("GoodsId", jsonObject2.getString("GoodsId"));
            map1.put("GoodsDescription", jsonObject2.getString("GoodsDescription"));
            map1.put("Modelm", jsonObject2.getString("Modelm"));
            map1.put("Colors", jsonObject2.getString("Colors"));
            map1.put("GoodsYear", jsonObject2.getString("GoodsYear"));
            map1.put("ProdType", jsonObject2.getString("ProdType"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取下载供应商记录数
     *
     * @throws Exception
     * */
    public String GetDownLoadSupplierRecord(
            String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadSupplierRecord", para);
    }
    /**
     * 下载供应商函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDownLoadSupplierInfor(String cUpdate)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadSupplierInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("SupplierId", jsonObject2.getString("SupplierId"));
            map1.put("SupplierName", jsonObject2.getString("SupplierName"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }
    /**
     * 获取下载仓库记录数
     * @throws Exception
     * */
    public String GetDownLoadStockRecord(
            String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadStockRecord", para);
    }
    /**
     * 下载仓库函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDownLoadStockInfor(String cUpdate)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadStockInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StockId", jsonObject2.getString("StockId"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取下载代理商记录数
     * @throws Exception
     * */
    public String GetDownLoadAgentRecord(
            String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadAgentRecord", para);
    }
    /**
     * 下载代理商函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDownLoadAgentInfor(String cUpdate)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadAgentInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("AgentName", jsonObject2.getString("AgentName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取下载直营店记录数
     *
     * @throws Exception
     * */
    public String GetDownLoadDirectRecord(
            String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadDirectRecord", para);
    }
    /**
     * 下载直营店函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDownLoadDirectInfor(String cUpdate)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadDirectInfor", para);
        //		Log.d("main","--"+result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("TraderId", jsonObject2.getString("TraderId"));
            map1.put("TraderName", jsonObject2.getString("TraderName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("ProviceName", jsonObject2.getString("ProviceName"));
            map1.put("CityName", jsonObject2.getString("CityName"));
            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("AgentName", jsonObject2.getString("AgentName"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取下载零售商记录数
     *
     * @throws Exception
     * */
    public String GetDownLoadTraderRecord(
            String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadTraderRecord", para);
    }
    /**
     * 下载零售商函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDownLoadTraderInfor(String tCondition)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadTraderInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("TraderId", jsonObject2.getString("TraderId"));
            map1.put("TraderName", jsonObject2.getString("TraderName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("ProviceName", jsonObject2.getString("ProviceName"));
            map1.put("CityName", jsonObject2.getString("CityName"));
            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("AgentName", jsonObject2.getString("AgentName"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取下载分销店记录数
     * */
    public String GetDownLoadStoreRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadStoreRecord", para);
    }

    /**
     * 获取下载分销店函数
     * */
    public List<Map<String, Object>> GetDownLoadStoreInfor(String tCondition)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadStoreInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++)
        {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StoreId", jsonObject2.getString("StoreId"));
            map1.put("StoreName", jsonObject2.getString("StoreName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("TraderId", jsonObject2.getString("TraderId"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }



    /**
     * 获取下载直营分销店记录数
     * */
    public String GetDownLoadStraightShopRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadStraightShopRecord", para);
    }

    /**
     * 获取下载直营分销店函数
     * */
    public List<Map<String, Object>> GetDownLoadStraightShopInfor (String tCondition)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadStraightShopInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++)
        {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StoreId", jsonObject2.getString("StoreId"));
            map1.put("StoreName", jsonObject2.getString("StoreName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("TraderId", jsonObject2.getString("TraderId"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载品检入库表头函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDowLoadPurCheckBill()
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadPurCheckBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PurCheckLno", jsonObject2.getString("PurCheckLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("SupplierName", jsonObject2.getString("SupplierName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("SupplierId", jsonObject2.getString("SupplierId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载品检入库明细函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDowLoadPurCheckDetail(String tBillNo)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadPurCheckDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoInGoodQty"));
            map1.put("amount", jsonObject2.getString("NoInGoodQty"));//增加一个字段
            list.add(map1);
        }
        return list;
    }
    /**
     * 下载销售配货代销表头函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDowLoadAgentInvoiceBill()
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAgentInvoiceBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PeiGoodLno", jsonObject2.getString("PeiGoodLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("AgentName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }
    /**
     * 下载销售配货代销明细函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDowLoadAgentInvoiceDetail(String tBillNo)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAgentInvoiceDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoSendGoodQty"));
            map1.put("amount", jsonObject2.getString("NoSendGoodQty"));//增加一个数量字段
            list.add(map1);
        }
        return list;
    }
    /**
     * 下载销售配货直销表头函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDowLoadDirectInvoiceBill()
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadDirectInvoiceBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PeiGoodLno", jsonObject2.getString("PeiGoodLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("TraderName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("TraderId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }
    /**
     * 下载销售配货直销明细函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDowLoadDirectInvoiceDetail(String tBillNo)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadDirectInvoiceDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoSendGoodQty"));
            map1.put("amount", jsonObject2.getString("NoSendGoodQty"));//增加一个字段
            list.add(map1);
        }
        return list;
    }
    /**
     * 下载销售退货代销表头函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDowLoadAgentReturnBill()
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAgentReturnBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PeiGoodLno", jsonObject2.getString("PeiGoodLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("AgentName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载销售退货直销表头函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetDowLoadDirectReturnBill()
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadDirectReturnBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PeiGoodLno", jsonObject2.getString("PeiGoodLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("TraderName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("TraderId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载套餐设置函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetPackMealSet()
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetPackMealSet", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PackId", jsonObject2.getString("PackId"));
            map1.put("PackName", jsonObject2.getString("PackName"));

            list.add(map1);
        }
        return list;
    }

    /**
     * 下载套餐明细函数
     * @throws Exception
     * */
    public List<Map<String, Object>> GetPackMealDetail(String tPackId)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tPackId", tPackId);
        para.add(map);
        String result = downLoadWebResult("GetPackMealDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("GoodsId", jsonObject2.getString("GoodsId"));
            map1.put("BrandName", jsonObject2.getString("BrandName"));
            map1.put("GoodsType", jsonObject2.getString("GoodsType"));
            map1.put("SeriesName", jsonObject2.getString("SeriesName"));
            map1.put("Modelm", jsonObject2.getString("Modelm"));
            map1.put("Colors", jsonObject2.getString("Colors"));
            map1.put("PackNum", String.valueOf(jsonObject2.getInt("PackNum")));
            list.add(map1);
        }
        return list;
    }

    /**
     * 套餐装盒验证
     * @throws Exception
     * */
    public String P_Dv_PackDressBoxCheck( String tPara)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String result = getWebResult("P_Dv_PackDressBoxCheck", para);

        return result;
    }

    /**
     * 套餐装盒完成上传
     * @throws Exception
     * */
    public String P_Dv_PackDressBoxWrite( String tPara)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String result = getWebResult("P_Dv_PackDressBoxWrite", para);

        return result;
    }

    /**
     * 套餐装盒补打读取套标信息
     * @throws Exception
     * */
    public String GetPackMealBoxLabel( String tBarcode)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBarcode", tBarcode);	//已套餐装盒中任一物流码
        para.add(map);
        String result = downLoadWebResult("GetPackMealBoxLabel", para);

        return result;
    }



    /**
     * 扫描操作函数
     * @param methodName 要执行操作的方法名
     * P_Dv_InStock_Bill：有单入库
     * P_Dv_InStock_Bill_Cancel：有单撤消入库
     * P_Dv_InStock_NoBill：无单入库
     * P_Dv_InStock_NoBill_Cancel：无单撤消入库
     * P_Dv_ReturnedPurchase_Z_G_Bill：有单入库退回
     * P_Dv_ReturnedPurchase_Z_G_Bill_Cancel：有单入库退回撤消
     * P_Dv_ReturnedPurchase_Z_G_NoBill：无单入库退回
     * P_Dv_ReturnedPurchase_Z_G_NoBill_Cancel：无单入库退回撤消
     *
     * @param methodName 方法名
     * @param tPara 扫描参数
     * @throws Exception
     * */
    public String P_Dv_Scan(String methodName, String tPara)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        if("P_Dv_RecoverScan_D".equals(methodName))//除了代理回收扫描大写外，其他的几乎小写，实际去服务里看
        {
            map.put("tLoginID", sysUserInfo.getLoginid());
        }else{
            map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        }
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String result = getWebResult(methodName, para);
        return result;
    }



    /**
     * 总公司物流查询
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-11-30 上午11:29:56
     * @param tCodeType
     * @param tCodeValue
     * @param tUnitId
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> P_ProductLogist(String tCodeType, String tCodeValue,String tUnitId)
            throws Exception
    {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        map.put("tCodeType", tCodeType);
        map.put("tCodeValue", tCodeValue);
        map.put("tUnitId", tUnitId);
        para.add(map);
        String result = getWebResult("P_ProductLogist", para);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        JSONArray listjson = new JSONArray(result);

        for (int i = 0; i < listjson.length(); i++)
        {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);

            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("Remark", jsonObject2.getString("memo"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 物流查询
     * @param tBcOrAc 查码类型(其值为：True-物流码;False-表示防伪码)
     * @param tCode 查码内容,扫描条码
     * @param tCompnay 用户所在公司代号(总公司用户为：00，代理商用户为所在代理商代号)
     */
    public List<Map<String, Object>> BarcodeLogistics(String tCode, boolean tBcOrAc, String tCompnay)
            throws Exception
    {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        map.put("tCode", tCode);
        map.put("tBcOrAc", tBcOrAc);
        map.put("tCompnay", tCompnay);
        para.add(map);
        String result = getWebResult("BarcodeLogistics", para);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        JSONArray listjson = new JSONArray(result);

        for (int i = 0; i < listjson.length(); i++)
        {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);

            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("Remark", jsonObject2.getString("memo"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载入库退出表头函数
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadPurOutBill()
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadPurOutBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PurCheckLno", jsonObject2.getString("PurCheckLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("SupplierName", jsonObject2.getString("SupplierName"));
            map1.put("SupplierId", jsonObject2.getString("SupplierId"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 退货选择客户扫码参数
     * */

    public List<Map<String, Object>> P_Dv_SeekCompanyInfo(String tCompanyType, String tBarcode)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCompanyType", tCompanyType);
        map.put("tBarcode", tBarcode);
        para.add(map);
        String result = getWebResult("P_Dv_SeekCompanyInfo", para);
        JSONObject listjson = new JSONObject(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            //			JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PId", listjson.getString("PId"));
            map1.put("CompanyId", listjson.getString("CId"));
            map1.put("CompanyName", listjson.getString("CName"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载入库退出明细函数
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadPurOutDetail(String tBillNo)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadPurOutDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoOutGoodQty"));
            map1.put("amount", jsonObject2.getString("NoOutGoodQty"));//增加一个字段
            list.add(map1);
        }
        return list;
    }
    /**
     * 下载仓库调拨单表头函数
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadAllots()
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAllots", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++)
        {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("AllotLno", jsonObject2.getString("AllotLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("OutStockName", jsonObject2.getString("OutStockName"));
            map1.put("InStockName", jsonObject2.getString("InStockName"));
            map1.put("NoAllotQty", jsonObject2.getString("NoAllotQty"));
            map1.put("OutStockId", jsonObject2.getString("OutStockId"));
            map1.put("InStockId", jsonObject2.getString("InStockId"));
            list.add(map1);
        }
        return list;
    }
    /**
     * 下载入库退出明细函数
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadAllotsdetail(String tBillNo)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAllotsdetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoAllotGoodQty"));
            map1.put("amount", jsonObject2.getString("NoAllotGoodQty"));//增加一个字段
            list.add(map1);
        }
        return list;
    }

    /**
     * 用户登录函数
     * */
    public String UserLogin(String pUsercode, String pPsw) throws Exception {
        String result = "";
        // SOAP Action
        String SOAP_ACTION = this.namespace + "UserLogin";//
        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject(this.namespace, "UserLogin");
        // 设置需调用WebService接口需要传入的参数
        rpc.addProperty("pUsercode", pUsercode);
        rpc.addProperty("pPsw", pPsw);
        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER10);
        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);

        // HttpTransportSE transport = new HttpTransportSE(this.webservice_url);
        MyAndroidHttpTransport transport = new MyAndroidHttpTransport(
                this.webservice_url, 300000);//5分钟
        try {
            // 调用WebService
            transport.call(SOAP_ACTION, envelope);
            // 获取返回的数据
            SoapObject object = (SoapObject) envelope.bodyIn;
            //			SoapObject object = (SoapObject) envelope.getResponse();
            // 获取返回的结果
            result = object.getProperty(0).toString();
        } catch (IOException e) {
            throw new Exception("服务器:（IOException网络超时）"+e);
        } catch (Exception e) {
            throw new Exception("服务器:服务器连接失败"+e.getMessage());
        }

        if (result.isEmpty())
        {
            throw new Exception("网络超时");
        }
        return result;
    }


    private String getWebResult(String methodName,ArrayList<HashMap<Object, Object>> propertys) throws Exception {

        if (!CheckNetWorkStatus())
        {
            throw new Exception("当前网络不可用，请检查设置！");
        }
        String result = "";
        try {
            SoapObject rpc =null;
            rpc = new SoapObject(this.namespace, methodName);
            for (HashMap<Object, Object> info : propertys) {
                Set<Object> set = info.keySet();
                Iterator<Object> iterator = set.iterator();

                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    rpc.addProperty(key, info.get(key));
                }
            }

            String SOAP_ACTION = rpc.getNamespace() + rpc.getName();

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER10);

            envelope.bodyOut = rpc;
            // 设置是否调用的是dotNet开发的WebService
            envelope.dotNet = true;
            // 等价于envelope.bodyOut = rpc;
            envelope.setOutputSoapObject(rpc);
            // HttpTransportSE transport = new
            // HttpTransportSE(this.webservice_url);
            MyAndroidHttpTransport transport;
            transport = new MyAndroidHttpTransport(
                    this.webservice_url, 5000); // set timeout 5s

            transport.debug = true;

            try {
                // 调用WebService
                //				long beforeTime = System.currentTimeMillis();
                transport.call(SOAP_ACTION, envelope);
                //				if(!methodName.equals("Connectline"))
                //				{
                //					//					Log.i("main", methodName+"---请求用时---"+(System.currentTimeMillis()-beforeTime));
                //				}
                SoapObject object = (SoapObject) envelope.bodyIn;
//				SoapObject object = (SoapObject) envelope.getResponse();
                // 获取返回的结果
                result = object.getProperty(0).toString();


            } catch (IOException e) {
                throw new Exception("服务器连接超时"+e.getMessage());
            } catch (Exception e) {
                throw e;
            }
            if (result == null || result == "") {
                throw new Exception("网络超时");
            }
            //			if (result.split(";").length < 2) {
            //				throw new Exception("本地处理:返回的值格式不正确.\r\n" + result);
            //			}

            //
            if (result.split(";")[0].equalsIgnoreCase("true"))
            {
                //如果用户在新增资料时填写的内容有“;”就会造成result被截成多个，要返回后面的全部
                if(result.split(";").length>2)
                {
                    String res = "";
                    for(int i=1;i<result.split(";").length;i++)
                    {
                        res+=result.split(";")[i];
                    }
                    return res;
                }
                return result.split(";")[1];
            } else {
                throw new Exception("服务器：" + result.split(";")[1]);
            }
        } catch (Exception e) {
            throw new Exception("发生错误："+e.getMessage());
        }

    }

    /**
     * 下载服务
     * @param methodName
     * @param propertys
     * @return
     * @throws Exception
     */
    private String downLoadWebResult(String methodName,ArrayList<HashMap<Object, Object>> propertys) throws Exception {

        if (!CheckNetWorkStatus())
        {
            throw new Exception("当前网络不可用，请检查设置！");
        }
        String result = "";
        try {
            SoapObject rpc =null;
            rpc = new SoapObject(this.namespace, methodName);
            for (HashMap<Object, Object> info : propertys) {
                Set<Object> set = info.keySet();
                Iterator<Object> iterator = set.iterator();

                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    rpc.addProperty(key, info.get(key));
                }
            }

            String SOAP_ACTION = rpc.getNamespace() + rpc.getName();

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER10);

            envelope.bodyOut = rpc;
            // 设置是否调用的是dotNet开发的WebService
            envelope.dotNet = true;
            // 等价于envelope.bodyOut = rpc;
            envelope.setOutputSoapObject(rpc);
            // HttpTransportSE transport = new
            // HttpTransportSE(this.webservice_url);
            MyAndroidHttpTransport transport;
            transport = new MyAndroidHttpTransport(
                    this.webdownload_url, 5000); // set timeout 5s

            transport.debug = true;

            try {
                // 调用WebService
                transport.call(SOAP_ACTION, envelope);
                SoapObject object = (SoapObject) envelope.bodyIn;
                //				SoapObject object = (SoapObject) envelope.getResponse();
                // 获取返回的结果
                result = object.getProperty(0).toString();


            } catch (IOException e) {
                throw new Exception("服务器连接超时");
            } catch (Exception e) {
                throw new Exception("服务器:" + e.getCause());
            }
            if (result == null || result == "") {
                throw new Exception("网络超时");
            }
            if (result.split(";").length < 2) {
                throw new Exception("本地处理:返回的值格式不正确.\r\n" + result);
            }

            //
            if (result.split(";")[0].equalsIgnoreCase("true"))
            {
                //如果用户在新增资料时填写的内容有“;”就会造成result被截成多个，要返回后面的全部
                if(result.split(";").length>2)
                {
                    String res = "";
                    for(int i=1;i<result.split(";").length;i++)
                    {
                        res+=result.split(";")[i];
                    }
                    return res;
                }
                //
                return result.split(";")[1];
            } else {
                throw new Exception("服务器：" + result.split(";")[1]);
            }
        } catch (Exception e) {
            throw  new Exception("服务器："+e.getMessage());
        }

    }

    // 检查3G网络和WiFi的，在这个项目中不需要
    public Boolean CheckNetWorkStatus()
    {

        boolean netSataus = false;

        ConnectivityManager cwjManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cwjManager.getActiveNetworkInfo() != null)
        {
            netSataus = cwjManager.getActiveNetworkInfo().isAvailable();
        }

        return netSataus;
    }


    /**
     * 获取菜单函数
     * GetDevMenuInfor
     * @param tBusinessId 品牌代号
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDevMenuInfor(String tBusinessId) throws Exception {
        String result = "";
        // SOAP Action
        String SOAP_ACTION = "http://tempuri.org/GetDevMenuInfor";
        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject("http://tempuri.org/", "GetDevMenuInfor");
        // 设置需调用WebService接口需要传入的参数
        rpc.addProperty("tBusinessId", tBusinessId);
        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER10);
        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE("http://www.4006889521.cn:9516/softreg/WebRegService.asmx");
        // 调用WebService
        transport.call(SOAP_ACTION, envelope);
        // 获取返回的数据
        SoapObject object = (SoapObject) envelope.bodyIn;
        //			SoapObject object = (SoapObject) envelope.getResponse();
        // 获取返回的结果
        result = object.getProperty(0).toString();

        if(result.split(";")[0].equals("true"))
        {
            JSONArray jsonArray = new JSONArray(result.split(";")[1]);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Map<String, Object> map;
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
                map = new HashMap<String, Object>();
                map.put("menucode", jsonObject.getString("MenuCode"));//菜单编号
                map.put("menuname", jsonObject.getString("MenuName"));//菜单名称
                map.put("parentcode", jsonObject.getString("ParentCode"));//父级编号
                map.put("showstatus", jsonObject.getString("ShowStatus"));//菜单显示状态，1显示，0不显示
                map.put("procedurename", jsonObject.getString("ProcedureName"));//存储过程名称

                list.add(map);
            }
            return list;
        }else{
            throw new Exception("服务器：" + result.split(";")[1]);
        }

    }

    /**
     * 判断版本第四代还是第五代
     * 返回True是第五代、返回false是第四代
     * @param tBrandNessCode 品牌代号
     * @return
     * @throws Exception
     */
    public boolean JudgeBrandNessVer(String tBrandNessCode){
        try{
            String result = "";
            // SOAP Action
            String SOAP_ACTION = "http://tempuri.org/JudgeBrandNessVer";
            // 指定WebService的命名空间和调用的方法名
            SoapObject rpc = new SoapObject("http://tempuri.org/", "JudgeBrandNessVer");
            // 设置需调用WebService接口需要传入的参数
            rpc.addProperty("tBrandNessCode", tBrandNessCode);
            // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER10);
            envelope.bodyOut = rpc;
            // 设置是否调用的是dotNet开发的WebService
            envelope.dotNet = true;
            // 等价于envelope.bodyOut = rpc;
            envelope.setOutputSoapObject(rpc);
            HttpTransportSE transport = new HttpTransportSE("http://www.4006889521.cn:9516/softreg/WebRegService.asmx");
            // 调用WebService
            transport.call(SOAP_ACTION, envelope);
            // 获取返回的数据
            SoapObject object = (SoapObject) envelope.bodyIn;
            //			SoapObject object = (SoapObject) envelope.getResponse();
            // 获取返回的结果
            result = object.getProperty(0).toString().trim();
            return  Boolean.parseBoolean(result.split(";")[0]);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }


    }



    /**
     *判断当前品牌商是否有首发发货功能
     * GettFirstDeliveryStatus
     * @param tBrandNessCode 品牌代号
     * @return
     * @throws Exception
     */
    public String GettFirstDeliveryStatus(String tBrandNessCode) throws Exception{
        try{
            String result = "";
            // SOAP Action
            String SOAP_ACTION = "http://tempuri.org/GettFirstDeliveryStatus";
            // 指定WebService的命名空间和调用的方法名
            SoapObject rpc = new SoapObject("http://tempuri.org/", "GettFirstDeliveryStatus");
            // 设置需调用WebService接口需要传入的参数
            rpc.addProperty("tBrandNessCode", tBrandNessCode);
            // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER10);
            envelope.bodyOut = rpc;
            // 设置是否调用的是dotNet开发的WebService
            envelope.dotNet = true;
            // 等价于envelope.bodyOut = rpc;
            envelope.setOutputSoapObject(rpc);
            HttpTransportSE transport = new HttpTransportSE("http://www.4006889521.cn:9516/softreg/WebRegService.asmx");
            // 调用WebService
            transport.call(SOAP_ACTION, envelope);
            // 获取返回的数据
            SoapObject object = (SoapObject) envelope.bodyIn;
            //			SoapObject object = (SoapObject) envelope.getResponse();
            // 获取返回的结果
            result = object.getProperty(0).toString();
            if (result == null || result == "") {
                throw new Exception("网络超时");
            }
            if (result.split(";").length < 2) {
                throw new Exception("本地处理:返回的值格式不正确.\r\n" + result);
            }

            if (result.split(";")[0].equalsIgnoreCase("true"))
            {
                //如果用户在新增资料时填写的内容有“;”就会造成result被截成多个，要返回后面的全部
                if(result.split(";").length>2)
                {
                    String res = "";
                    for(int i=1;i<result.split(";").length;i++)
                    {
                        res+=result.split(";")[i];
                    }
                    return res;
                }
                return result.split(";")[1];
            } else {
                throw new Exception("服务器：" + result.split(";")[1]);
            }

        } catch (Exception e) {
            throw new Exception("服务器:" + e.getCause());
        }
    }



    /**
     * 代理商发货通添加品牌验证服务
     * @param tAccreditCode 品牌验证码
     * @param tEsn 设备序列号
     * @throws Exception
     * */
    public Map<String, Object> AccreditCodeLeadingV2(String tAccreditCode,String tEsn)
            throws Exception {
        String result = "";
        // SOAP Action
        String SOAP_ACTION = "http://tempuri.org/AccreditCodeLeadingV2";
        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject("http://tempuri.org/", "AccreditCodeLeadingV2");
        // 设置需调用WebService接口需要传入的参数
        rpc.addProperty("tAccreditCode", tAccreditCode);
        rpc.addProperty("tEsn",tEsn);

        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER10);

        envelope.bodyOut = rpc;

        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);
        //				HttpTransportSE transport = new HttpTransportSE(
        //						"http://www.4006889521.cn:9516/softreg/WebRegService.asmx");
        MyAndroidHttpTransport transport = new MyAndroidHttpTransport(
                "http://www.4006889521.cn:9516/softreg/WebRegService.asmx", 300000);//5分钟
        try {
            // 调用WebService
            transport.call(SOAP_ACTION, envelope);
            // 获取返回的数据
            SoapObject object = (SoapObject) envelope.bodyIn;
            //SoapObject object = (SoapObject) envelope.getResponse();
            // 获取返回的结果
            result = object.getProperty(0).toString();
        } catch (IOException e) {
            throw new Exception("服务器连接超时"+e.getMessage());
        } catch (Exception e) {
            throw new Exception("服务器:" + e.getCause());
        }
        if (result == null || result == "") {
            throw new Exception("网络超时");
        }
        if (result.split(";")[0].equalsIgnoreCase("true"))
        {
            result=result.split(";")[1];
            JSONArray listjson = new JSONArray(result);
            Map<String, Object> map1 = new HashMap<String, Object>();
            for (int i = 0; i < listjson.length(); i++) {
                JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                map1.put("businessid", jsonObject2.getString("BusinessId"));
                map1.put("brandname", jsonObject2.getString("BrandName"));
                map1.put("serverccip", jsonObject2.getString("ServerCcIp"));
                map1.put("servernetip", jsonObject2.getString("ServerNetIp"));
                map1.put("port", jsonObject2.getString("Port"));
                map1.put("logurl", jsonObject2.getString("LogUrl"));
            }
            return (Map<String, Object>) map1;
        } else {
            throw new Exception("服务器：" + result.split(";")[1]);
        }
    }


    /** 代理商给零售商发货 */

    public String P_Dv_OutStock_D_L_NoBill(String barcode, String pSoCompid, String pCompid,
                                           String nSN, String Scanbillno, String mBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("pBarcode", barcode);
        map.put("pSoCompid", pSoCompid);
        map.put("pCompid", pCompid);
        map.put("pUserId", sysUserInfo.getUserid());
        map.put("pSN", nSN);
        map.put("pScanBillNo", Scanbillno);
        map.put("mBillNo", mBillNo);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_D_L_NoBill", para);
        return data;
    }


    /** 代理商给零售商发货  首发
     *
     *tLoginId：用户登录成功的LogId
     tWebId：当前扫描条码
     tPara：Josn字符串
     Barcode：扫描的条码(必传)
     SoCompId：代理商代号(必传)
     DeCompId：零售商代号(必传)
     OaSuserId：扫描人员代号(必传)
     ScanSn：扫描序号(必传)
     ScanBillNo：扫描单号(必传)
     BillNo：发货单号(首次扫码传空，成功再扫码时传返回的发货单号)
     FirstDelivery：是否首次发货(0-非首次；1-首次)
     * */

    public String P_Dv_OutStock_D_L_NoBillv1(String tPara) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_D_L_NoBillv1", para);
        return data;
    }


    /** 代理商给分销店发货
     *
     *tLoginId：用户登录成功的LogId
     tWebId：当前扫描条码
     tPara：Josn字符串
     Barcode：扫描的条码(必传)
     SoCompId：代理商代号(必传)
     DeCompId：零售商代号(必传)
     StoreId： 分店代号
     OaSuserId：扫描人员代号(必传)
     ScanSn：扫描序号(必传)
     ScanBillNo：扫描单号(必传)
     BillNo：发货到零售商单号(首次扫码传空，成功再扫码时传返回的发货单号)
     DocumentNo：发货到分销店单号(首次扫码传空，成功再扫码时传返回的发货单号)
     FirstDelivery：是否首次发货(0-非首次；1-首次)
     * */

    public String P_Dv_OutStock_D_S_NoBillv1(String tPara) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_D_S_NoBillv1", para);
        return data;
    }


    /** 代理商给分销店发货
     *  tLoginId：用户登录成功的LogId
     tWebId：当前扫描条码
     pBarcode：扫描的条码(必传)
     pSoCompid：代理商代号(必传)
     pCompid：零售商代号(必传)
     pShopCompid：分销店代号(必传)
     pUserId：扫描人员代号(必传)
     pSN：扫描序号(必传)
     pScanBillNo：扫描单号(必传)
     pBillNo：零售商发货单号(首次传空，其他情况传返回的发货单号)
     pShopBillNo：分销店发货单号(首次传空，其他情况传返回的发货单号)*/

    public String P_Dv_OutStock_D_S_NoBill(String barcode, String pSoCompid, String pCompid,String pShopCompid,
                                           String nSN, String Scanbillno, String pBillNo,String pShopBillNo) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("pBarcode", barcode);
        map.put("pSoCompid", pSoCompid);
        map.put("pCompid", pCompid);
        map.put("pShopCompid", pShopCompid);
        map.put("pUserId", sysUserInfo.getUserid());
        map.put("pSN", nSN);
        map.put("pScanBillNo", Scanbillno);
        map.put("pBillNo", pBillNo);
        map.put("pShopBillNo", pShopBillNo);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_D_S_NoBill", para);
        return data;
    }

    /** 零售商给代理商退货
     * tLoginId：用户登录成功的LogId
     tWebId：当前扫描条码
     pBarcode：扫描的条码(必传)
     pSoCompid：代理商代号(必传)
     pCompid：零售商代号(不传)
     pUserId：扫描人员代号(必传)
     pSN：扫描序号(必传)
     pScanBillNo：扫描单号(必传)
     mBillNo：退货单号(首次传空，其他情况传返回的退货单号)
     */
    public String P_Dv_ReturnedPurchase_D_L_NoBill(String barcode, String pSoCompid, String pCompid,
                                                   String nSN, String Scanbillno, String mBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("pBarcode", barcode);
        map.put("pSoCompid", pSoCompid);
        map.put("pCompid", pCompid);
        map.put("pUserId", sysUserInfo.getUserid());
        map.put("pSN", nSN);
        map.put("pScanBillNo", Scanbillno);
        map.put("mBillNo", mBillNo);
        para.add(map);
        String data = getWebResult("P_Dv_ReturnedPurchase_D_L_NoBill", para);
        return data;
    }

    /**
     *	创建总公司装盒入库任务
     * @param tLoginId
     * @param tWebId
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_CreatePackingJob(String tLoginId,String tWebId,String tPara) throws Exception
    {
        String result = "";
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tWebId", tWebId);
        map.put("tPara", tPara);
        para.add(map);
        result = getWebResult("P_Dv_InStock_CreatePackingJob", para);

        return result;
    }

    /**
     * 总公司装盒创建临时盒标(没用
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_NewTempBoxNo(String tLoginId,  String tBillNo) throws Exception
    {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_NewTempBoxNo", para);
        return result;
    }


    /**
     *总公司装盒检查条码状态(没用
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_PackBarcodeState(String tLoginId, String tTempBoxNo, String tBarCode) throws Exception
    {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tTempBoxNo", tTempBoxNo);
        map.put("tBarCode", tBarCode);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_PackBarcodeState", para);
        return result;
    }

    /**
     *创建总公司装盒入库(没用
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_PackBox_NoBill(String tLoginId, String tWebId,
                                              String tTempBoxNo,String tProduct_id,int tNum) throws Exception
    {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tWebId", tWebId);
        map.put("tTempBoxNo", tTempBoxNo);
        map.put("tProduct_id", tProduct_id);
        map.put("tNum", tNum);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_PackBox_NoBill", para);
        return result;
    }

    /**
     * 装盒入库
     * @author van van.shu@magic-point.com
     * @version 创建时间：2018-1-18 下午4:11:57
     * @param tLoginId
     * @param tWebId
     * @param tBarCodes
     * @param tProduct_id
     * @param tNum
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_PackBox_List_NoBill(String tLoginId, String tWebId,
                                                   String tBarCodes,String tProduct_id,int tNum) throws Exception
    {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tWebId", tWebId);
        map.put("tBarCodes", tBarCodes);
        map.put("tProduct_id", tProduct_id);
        map.put("tNum", tNum);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_PackBox_List_NoBill", para);
        return result;
    }

    /**
     * 查找盒标
     * @author van van.shu@magic-point.com
     * @version 创建时间：2018-1-18 下午4:12:15
     * @param tLoginId
     * @param tTempBoxNo 条码或盒标
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_PackBox_Search(String tLoginId, String tTempBoxNo) throws Exception
    {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tTempBoxNo", tTempBoxNo);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_PackBox_Search", para);
        return result;
    }


    /**
     * 查看单据明细
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadBilldetail(String tLoginId, String tScanBillNo) throws Exception
    {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tScanBillNo", tScanBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadBilldetail", para);
        JSONArray jsonArray = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> maps;
        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
            maps = new HashMap<String, Object>();
            maps.put("goodsid", jsonObject.getString("GoodsId"));
            maps.put("modelm", jsonObject.getString("Modelm"));
            maps.put("colors", jsonObject.getString("Colors"));
            maps.put("curcount", jsonObject.getString("Num"));

            list.add(maps);
        }
        return list;
    }


    /** 总公司无单无入库(直营店分销店)发货
     *  tPara    -->Json 格式字符串,包含以下属性：
     Barcode:物流码(条码)    传值:扫描的条码
     GoodsId:产品编码             选择的产品编号
     SoCompId:来源单位编码        选择的直营店
     DeCompId:目的单位编码        选择的分销店
     OaSuserId:操作员代号         操作员代号
     StockId:仓库编码             01
     ScanSn:扫描序号              流水号
     ScanBillNo:扫描单号          本地生成（见以前代码）
     BillNo:单据编号              首次传空，第二次按服务器返回单号传。
     SourceBillNo:来源单号        空
     DocumentNo:单据编号          空
     */
    public String P_Dv_OutStock_Z_S_NoBill_NoInStock(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_S_NoBill_NoInStock", para);
        return data;
    }

    /**
     * 总公司无单无入库(直营店分销店)发货撤销
     */
    public String P_Dv_OutStock_Z_S_NoBill_NoInStock_Cancel(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_S_NoBill_NoInStock_Cancel", para);
        return data;
    }

    /** 总公司无单有入库(直营店分销店)发货
     *  Barcode:物流码(条码)    传值:扫描的条码
     GoodsId:产品编码             空
     SoCompId:来源单位编码        选择的直营店
     DeCompId:目的单位编码        选择的分销店
     OaSuserId:操作员代号         操作员代号
     StockId:仓库编码             选择的仓库编号
     ScanSn:扫描序号              流水号
     ScanBillNo:扫描单号          本地生成（见以前代码）
     BillNo:单据编号              首次传空，第二次按服务器返回单号传。
     SourceBillNo:来源单号        空
     DocumentNo:单据编号          空
     */
    public String P_Dv_OutStock_Z_S_NoBill_InStock(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_S_NoBill_InStock", para);
        return data;
    }

    /**
     * 总公司无单有入库(直营店分销店)发货撤销
     */
    public String P_Dv_OutStock_Z_S_NoBill_InStock_Cancel(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_S_NoBill_InStock_Cancel", para);
        return data;
    }

    /** 无单(直营分店)退货
     * Barcode:物流码(条码)    传值:扫描的条码
     GoodsId:产品编码             空
     SoCompId:来源单位编码        选择的直营店
     DeCompId:目的单位编码        选择的分销店
     OaSuserId:操作员代号         操作员代号
     StockId:仓库编码             选择的仓库编号
     ScanSn:扫描序号              流水号
     ScanBillNo:扫描单号          本地生成（见以前代码）
     BillNo:单据编号              首次传空，第二次按服务器返回单号传。
     SourceBillNo:来源单号        空
     DocumentNo:单据编号          空
     */
    public String P_Dv_ReturnedPurchase_Z_S_NoBill(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_ReturnedPurchase_Z_S_NoBill", para);
        return data;
    }

    /**
     * 无单(直营分店)退货撤消
     */
    public String P_Dv_ReturnedPurchase_Z_S_NoBill_Cancel(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_ReturnedPurchase_Z_S_NoBill_Cancel", para);
        return data;
    }
    /**直营分销店发货
     *  tLoginId -->登录ID
     tWebId   -->条码，用做网络令牌
     tPara    -->Json 格式字符串,包含以下属性：
     {
     Barcode:物流码(条码)    传值:物流码(必传)
     DeCompId:产品编码            分店代号(必传)
     OaSuserId:操作员代号         操作员代号(必传)
     StockId:仓库编码             选择的仓库编号(必传)
     ScanSn:扫描序号              流水号(必传)
     ScanBillNo:扫描单号          本地生成（见以前代码）
     SourceBillNo:单据编号        直营店单号，首次传空，第二次按服务器返回单号传。
     BillNo:单据编号              分店单号，首次传空，第二次按服务器返回单号传。
     }
     */

    public String P_Dv_OutStock_Z_L_S_NoBill_InStock( String tPara) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_L_S_NoBill_InStock", para);
        return data;
    }

    /**
     * 下载代理商调货单函数
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadTransferBill(String tCompanyId)
            throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCompanyId", tCompanyId);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadTransferBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("TransferNo", jsonObject2.getString("TransferNo"));
            map1.put("InAgentName", jsonObject2.getString("InAgentName"));
            map1.put("TransferDate", jsonObject2.getString("TransferDate"));
            list.add(map1);
        }
        return list;
    }



    /**
     * 代理商调货单发货函数
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     * @return
     * @throws Exception
     */

    public String P_Dv_TransferGoods_D( String tPara) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_TransferGoods_D", para);
        return data;
    }

}
