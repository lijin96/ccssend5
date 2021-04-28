package com.holyes.ccssend5.lib;

import java.util.Comparator;
import java.util.Map;

/**
 * @ClassName: SortListMapComparator
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:29
 */
public class SortListMapComparator implements Comparator<Map<String,Object>>{
        private String sortRule;


        public SortListMapComparator(String sortRule) {
            this.sortRule = sortRule;
        }

        @Override
        public int compare(Map<String, Object> map1, Map<String, Object> map2) {
            if(sortRule.equals("modelm"))
            {
                //modelm不相同比较modelm
                if(!map1.get("modelm").toString().equals(map2.get("modelm").toString())){
                    return map1.get("modelm").toString().compareToIgnoreCase(map2.get("modelm").toString());
                }
                //modelm相同，比较colors
                else{
                    return map1.get("colors").toString().compareToIgnoreCase(map2.get("colors").toString());
                }
            }else if(sortRule.equals("molen")){
                //modelm不相同比较modelm
                if(!map1.get("molen").toString().equals(map2.get("molen").toString())){
                    return map1.get("molen").toString().compareToIgnoreCase(map2.get("molen").toString());
                }
                //modelm相同，比较colors
                else{
                    return map1.get("color").toString().compareToIgnoreCase(map2.get("color").toString());
                }
            }else if(sortRule.equals("stock_id"))
            {
                //仓库ID不相同比较仓库ID
                if(!map1.get("stock_id").toString().equals(map2.get("stock_id").toString())){
                    return map1.get("stock_id").toString().compareToIgnoreCase(map2.get("stock_id").toString());
                }
                //仓库ID相同比较仓库名称
                else{
                    return map1.get("stock_name").toString().compareToIgnoreCase(map2.get("stock_name").toString());
                }
            }
            else  if(sortRule.equals("agentname"))
            {
                //代理商名称不相同比较代理商名称
                if(!map1.get("agentname").toString().equals(map2.get("agentname").toString())){
                    return map1.get("agentname").toString().compareToIgnoreCase(map2.get("agentname").toString());
                }
                //代理商名称相同比较联系人
                else{
                    return map1.get("link").toString().compareToIgnoreCase(map2.get("link").toString());
                }
            }else if(sortRule.equals("supplier_id"))
            {
                //供应商代号不相同比较供应商代号
                if(!map1.get("supplier_id").toString().equals(map2.get("supplier_id").toString())){
                    return map1.get("supplier_id").toString().compareToIgnoreCase(map2.get("supplier_id").toString());
                }
                //供应商代号相同比较供应商名称
                else{
                    return map1.get("supplier_name").toString().compareToIgnoreCase(map2.get("supplier_name").toString());
                }
            }
            else if(sortRule.equals("purchecklno"))
            {
                //品检单号（配货单号）不相同比较品检单号（配货单号）
                if(!map1.get("purchecklno").toString().equals(map2.get("purchecklno").toString())){
                    return map1.get("purchecklno").toString().compareToIgnoreCase(map2.get("purchecklno").toString());
                }
                //品检单号（配货单号）相同比较供应商（代理商）
                else{
                    return map1.get("supplier_na").toString().compareToIgnoreCase(map2.get("supplier_na").toString());
                }
            }
            else if(sortRule.equals("allotlno"))
            {
                //调拨单号不相同比较调拨单号
                if(!map1.get("allotlno").toString().equals(map2.get("allotlno").toString())){
                    return map1.get("allotlno").toString().compareToIgnoreCase(map2.get("allotlno").toString());
                }
                //调拨单号相同比较调出仓
                else{
                    return map1.get("outstockname").toString().compareToIgnoreCase(map2.get("outstockname").toString());
                }
            }
            else { //if(sortRule.equals("product_id")
                return map1.get(sortRule).toString().compareToIgnoreCase(map2.get(sortRule).toString());
            }
        }
    }

