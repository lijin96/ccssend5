package com.holyes.ccssend5.utils;

import android.device.PrinterManager;

import com.holyes.ccssend5.lib.SortListMapComparator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: DetailPrintUtilss
 * @Description: 新机器的打印集合类
 * @Author: lijin
 * @Date: 2021/3/10 10:01
 */
public class DetailPrintUtilss {

        private int subStringIndex = 15;//换行截取的索引

        public DetailPrintUtilss() {
            //		printer = new PrinterManager();
        }


        /**
         * 打印功能
         * @title 打单抬头
         * @mark 定义在标题下面的内容，每一行为一个数组元素
         * @slist 型号色号数据
         * @sum 总计数量
         * @username 打单人
         * //ret +：表示打印之后换行
         * */
        public void print(String title, String[] mark,
                          List<Map<String, Object>> slist, String username) {
            //把集合数据先排序
            Collections.sort(slist, new SortListMapComparator("modelm"));

            PrinterManager printer = new PrinterManager();
            printer.prn_open();
            printer.prn_setupPage(384, -1);
            int ret = printer.prn_drawTextEx(title, 0, 0, -1, -1, "arial", 38,
                    0, 0, 0);


            for (int i = 0; i < mark.length; i++)
            {
                //如果含有“单”字就认为是单据项，都是字母或数字，一行的长度可以设置为25，
                //否则认为都是其他客户或者店名之类的都是汉字，长度要短点
                if(mark[i].contains("单"))
                {
                    subStringIndex = 25;//有
                }else{
                    subStringIndex = 15;
                }
                //如果超过截取字符串长度就截取换行打印
                if(mark[i].length()>subStringIndex)
                {
                    String str1="",str2="";
                    str1 = mark[i].substring(0, subStringIndex);
                    str2 = mark[i].substring(subStringIndex, mark[i].length());
                    //如果还超过截取字符串长度就再截取换行打印
                    if(str2.length()>subStringIndex)
                    {
                        //截两次打印3行
                        String str3 = "",str4="";
                        str3 = str2.substring(0, subStringIndex);
                        str4 = str2.substring(subStringIndex, str2.length());
                        ret +=printer.prn_drawTextEx(str1, 0, ret, -1, -1, "arial",
                                25, 0, 0, 0);
                        ret +=printer.prn_drawTextEx(str3, 0, ret, -1, -1, "arial",
                                25, 0, 0, 0);
                        ret +=printer.prn_drawTextEx(
                                str4, 0, ret, -1, -1,"arial", 25, 0, 0, 0);

                    }else{
                        //截一次打印两行
                        ret +=printer.prn_drawTextEx(str1, 0, ret, -1, -1, "arial",
                                25, 0, 0, 0);
                        ret +=printer.prn_drawTextEx(
                                str2, 0, ret, -1, -1,"arial", 25, 0, 0, 0);
                    }

                }else{
                    ret +=printer.prn_drawTextEx(mark[i], 0, ret, -1, -1, "arial",
                            25, 0, 0, 0);
                }
            }


            ret += printer.prn_drawTextEx(
                    "-------------------------------------", 0, ret, -1, -1,
                    "arial", 25, 0, 0, 0);
            printer.prn_drawTextEx(" 型号-色号 ", 0, ret, -1, -1, "arial",
                    25, 0, 0, 0);
            ret += printer.prn_drawTextEx("数量", 300, ret, -1, -1, "arial", 25,
                    0, 0, 0);
            ret += printer.prn_drawTextEx(
                    "-------------------------------------", 0, ret, -1, -1,
                    "arial", 25, 0, 0, 0);

            String modelm_colors = "",modelm_colors2="";
            int sum = 0;
            for (Map<String, Object> map : slist)
            {
                modelm_colors = map.get("modelm").toString() + "-"
                        + map.get("colors").toString();
                //如果型号色号长度超过16就要截取再分两行换行打印
                if(modelm_colors.length()>16)
                {
                    modelm_colors2 = modelm_colors.substring(16, modelm_colors.length());
                    modelm_colors = modelm_colors.substring(0, 16);
                    ret +=printer.prn_drawTextEx(modelm_colors, 0, ret, -1, -1, "arial",
                            25, 0, 0, 0);
                    printer.prn_drawTextEx(
                            modelm_colors2, 0, ret, -1, -1,"arial", 25, 0, 0, 0);
                }
                //否则直接
                else{
                    printer.prn_drawTextEx(modelm_colors, 0, ret, -1, -1, "arial",
                            25, 0, 0, 0);
                }

                ret += printer.prn_drawTextEx(map.get("curcount").toString(),
                        300, ret, -1, -1, "arial", 25, 0, 0, 0);
                sum = sum + Integer.valueOf(map.get("curcount").toString());
//			if (ret >= 1024) // 20行就打印
//			{
//				printer.prn_printPage(0);
//				printer.prn_clearPage();
//				ret = 0;
//			}
            }

            ret += printer.prn_drawTextEx(
                    "-------------------------------------", 0, ret, -1, -1,
                    "arial", 25, 0, 0, 0);
            ret += printer.prn_drawTextEx("合计：" + sum + "      打单:" + username,
                    0, ret, -1, -1, "arial", 25, 0, 0, 0);
            SimpleDateFormat sDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            ret += printer.prn_drawTextEx(
                    "日期时间：" + sDateFormat.format(new java.util.Date()), 0, ret,
                    -1, -1, "arial", 25, 0, 0, 0);
            ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                    0, 0);
            ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                    0, 0);
            ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                    0, 0);
            ret += printer.prn_drawTextEx(" ", 0, ret, -1, -1, "arial", 25, 0,
                    0, 0);
            printer.prn_printPage(0);
            printer.prn_close();

        }



        /**
         * 打印测试，这里打印出来的数据是长数据，会换行打印
         */
        public void printTest()
        {
            String[] mark = new String[3];
            mark[0] = "零售单：DF-BillNo12345";
            mark[1] = "分销单：LF-BillNo12345-87654321";
            mark[2] = "分销店：广东省深圳市龙华新区民治街道泰明工业区一栋三层B区分销测试店";

            List<Map<String, Object>> slist = new ArrayList<Map<String, Object>>();

            Map<String, Object> map = new HashMap<String, Object>();
//		 map.put("modelm", "B67100");
//		 map.put("colors", "P01");
//		 map.put("curcount", "5");
//		 map.put("product_id", "B67100-P01");
//		 slist.add(0, map);

            for (int i = 0; i < 5; i++) {
                map = new HashMap<String, Object>();
                map.put("modelm", "B67100"+i);
                map.put("colors", "B67100-P01");
                map.put("curcount", "3"+i);
                map.put("product_id", "B67100-P012");
                slist.add(map);
            }

            print("          分销店单据", mark, slist, "0001");
        }

    }


