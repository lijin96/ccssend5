package cn.mipos.printer;

/**
 * 想米打印机错误枚举
 */

public enum MiposPrinterErrorEnum {
    OK(0, "成功"),
    UNKNOWN_ERR(-1, "打印机未知错误"),
    DEVICE_NOT_FOUND(-401, "无法找到打印机设备"),
    OVER_HEAT(-402, "打印机过热"),
    OUT_PAPER(-403, "打印机缺纸"),
    DATA_ERR(-404, "打印机数据错误"),
    FORCE_STOP(-405, "打印机数据错误"),
    SET_POWER_MODE_ERR(-406, "打印机已强制停止"),
    SET_HEAT_TIME_ERR(-407, "打印机设置电源模式错误"),
    POWER_STATUS_ERR(-408, "打印机设置加热时间错误"),
    LOW_BATTERY(-409, "电池电量过低，无法打印"),
    ;

    private int code;

    private String desc;

    MiposPrinterErrorEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode(){
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDescByCode(int code){
        for(MiposPrinterErrorEnum item : values()){
            if(item.code == code){
                return item.getDesc();
            }
        }

        return UNKNOWN_ERR.getDesc();
    }
}
