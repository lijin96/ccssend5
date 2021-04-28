package com.holyes.ccssend5.lib;

import com.holyes.ccssend5.entity.MenuGroup;

import java.util.Comparator;

/**
 * @ClassName: SortMenuListComparator
 * @Description: 比较器，把要集合的的数据进行排序，目前用在查看明细和打印时
 * @Author: lijin
 * @Date: 2021/3/6 14:29
 */
public class SortMenuListComparator implements Comparator<MenuGroup> {


        public SortMenuListComparator() {
            super();
        }

        @Override
        public int compare(MenuGroup map1, MenuGroup map2) {
            return map1.getMenuCode().compareTo(map2.getMenuCode());
        }
    }

