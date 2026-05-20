package com.holyes.ccssend5.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @ClassName: Para
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:17
 */
public class Para {
    private String Barcode;//扫描的条码(必传)
    private String GoodsId;//入库的产品(必传)
    private String SoCompId;//供应商代号(必传)
    private String StoreId;//分店代号
    private String DeCompId;//总公司代号(固定值：00)
    private String OaSuserId;//扫描人员代号(必传)
    private String StockId;//仓库代号(必传)
    private String ScanSn;//扫描序号(必传)
    private String ScanBillNo;//扫描单号(必传)
    private String BillNo;//入库单号(首次扫码传空，成功再扫码时传返回的入库单号)--入库退回单号(首次扫码传空，成功再扫码时传返回的入库单号)
    private String SourceBillNo;//来源单号(品检入库单号)(必传)
    private String DocumentNo;//入库单号(首次扫码传空，成功再扫码时传返回的入库单号)
    private String  FirstDelivery;//是否首次发货(0-非首次；1-首次)
    private String  Batchno;//金大合需求新增批次号

    private String RetailerId;//零售商代号
    private String OutBoxCode;//发货装箱条码(非必传)
//

    public String getBarcode() {
        return Barcode;
    }
    public String getDocumentNo() {
        return DocumentNo;
    }
    public void setDocumentNo(String documentNo) {
        DocumentNo = documentNo;
    }
    public void setBarcode(String barcode) {
        Barcode = barcode;
    }
    public String getGoodsId() {
        return GoodsId;
    }
    public void setGoodsId(String goodsId) {
        GoodsId = goodsId;
    }
    public String getSoCompId() {
        return SoCompId;
    }
    public void setSoCompId(String soCompId) {
        SoCompId = soCompId;
    }
    public String getDeCompId() {
        return DeCompId;
    }
    public void setDeCompId(String deCompId) {
        DeCompId = deCompId;
    }
    public String getOaSuserId() {
        return OaSuserId;
    }
    public void setOaSuserId(String oaSuserId) {
        OaSuserId = oaSuserId;
    }
    public String getStockId() {
        return StockId;
    }
    public void setStockId(String stockId) {
        StockId = stockId;
    }
    public String getScanSn() {
        return ScanSn;
    }
    public void setScanSn(String scanSn) {
        ScanSn = scanSn;
    }
    public String getScanBillNo() {
        return ScanBillNo;
    }
    public void setScanBillNo(String scanBillNo) {
        ScanBillNo = scanBillNo;
    }
    public String getBillNo() {
        return BillNo;
    }
    public void setBillNo(String billNo) {
        BillNo = billNo;
    }
    public String getSourceBillNo() {
        return SourceBillNo;
    }
    public void setSourceBillNo(String sourceBillNo) {
        SourceBillNo = sourceBillNo;
    }
    public String getStoreId() {
        return StoreId;
    }
    public void setStoreId(String storeId) {
        StoreId = storeId;
    }
    public String getFirstDelivery() {
        return FirstDelivery;
    }
    public void setFirstDelivery(String firstDelivery) {
        FirstDelivery = firstDelivery;
    }

    public String getBatchno() {
        return Batchno;
    }

    public String getRetailerId() {
        return RetailerId;
    }

    public void setRetailerId(String retailerId) {
        RetailerId = retailerId;
    }

    public String getOutBoxCode() {
        return OutBoxCode;
    }

    public void setOutBoxCode(String outBoxCode) {
        OutBoxCode = outBoxCode;
    }

    public void setBatchno(String batchno) {
        Batchno = batchno;
    }

    public Para() {
        super();
    }
    public Para(String barcode, String goodsId, String soCompId,
                String deCompId, String oaSuserId, String stockId, String scanSn,
                String scanBillNo, String billNo, String sourceBillNo) {
        super();
        Barcode = barcode;
        GoodsId = goodsId;
        SoCompId = soCompId;
        DeCompId = deCompId;
        OaSuserId = oaSuserId;
        StockId = stockId;
        ScanSn = scanSn;
        ScanBillNo = scanBillNo;
        BillNo = billNo;
        SourceBillNo = sourceBillNo;
    }

    public Para(String barcode, String goodsId, String soCompId, String storeId, String deCompId, String oaSuserId, String stockId, String scanSn, String scanBillNo, String billNo, String sourceBillNo, String documentNo, String firstDelivery, String batchno) {
        Barcode = barcode;
        GoodsId = goodsId;
        SoCompId = soCompId;
        StoreId = storeId;
        DeCompId = deCompId;
        OaSuserId = oaSuserId;
        StockId = stockId;
        ScanSn = scanSn;
        ScanBillNo = scanBillNo;
        BillNo = billNo;
        SourceBillNo = sourceBillNo;
        DocumentNo = documentNo;
        FirstDelivery = firstDelivery;
        Batchno = batchno;
    }

    @Override
    public String toString() {
        return "Para [Barcode=" + Barcode + ", GoodsId=" + GoodsId
                + ", SoCompId=" + SoCompId + ", DeCompId=" + DeCompId
                + ", OaSuserId=" + OaSuserId + ", StockId=" + StockId
                + ", ScanSn=" + ScanSn + ", ScanBillNo=" + ScanBillNo
                + ", BillNo=" + BillNo + ", SourceBillNo=" + SourceBillNo + "]";
    }


    public String toJson() throws JSONException {
        JSONObject js = new JSONObject();
        js.put("Barcode", getBarcode());
        js.put("GoodsId", getGoodsId());
        js.put("SoCompId", getSoCompId());
        js.put("DeCompId", getDeCompId());
        js.put("OaSuserId", getOaSuserId());
        js.put("StockId", getStockId());
        js.put("ScanSn", getScanSn());
        js.put("ScanBillNo", getScanBillNo());
        js.put("BillNo", getBillNo());
        js.put("SourceBillNo", getSourceBillNo());
        js.put("DocumentNo", getDocumentNo());
        js.put("StoreId", getStoreId());
        js.put("FirstDelivery", getFirstDelivery());
        js.put("BatchNo", getBatchno());
        js.put("RetailerId", getRetailerId());
        js.put("OutBoxCode", getOutBoxCode());

        return js.toString();
    }


}
