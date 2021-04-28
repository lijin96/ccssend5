package com.holyes.ccssend5.entity;

import java.io.Serializable;

/**
 * @ClassName: StoreInfor
 * @Description: 分销商，分销店
 * @Author: lijin
 * @Date: 2021/3/6 14:18
 */
public class StoreInfor implements Serializable{
        private static final long serialVersionUID = 1L;
        private String StoreId;//分销店代号
        private String StoreName;//分销店名称
        private String Link;//联系人
        private String Tel;//电话
        private String CorpAddr;//地址
        private String TraderId;//零售店代号（上级代号）


        public String getStoreId() {
            return StoreId;
        }
        public void setStoreId(String storeId) {
            StoreId = storeId;
        }
        public String getStoreName() {
            return StoreName;
        }
        public void setStoreName(String storeName) {
            StoreName = storeName;
        }
        public String getLink() {
            return Link;
        }
        public void setLink(String link) {
            Link = link;
        }
        public String getTel() {
            return Tel;
        }
        public void setTel(String tel) {
            Tel = tel;
        }
        public String getCorpAddr() {
            return CorpAddr;
        }
        public void setCorpAddr(String corpAddr) {
            CorpAddr = corpAddr;
        }
        public String getTraderId() {
            return TraderId;
        }
        public void setTraderId(String traderId) {
            TraderId = traderId;
        }

        public StoreInfor() {
            super();
            // TODO Auto-generated constructor stub
        }
        public StoreInfor(String storeId, String storeName, String link,
                          String tel, String corpAddr, String traderId) {
            super();
            StoreId = storeId;
            StoreName = storeName;
            Link = link;
            Tel = tel;
            CorpAddr = corpAddr;
            TraderId = traderId;
        }
        @Override
        public String toString() {
            return "StoreInfor [StoreId=" + StoreId + ", StoreName=" + StoreName
                    + ", Link=" + Link + ", Tel=" + Tel + ", CorpAddr=" + CorpAddr
                    + ", TraderId=" + TraderId + "]";
        }


    }

