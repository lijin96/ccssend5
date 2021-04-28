package com.holyes.ccssend5.entity;

import java.util.ArrayList;

/**
 * @ClassName: MenuGroup
 * @Description: 菜单项头类
 * @Author: lijin
 * @Date: 2021/3/6 14:14
 */
public class MenuGroup {

        private String menuCode;//菜单编号id,
        private String title;//选项头的名，菜单名称
        private boolean isChecked;//是否勾选了全选
        private ArrayList<MenuChild> children;//一个选项头下面的子选项集合

        public MenuGroup(String menuCode, String title,boolean isChecked) {
            super();
            this.menuCode = menuCode;
            this.title = title;
            this.isChecked=isChecked;
            this.children=new ArrayList<MenuChild>();
        }

        public String getMenuCode() {
            return menuCode;
        }

        public void setMenuCode(String menuCode) {
            this.menuCode = menuCode;
        }


        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean getChecked() {
            return isChecked;
        }
        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }
        /**
         * 切换选项头状态，当全选时调用
         */
        public void toggle() {
            this.isChecked = !this.isChecked;
        }

        public void addChildrenItem(MenuChild child) {
            children.add(child);
        }

        public int getChildrenCount() {
            return children.size();
        }

        public MenuChild getChildrenItem(int index) {
            return children.get(index);
        }

        @Override
        public String toString() {
            return "MenuGroup [menuCode=" + menuCode + ", title=" + title
                    + ", isChecked=" + isChecked + ", children=" + children + "]";
        }


    }

