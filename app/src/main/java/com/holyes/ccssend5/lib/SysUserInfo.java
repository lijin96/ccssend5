package com.holyes.ccssend5.lib;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @ClassName: SysUserInfo
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:29
 */
public class SysUserInfo {
    private final Context mContext;

    public static String pSoftType = "v8";

    /***/
    private String brand = "holyes";//>>>该变量暂时没什么用

    /** 测试的是哪个品牌,改变这里改变ip地址等，一般用holyes，这只是为了方便测试的，给客户安装的让客服给客户重新修改即可*/

    public SysUserInfo(Context context)
    {
        mContext = context;
    }

    /** 下载链接 */
    public String getApkUrl()
    {
        if (ReadConfigString("apkUrl").isEmpty())
        {
            return String.format("http://www.4006889521.cn/softupdate/Android/Dev/T9/%1$s/ccs5v01.apk", getEnterpriseId());
        }

        return ReadConfigString("apkUrl");
    }

    public String getBrand()
    {
        return ReadConfigString("brand");
    }

    /** 配置文件 */
    public String getUploadSoftUrl()
    {
//		if(ADevicesManager.CorpCompany)
        if(!getSoftType().equals("52"))//这样设置之后只要修改系统类型就可以下载对应的apk,总公司模式和代理商模式就可以转换了。
        {
            //总公司模式
            return String.format("http://www.4006889521.cn/softupdate/Android/Dev/T9/%1$s/ver01.xml",getEnterpriseId());
        }else{
            return String.format("http://www.4006889521.cn/softupdate/Android/Dev/T9/agent/ver52.xml");
        }

    }

    /**
     * 获取服务器ip//>>>
     */
    public String getServerip()
    {
        String rest = ReadConfigString("serverip").trim();
        rest = rest.isEmpty() ? "test.4006889521.cn" : rest;//合力思
        return rest;
    }
    /**
     * 获取服务器ip的端口号//测试9520，其他的9521，在AccessWeb里面用>>>
     */
    public String getServerport()
    {
        String rest = ReadConfigString("serverport").trim();
        rest = rest.isEmpty() ? "9521" : rest;
        return rest;
    }

    /**
     * 获取系统类型，01总公司，其他为代理商，一般为52
     */
    public String getSoftType()
    {
        //return "";
        String rest = ReadConfigString("softtype").trim();
        rest = rest.isEmpty() ? "01" : rest;
        return rest;
    }

    /**
     * 获取品牌代号
     */
    public String getEnterpriseId()
    {
        String rest = ReadConfigString("enterpriseid");
        rest = rest.isEmpty() ? "00" : rest;//合力思
        return rest;
    }

    public String getClientId()
    {
        String rest = ReadConfigString("ClientId");
        rest = rest.isEmpty() ? "00" : rest;
        return rest;
    }


    public String getBusikind()
    {
        return ReadConfigString("busikind");
    }

    public String getCity()
    {
        return ReadConfigString("city");
    }

    public String getCompanyid()
    {
        return ReadConfigString("companyid");
    }

    public String getDeviceid()
    {
        return ReadConfigString("deviceid");
    }
    public String getClassname()
    {
        return ReadConfigString("classname");
    }
    public String getStock()
    {
        return ReadConfigString("Stock");
    }


    public String getKefu()
    {
        return "客服电话：0755-29822832\n深圳合力思科技有限公司版权所有";
    }

    /**
     * 是否记住密码
     * @return
     */
    public Boolean getIfrember()
    {
        return ReadConfigBoolean("ifrember");
    }


    public Boolean getChange()
    {
        return ReadConfigBoolean("change");
    }


    public String getLoginid()
    {
        return ReadConfigString("loginid");
    }

    /**
     * 获取记住密码后保存的密码
     */
    public String getLoginpwd()
    {
        return ReadConfigString("loginpwd");
    }

    /**
     * 获取记住密码后保存的账号
     */
    public String getMobile()
    {
        return ReadConfigString("mobile");
    }

    public String getProvince()
    {
        return ReadConfigString("province");
    }
    public String getFirstStart()
    {
        return ReadConfigString("start");
    }

    public String getMode()
    {
        return ReadConfigString("Mode");
    }

    public String getQuyu()
    {
        return ReadConfigString("quyu");
    }

    public Boolean getUpdate()
    {
        return ReadConfigBoolean("update");
    }

    public String getUpdateMsg()
    {
        return ReadConfigString("updateMsg");
    }

    public String getUserid()
    {

        return ReadConfigString("userid");
    }

    public String getVisitbillno()
    {
        return ReadConfigString("visitbillno");
    }

    private Boolean ReadConfigBoolean(String name)
    {
        SharedPreferences preferences = mContext.getSharedPreferences(
                "configure", Context.MODE_PRIVATE);
        return preferences.getBoolean(name, false);
    }

    public String ReadConfigString(String name)
    {
        SharedPreferences preferences = mContext.getSharedPreferences(
                "configure", Context.MODE_PRIVATE);
        return preferences.getString(name, "");
    }

