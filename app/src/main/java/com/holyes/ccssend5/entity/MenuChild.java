package com.holyes.ccssend5.entity;

/**
 * @ClassName: MenuChild
 * @Description: 菜单子选项类
 * @Author: lijin
 * @Date: 2021/3/6 14:14
 */
public class MenuChild {

        private String menucode;//菜单编号
        private String menuname;//菜单名称
        private boolean isChecked;//是否选择了该选项，对应menus表中的showstatus，1为true,0为false

        public MenuChild(String menucode, String menuname, boolean isChecked) {
            super();
            this.menucode = menucode;
            this.menuname = menuname;
            this.isChecked = isChecked;
        }

        public String getMenucode() {
            return menucode;
        }

        public void setMenucode(String menucode) {
            this.menucode = menucode;
        }

        public String getMenuname() {
            return menuname;
        }


        public void setMenuname(String menuname) {
            this.menuname = menuname;
        }


        public boolean getChecked() {
            return isChecked;
        }
        /**
         * 切换选项状态，当选择选项时调用
         */
        public void toggle() {
            this.isChecked = !this.isChecked;
        }
        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }

        @Override
        public String toString() {
            return "MenuChild [menucode=" + menucode + ", menuname=" + menuname
                    + ", isChecked=" + isChecked + "]";
        }



    }

