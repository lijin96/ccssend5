package cn.mipos.service;

/**
 * Created by jarry on 16/12/2.
 */

public class ServiceError {
    public static final int ERR_OKAY                      = 0;
    public static final int ERR_IBEACON_ERROR             = -201;
    public static final int ERR_IBEACON_DATA_ERROR        = -202;
    public static final int ERR_IBEACON_UART_IS_NULL      = -203;
    public static final int ERR_IBEACON_SEND_FAILURE      = -204;
    public static final int ERR_IBEACON_RECV_PACKET_HEADER= -205;
    public static final int ERR_IBEACON_RECV_CHECK        = -206;
    public static final int ERR_IBEACON_NOT_ACK           = -207;

    public static final int ERR_CALLERID_ERROR            = -302;
    public static final int ERR_CALLERID_UART_IS_NULL     = -303;
    public static final int ERR_CALLERID_RECV_LESS_CHAR   = -304;
    public static final int ERR_CALLERID_RECV_MORE_CHAR   = -305;
    public static final int ERR_CALLERID_RECV_CHECK       = -306;
    public static final int ERR_CALLERID_RECV_NUM_LEN     = -307;

    public static final int ERR_DRIVER_ERROR              = -400;
    public static final int ERR_PRINTER_DEVICE_NOT_FOUND  = -401;
    public static final int ERR_PRINTER_RS_OVER_HEAT      = -402;
    public static final int ERR_PRINTER_RS_OUT_PAPER      = -403;
    public static final int ERR_PRINTER_RS_DATA_ERR       = -404;
    public static final int ERR_PRINTER_RS_FORCE_STOP     = -405;

    public static final int ERR_OBJECT_NOT_FOUND          = -900;


}
