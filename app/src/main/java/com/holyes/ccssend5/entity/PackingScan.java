package com.holyes.ccssend5.entity;

import java.io.Serializable;

/**
 * @ClassName: PackingScan
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:16
 */
public class PackingScan implements Serializable{

        private static final long serialVersionUID = 1L;
        private String product_id;
        private String modelm;
        private String colors;
        private String barcode;
        private String tempboxcode;
        private String boxcode;
        private String scantime;
        public String getProduct_id() {
            return product_id;
        }
        public void setProduct_id(String product_id) {
            this.product_id = product_id;
        }
        public String getModelm() {
            return modelm;
        }
        public void setModelm(String modelm) {
            this.modelm = modelm;
        }
        public String getColors() {
            return colors;
        }
        public void setColors(String colors) {
            this.colors = colors;
        }
        public String getBarcode() {
            return barcode;
        }
        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }
        public String getTempboxcode() {
            return tempboxcode;
        }
        public void setTempboxcode(String tempboxcode) {
            this.tempboxcode = tempboxcode;
        }
        public String getBoxcode() {
            return boxcode;
        }
        public void setBoxcode(String boxcode) {
            this.boxcode = boxcode;
        }
        public String getScantime() {
            return scantime;
        }
        public void setScantime(String scantime) {
            this.scantime = scantime;
        }
        public PackingScan() {
            super();
        }
        public PackingScan(String product_id, String modelm, String colors,
                           String barcode, String tempboxcode, String boxcode, String scantime) {
            super();
            this.product_id = product_id;
            this.modelm = modelm;
            this.colors = colors;
            this.barcode = barcode;
            this.tempboxcode = tempboxcode;
            this.boxcode = boxcode;
            this.scantime = scantime;
        }
        @Override
        public String toString() {
            return "PackingScan [product_id=" + product_id + ", modelm=" + modelm
                    + ", colors=" + colors + ", barcode=" + barcode
                    + ", tempboxcode=" + tempboxcode + ", boxcode=" + boxcode
                    + ", scantime=" + scantime + "]";
        }



    }

