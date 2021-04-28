package com.holyes.ccssend5.entity;

/**
 * @ClassName: User
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:18
 */
public class User {

        //State  登录状态
        private String p00;
        //LoginRemark 登录信息
        private String p01;
        //LoginId  登录ID
        private String p02;
        //UserName  用户名称
        private String p03;
        //ParentNa  操作人（仓库，业务，客服）
        private String p04;
        //Remark   公司类型（代理商，零售商，直营店，区域）
        private String p05;
        //ParentId  公司ID
        private String p06;


        public String getP00() {
            return p00;
        }
        public void setP00(String p00) {
            this.p00 = p00;
        }

        public String getP01() {
            return p01;
        }
        public void setP01(String p01) {
            this.p01 = p01;
        }
        public String getP02() {
            return p02;
        }
        public void setP02(String p02) {
            this.p02 = p02;
        }
        public String getP03() {
            return p03;
        }
        public void setP03(String p03) {
            this.p03 = p03;
        }
        public String getP04() {
            return p04;
        }
        public void setP04(String p04) {
            this.p04 = p04;
        }
        public String getP05() {
            return p05;
        }
        public void setP05(String p05) {
            this.p05 = p05;
        }
        public String getP06() {
            return p06;
        }
        public void setP06(String p06) {
            this.p06 = p06;
        }
        @Override
        public String toString() {
            return "User [p00=" + p00 + ", p01=" + p01 + ", p02=" + p02 + ", p03="
                    + p03 + ", p04=" + p04 + ", p05=" + p05 + ", p06=" + p06 + "]";
        }

    }

