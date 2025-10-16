package com.holyes.ccssend5.lib.bluetooth;

/**
 * @ClassName: BoxTag
 * @Description: 打印的参数
 * @Author: lijin
 * @Date: 2021/3/6 14:25
 */
public class BoxTag {

    //BoxNo,Num,UserCode,BrandName,Model,Color,PackDate;
    private String BoxNo;//单号
    private String BrandName;//名称
    private String SerialName;//系列
    private String Model;//型号
    private String Color;//色号
    private String Num;//数量
    private String UserCode;//用户代号
    private String PackDate;//时间
    private String StockName;//仓库名称
    public String getBoxNo() {
        return BoxNo;
    }
    public void setBoxNo(String boxNo) {
        BoxNo = boxNo;
    }
    public String getUserCode() {
        return UserCode;
    }
    public void setUserCode(String userCode) {
        UserCode = userCode;
    }
    public String getBrandName() {
        return BrandName;
    }
    public void setBrandName(String brandName) {
        BrandName = brandName;
    }

    public String getSerialName() {
        return SerialName;
    }
    public void setSerialName(String serialName) {
        SerialName = serialName;
    }
    public String getModel() {
        return Model;
    }
    public void setModel(String model) {
        Model = model;
    }
    public String getColor() {
        return Color;
    }
    public void setColor(String color) {
        Color = color;
    }
    public String getNum() {
        return Num;
    }
    public void setNum(String num) {
        Num = num;
    }
    public String getPackDate() {
        return PackDate;
    }
    public void setPackDate(String packDate) {
        PackDate = packDate;
    }

    public String getStockName() {
        return StockName;
    }

    public void setStockName(String stockName) {
        StockName = stockName;
    }

    public BoxTag() {
        super();
    }
    public BoxTag(String boxNo, String brandName, String serialName,
                  String model, String color, String num, String userCode,
                  String packDate) {
        super();
        BoxNo = boxNo;
        BrandName = brandName;
        SerialName = serialName;
        Model = model;
        Color = color;
        Num = num;
        UserCode = userCode;
        PackDate = packDate;
    }
    public BoxTag(String boxNo, String brandName, String serialName,
                  String model, String color, String num, String userCode,
                  String packDate,String stockName) {
        super();
        BoxNo = boxNo;
        BrandName = brandName;
        SerialName = serialName;
        Model = model;
        Color = color;
        Num = num;
        UserCode = userCode;
        PackDate = packDate;
        StockName=stockName;
    }
    @Override
    public String toString() {
        return "BoxTag [BoxNo=" + BoxNo + ", BrandName=" + BrandName
                + ", SerialName=" + SerialName + ", Model=" + Model
                + ", Color=" + Color + ", Num=" + Num + ", UserCode="
                + UserCode + ", PackDate=" + PackDate + "]";
    }



}

