package cn.mipos.printer;

import android.graphics.Bitmap;

public interface IPrinter {

    //中文简体
    String GB2312 = "GB2312";

    //普通字体
    int FONT_NORMAL = 1;

    //大号字体
    int FONT_LARGE = 2;

    //中号字体
    int FONT_MEDIUM = 3;

    //左对齐
    int ALIGN_LEFT = 0;

    //中对齐
    int ALIGN_CENTER = 1;

    //右对齐
    int ALIGN_RIGHT = 2;

    /**
     * 开始连接
     * @return
     */
    void connect() throws Exception;

    /**
     * 关闭连接
     */
    void disconnect();

    /**
     * 设置字体（默认，大号）
     * @param font
     */
    void setFont(int font) throws Exception;

    /**
     * 设置对齐方式（左对齐，中对齐，右对齐）
     * @param align
     */
    @Deprecated
    void setAlign(int align) throws Exception;

    /**
     * 写入字节数据
     * @param text
     * @throws Exception
     */
    void write(byte[] text) throws Exception;

    /**
     * 写到打印纸的中间
     * @param text
     */
    void writeCenter(String text) throws Exception;

    /**
     * 写到打印纸的中间
     * @param text
     */
    void writeCenter(String text, boolean addLine) throws Exception;

    /**
     * 写一条线
     */
    void writeLine() throws Exception;

    /**
     * 向下走纸一行
     */
    void nextLine() throws Exception;

    /**
     * 写一行
     * @param text
     */
    void writeln(String text) throws Exception;

    /**
     * 写一行，左右格式
     * @param leftText
     * @param rightText
     */
    void writeln(String leftText, String rightText) throws Exception;

    /**
     * 写一行，表格格式,默认左对齐
     * @param arrStr 数据
     * @param arrWidth 一行的比例
     */
    void writeln(String[] arrStr, int[] arrWidth) throws Exception;

    /**
     * 写一行，表格格式
     * @param arrStr 数据
     * @param arrWidth 一行的比例
     * @param arrAlign 每列的对齐方式
     */
    void writeln(String[] arrStr, int[] arrWidth, int[] arrAlign) throws Exception;

    /**
     * 切纸
     */
    void cut() throws Exception;

    /**
     * 打印二维码
     * @param url
     * @throws Exception
     */
    void printCode(String url) throws Exception;

    void printCode(String url, int width, int height) throws Exception;

    /**
     * 开钱箱
     * @throws Exception
     */
    void openCashBox() throws Exception;

    /**
     * 打印图片
     * @param filePath 本地路径
     * @throws Exception
     */
    void printImage(String filePath, int width, int height) throws Exception;

    /**
     * 打印图片
     * @param bitmap
     * @throws Exception
     */
    void printBitmap(Bitmap bitmap) throws Exception;

    /**
     * 打印条码
     * @param barcode barcode
     * @throws Exception
     */
    void printBarcode(String barcode, int width, int height) throws Exception;

    /**
     * 打印条码
     * @throws Exception
     */
    void printBarcode(String barcode) throws Exception;
}
