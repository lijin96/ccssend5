package com.holyes.ccssend5.entity;

import java.util.List;

/**
 * @ClassName: Retailer
 * @Description: 零售商，选择分销店的时候用到
 * @Author: lijin
 * @Date: 2021/3/6 14:17
 */
public class Retailer {

        private String TraderId;//零售商代号
        private String TraderName;//零售商名称
        private List<StoreInfor> storeInforList;//零售商下面分销店的集合
        public String getTraderId() {
            return TraderId;
        }
        public void setTraderId(String traderId) {
            TraderId = traderId;
        }
        public String getTraderName() {
            return TraderName;
        }
        public void setTraderName(String traderName) {
            TraderName = traderName;
        }


        public List<StoreInfor> getStoreInforList() {
            return storeInforList;
        }
        public void setStoreInforList(List<StoreInfor> storeInforList) {
            this.storeInforList = storeInforList;
        }
        public Retailer() {
            super();
        }


        public Retailer(String traderId, String traderName,
                        List<StoreInfor> storeInforList) {
            super();
            TraderId = traderId;
            TraderName = traderName;
            this.storeInforList = storeInforList;
        }

        public int getChildrenCount() {
            return storeInforList.size();
        }

        public StoreInfor getChildrenItem(int index) {
            return storeInforList.get(index);
        }






    }

