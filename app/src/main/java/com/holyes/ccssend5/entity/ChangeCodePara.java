package com.holyes.ccssend5.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @ClassName: ChangeCodePara
 * @Description: 产品换标参数
 * @Author: lijin
 * @Date: 2021/3/6 14:14
 */
public class ChangeCodePara {
        private String NewBarcode;//新物流码(必传)
        private String OldCode;//旧的防伪码或旧的物流码(必传，此值由换码类型来决定，如果换码类型为1，则输入的是旧的防伪码；如果换码类型为0，则输入旧的物流码)
        private String ChangeCodeType;//换码类型(必传,值为：0或1)
        public String getNewBarcode() {
            return NewBarcode;
        }
        public void setNewBarcode(String newBarcode) {
            NewBarcode = newBarcode;
        }
        public String getOldCode() {
            return OldCode;
        }
        public void setOldCode(String oldCode) {
            OldCode = oldCode;
        }
        public String getChangeCodeType() {
            return ChangeCodeType;
        }
        public void setChangeCodeType(String changeCodeType) {
            ChangeCodeType = changeCodeType;
        }


        public ChangeCodePara() {
            super();
        }
        public ChangeCodePara(String newBarcode, String oldCode,
                              String changeCodeType) {
            super();
            NewBarcode = newBarcode;
            OldCode = oldCode;
            ChangeCodeType = changeCodeType;
        }
        @Override
        public String toString() {
            return "ChangeCodePara [NewBarcode=" + NewBarcode + ", OldCode="
                    + OldCode + ", ChangeCodeType=" + ChangeCodeType + "]";
        }

        public String toJson() throws JSONException {
            JSONObject js = new JSONObject();
            js.put("NewBarcode", getNewBarcode());
            js.put("OldCode", getOldCode());
            js.put("ChangeCodeType", getChangeCodeType());
            return js.toString();

        }


    }

