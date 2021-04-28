package com.holyes.ccssend5.entity;

/**
 * @ClassName: PackPara
 * @Description: 套装批量上传参数
 * @Author: lijin
 * @Date: 2021/3/6 14:17
 */
public class PackPara {

        private String PackId;//：套餐代号(必传)
        private String Barcode;//：扫描的条码(必传)
        private String OaSuserId;//：扫描人员代号(必传)
        /**
         * @return the packId
         */
        public String getPackId() {
            return PackId;
        }
        /**
         * @param packId the packId to set
         */
        public void setPackId(String packId) {
            PackId = packId;
        }
        /**
         * @return the barcode
         */
        public String getBarcode() {
            return Barcode;
        }
        /**
         * @param barcode the barcode to set
         */
        public void setBarcode(String barcode) {
            Barcode = barcode;
        }
        /**
         * @return the oaSuserId
         */
        public String getOaSuserId() {
            return OaSuserId;
        }
        /**
         * @param oaSuserId the oaSuserId to set
         */
        public void setOaSuserId(String oaSuserId) {
            OaSuserId = oaSuserId;
        }
        /**
         * @param packId
         * @param barcode
         * @param oaSuserId
         */
        public PackPara(String packId, String barcode, String oaSuserId) {
            super();
            PackId = packId;
            Barcode = barcode;
            OaSuserId = oaSuserId;
        }
        /**
         *
         */
        public PackPara() {
            super();
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "PackPara [PackId=" + PackId + ", Barcode=" + Barcode
                    + ", OaSuserId=" + OaSuserId + "]";
        }


    }