    private void SaveConfigBoolean(String name, Boolean value)
    {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("configure",
                Context.MODE_PRIVATE).edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

    public void SaveConfigString(String name, String value)
    {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("configure",
                Context.MODE_PRIVATE).edit();
        editor.putString(name, value);
        editor.commit();
    }

    public void setClientId(String ClientId)
    {
        SaveConfigString("ClientId",ClientId);

    }


    // 保存服务地址
    public void setServerIp(String serverIp)
    {
        SaveConfigString("serverip", serverIp);
    }


    // 保存软件类型
    public void setSoftType(String type)
    {
        SaveConfigString("softtype", type);
    }

    // 保存企业ID
    public void setEnterpriseId(String id)
    {
        SaveConfigString("enterpriseid", id);
    }

    public void setServerport(String serverport)
    {
        SaveConfigString("serverport", serverport);
    }

    public void setApkUrl(String apkUrl)
    {
        SaveConfigString("apkUrl", apkUrl);
    }

    public void setBrand(String brand)
    {
        this.brand = brand;
        SaveConfigString("brand", brand);
    }

    public void setBrandurl(String brandurl)
    {
        SaveConfigString("brandurl", brandurl);
    }

//	public void setBusikind(String busikind)
//	{
//		SaveConfigString("busikind", busikind);
//	}

    public void setCity(String city)
    {
        SaveConfigString("city", city);
    }

    public void setCompanyid(String companyid)
    {
        SaveConfigString("companyid", companyid);
    }

    public void setDeviceid(String deviceid)
    {
        SaveConfigString("deviceid", deviceid);

    }

    public void setIfrember(Boolean ifrember)
    {
        SaveConfigBoolean("ifrember", ifrember);
    }

    public void setChange(Boolean change)
    {
        SaveConfigBoolean("change", change);
    }

    public void setClassname(String classname)
    {
        SaveConfigString("classname", classname);
    }

    public void setKefu(String kefu)
    {
        SaveConfigString("kefu", kefu);
    }


    public void setLoginid(String loginid)
    {
        SaveConfigString("loginid", loginid);
    }

    public void setMode(String Mode)
    {
        SaveConfigString("Mode", Mode);
    }
    /**
     * 设置记住密码后保存密码
     */
    public void setLoginpwd(String loginpwd)
    {
        SaveConfigString("loginpwd", loginpwd);
    }

    /**
     * 设置记住密码后保存账号
     */
    public void setMobile(String mobile)
    {
        SaveConfigString("mobile", mobile);
    }

    public void setProvince(String province)
    {

        SaveConfigString("province", province);
    }
    public void setFirstStart(String start)
    {
        SaveConfigString("start", start);
    }

    public void setQuyu(String quyu)
    {
        SaveConfigString("quyu", quyu);
    }

    public void setUpdate(Boolean update)
    {
        SaveConfigBoolean("update", update);
    }

    public void setUpdateMsg(String updateMsg)
    {
        SaveConfigString("updateMsg", updateMsg);
    }

    public void setUserid(String userid)
    {
        SaveConfigString("userid", userid);
    }
    public void setStock(String Stock)
    {
        SaveConfigString("Stock", Stock);
    }
    public void setVisitbillno(String visitbillno)
    {
        SaveConfigString("visitbillno", visitbillno);
    }

    public void setLastLoginServerIp(String lastLoginIp)
    {
        SaveConfigString("lastLoginIp", lastLoginIp);
    }
    public String getLastLoginServerIp()
    {
        return ReadConfigString("lastLoginIp");
    }

    public void setLastLoginBusinessid(String businessid)
    {
        SaveConfigString("lastBusinessid", businessid);
    }
    public String getLastLoginBusinessid()
    {
        return ReadConfigString("lastBusinessid");
    }

    /**
     * 是否要再下载产品明细
     * @param isDownload 不为空且不为null则下载
     */
    public void setIsDownload(boolean isDownload)
    {
        SaveConfigBoolean("isDownload", isDownload);
    }


    /**
     * 获取是否要再下载产品明细
     * @return
     */
    public boolean getIsDownload()
    {
        return ReadConfigBoolean("isDownload");
    }

    /**
     * 设置是否是新版本的软件
     * @param isNewVerSoft
     */
    public void setIsNewVerSoft(boolean isNewVerSoft)
    {
        SaveConfigBoolean("isNewVerSoft", isNewVerSoft);
    }

    /**
     * 获取该品牌是否是新版本的软件
     * 是新的才会有发货撤销和退货撤销功能，否则没有，
     * 在DSendGoodsMenuListActivity和DBackGoodsMenuListActivity里用来判断
     * @return
     */
    public boolean getIsNewVerSoft()
    {
        return ReadConfigBoolean("isNewVerSoft");
    }


    /**
     * 设置装盒入库每盒数量
     * @author van van.shu@magic-point.com
     * @version 创建时间：2018-1-12 上午9:41:05
     * @param packingNumber
     */
    public void setPackingNumber(String packingNumber)
    {
        SaveConfigString("packingNumber", packingNumber);
    }
    /**
     * 获取装盒每盒数量
     * @author van van.shu@magic-point.com
     * @version 创建时间：2018-1-12 上午9:41:18
     * @return
     */
    public int getPackingNumber()
    {
        if(ReadConfigString("packingNumber").isEmpty())
        {
            return 0;
        }else{
            return Integer.parseInt((ReadConfigString("packingNumber")));
        }
    }

    /**
     * 设置上次连接的蓝牙名称
     * @param deviceName
     */
    public void setConnectedBluetoothName(String deviceName)
    {
        SaveConfigString("deviceName", deviceName);
    }
    /**
     * 获取上次连接的蓝牙名称
     * @return
     */
    public String getConnectedBluetoothName()
    {
        return ReadConfigString("deviceName");
    }

    /**
     * 设置上次连接的蓝牙地址
     * @param deviceAddress
     */
    public void setConnectedBluetoothAddress(String deviceAddress)
    {
        SaveConfigString("deviceAddress", deviceAddress);
    }
    /**
     * 获取上次连接的蓝牙地址
     * @return
     */
    public String getConnectedBluetoothAddress()
    {
        return ReadConfigString("deviceAddress");
    }


}

