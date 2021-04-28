package com.holyes.ccssend5.lib;

import java.text.Collator;
import java.util.Comparator;
import java.util.Map;

/**
 * @ClassName: SortCompanyZyComparator
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:28
 */
public class SortCompanyZyComparator  implements Comparator<Map<String,Object>>{

        private String sortRule;
        private Collator collator =  Collator.getInstance(java.util.Locale.CHINA);

        public SortCompanyZyComparator(String sortRule) {
            this.sortRule = sortRule;
        }

        @Override
        public int compare(Map<String, Object> map1, Map<String, Object> map2) {
            if(sortRule.equals("tradername"))
            {
                if(!map1.get("tradername").toString().equals(map2.get("tradername").toString())){
                    return collator.compare(map1.get("tradername").toString(), map2.get("tradername").toString());
                }
                else{
                    return map1.get("link").toString().compareToIgnoreCase(map2.get("link").toString());
                }
            }else {
                return collator.compare(map1.get("tradername").toString(), map2.get("tradername").toString());
            }
        }
    }

