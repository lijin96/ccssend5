package com.holyes.ccssend5.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @ClassName: Para
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2024年7月5日17:36:33
 */
public class UploadPara {
    private String OaSuserId;//	操作员代号	是		string
    private String  Purchecklno;//	采购品检单号	是		string
    private String  ProductId;//	产品代号	是		string
    private String Diopter;//	球镜	是		string
    private String Astigmatism;//	柱镜	是		string
    private int Checkqty;//	采购数量	是		int

    public String getOaSuserId() {
        return OaSuserId;
    }

    public void setOaSuserId(String oaSuserId) {
        OaSuserId = oaSuserId;
    }

    public String getPurchecklno() {
        return Purchecklno;
    }

    public void setPurchecklno(String purchecklno) {
        Purchecklno = purchecklno;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getDiopter() {
        return Diopter;
    }

    public void setDiopter(String diopter) {
        Diopter = diopter;
    }

    public String getAstigmatism() {
        return Astigmatism;
    }

    public void setAstigmatism(String astigmatism) {
        Astigmatism = astigmatism;
    }

    public int getCheckqty() {
        return Checkqty;
    }

    public void setCheckqty(int checkqty) {
        Checkqty = checkqty;
    }

    @Override
    public String toString() {
        return "UploadPara{" + "OaSuserId='" + OaSuserId + '\'' + ", Purchecklno='" + Purchecklno + '\'' + ", ProductId='" + ProductId + '\'' + ", Diopter='" + Diopter + '\'' + ", Astigmatism='" + Astigmatism + '\'' + ", Checkqty=" + Checkqty + '}';
    }

    public UploadPara(String oaSuserId, String purchecklno, String productId, String diopter,
                      String astigmatism, int checkqty) {
        OaSuserId = oaSuserId;
        Purchecklno = purchecklno;
        ProductId = productId;
        Diopter = diopter;
        Astigmatism = astigmatism;
        Checkqty = checkqty;
    }

    public UploadPara() {
    }


    public String toJson() throws JSONException {
        JSONObject js = new JSONObject();
        js.put("OaSuserId", getOaSuserId());
        js.put("Purchecklno", getPurchecklno());
        js.put("ProductId", getProductId());
        js.put("Diopter", getDiopter());
        js.put("Astigmatism", getAstigmatism());
        js.put("Checkqty", getCheckqty());
        return js.toString();

    }


}
