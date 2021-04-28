package com.holyes.ccssend5.entity;

/**
 * @ClassName: UpdateInfo
 * @Description: 更新信息实体类
 * @Author: lijin
 * @Date: 2021/3/6 14:18
 */
public class UpdateInfo {

        private String version; // 版本号

        private String url; // 新版本存放url路径

        private String description; // 更新说明信息，比如新增什么功能特性等

        private String brand = ""; //品牌

        private String ifupdate = "true";


        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getIfupdate() {
            return ifupdate;
        }

        public void setIfupdate(String ifupdate) {
            this.ifupdate = ifupdate;
        }


    }

