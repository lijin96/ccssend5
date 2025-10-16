package cn.mipos.printer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 小票打印机（通过图片的方式打印）
 */

public class BasePrinter implements IPrinter{

    protected static final String LINE = "-";

    protected static final String BLANK = " ";

    protected int mFont = FONT_NORMAL;

    protected int mAlign = ALIGN_LEFT;

    protected int mTotalWord = 32;

    protected String mCharset = "UTF-8";

    protected String mBlank = BLANK;

    protected final static int QR_WIDTH = 300;

    protected final static int QR_HEIGHT = 300;

    protected final static int BARCODE_WIDTH = 300;

    protected final static int BARCODE_HEIGHT = 81;

    private static final String LANG_UG = "ug";

    private static final int LINE_H = 0;

    /** 普通字体 */
    private static final int DEFAULT_FONT_SIZE = 24;

    /** 中号字体 */
    private static final int DEFAULT_FONT_MEDIUM = 32;

    /** 大号字体 */
    private static final int DEFAULT_FONT_LARGE_SIZE = 48;

    protected static int mPrintMaxLine = 30;

    private Context mContext;
    
    private List<View> mPrintViewList = new ArrayList<>();

    private boolean mIsUyghur = false;

    public BasePrinter(Context context){
        mContext = context;
        mCharset = "UTF-8";
        mIsUyghur = LANG_UG.equals(getLang());
    }

    @Override
    public void setAlign(int align) throws Exception {
        mAlign = align;
    }

    @Override
    public void setFont(int font) throws Exception {
        mFont = font;
    }

    @Override
    public void connect() throws Exception {
        mPrintViewList.clear();
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void writeLine() throws Exception {
        int pagerWidth = getPagerWidth();
        TextView textView = newTextView();
        textView.setText(getLine(getTotalLen() * 2));
        textView.setLines(1);
        textView.layout(0, 0, pagerWidth, getTextViewHeight(textView, pagerWidth));
        mPrintViewList.add(textView);
    }


    @Override
    public void write(byte[] data) throws Exception {
        int pagerWidth = getPagerWidth();
        TextView textView = newTextView();
        textView.setText(new String(data, mCharset));
        textView.layout(0, 0, pagerWidth, getTextViewHeight(textView, pagerWidth));
        mPrintViewList.add(textView);
    }

    protected void write(String text) throws Exception {
        int pagerWidth = getPagerWidth();
        TextView textView = newTextView();
        textView.setText(text);
        textView.layout(0, 0, pagerWidth, getTextViewHeight(textView, pagerWidth));
        mPrintViewList.add(textView);
    }

    @Override
    public void writeln(String text) throws Exception {
        write(text);
    }

    @Override
    public void writeln(String leftText, String rightText) throws Exception {
        int pagerWidth = getPagerWidth();
        TextView textView = newTextView(ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setText(leftText);
        int leftTextWidth = getTextViewWidth(textView, ViewGroup.LayoutParams.WRAP_CONTENT);

        textView.setText(rightText);
        int rightTextWidth = getTextViewWidth(textView, ViewGroup.LayoutParams.WRAP_CONTENT);

        if(leftTextWidth + rightTextWidth > pagerWidth){
            if(leftTextWidth > rightTextWidth){
                if(rightTextWidth > toInt(pagerWidth / 2F)){
                    leftTextWidth = 1;
                    rightTextWidth = 1;
                }else {
                    leftTextWidth = pagerWidth - rightTextWidth;
                }

            }else if(leftTextWidth < rightTextWidth){
                if(leftTextWidth > toInt(pagerWidth / 2F)){
                    leftTextWidth = 1;
                    rightTextWidth = 1;
                }else {
                    rightTextWidth = pagerWidth - leftTextWidth;
                }
            }
        }

        writeln(new String[]{leftText, rightText}, new int[]{leftTextWidth, rightTextWidth}, new int[]{ALIGN_LEFT, ALIGN_RIGHT});
    }

    @Override
    public void writeCenter(String text) throws Exception {
        int pagerWidth = getPagerWidth();
        TextView textView = newTextView();
        textView.setGravity(Gravity.CENTER);
        textView.setText(text);
        textView.layout(0, 0, pagerWidth, getTextViewHeight(textView, pagerWidth));
        mPrintViewList.add(textView);
    }

    @Override
    public void writeCenter(String text, boolean addLine) throws Exception {
        writeCenter(text);
    }

    @Override
    public void writeln(String[] arrStr, int[] arrWidth, int[] arrAlign) throws Exception {
        if(mIsUyghur) {
            arrStr = arrStr == null? null : arrStr.clone();
            arrWidth = arrWidth == null? null : arrWidth.clone();
            arrAlign = arrAlign == null? null : arrAlign.clone();

            reverse(arrStr);
            reverse(arrWidth);
            reverse(arrAlign);
        }

        int pagerWidth = getPagerWidth();
        FrameLayout layout = new FrameLayout(mContext);
        layout.setLayoutParams(new FrameLayout.LayoutParams(pagerWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setBackgroundColor(Color.WHITE);

        int totalWeight = 0;
        for(int item : arrWidth){
            totalWeight += item;
        }

        int maxHeight = 0;
        int startX = 0;
        List<View> childList = new ArrayList<>();
        for(int i=0; i<arrStr.length; i++){
            int weight = arrWidth[i];
            int gravity = arrAlign == null? Gravity.LEFT : getGravity(arrAlign[i]);
            int width = Math.round(pagerWidth * (weight / (float)totalWeight));

            TextView textView = newTextView(width);
            textView.setGravity(gravity);
            textView.setText(arrStr[i]);
            int height = getTextViewHeight(textView, width);
            textView.layout(startX, 0, startX + width, height);

            childList.add(textView);

            startX += width;
            maxHeight = Math.max(maxHeight, height);
        }
        layout.layout(0,0, pagerWidth ,maxHeight);

        for(View child : childList){
            layout.addView(child);
        }

        mPrintViewList.add(layout);
    }

    @Override
    public void nextLine() throws Exception {
        write(mBlank);
    }

    @Override
    public void writeln(String[] arrStr, int[] arrWidth) throws Exception {
        writeln(arrStr, arrWidth, null);
    }


    @Override
    public void printCode(String url) throws Exception {
        printCode(url, QR_WIDTH, QR_HEIGHT);
    }

    @Override
    public void printCode(String url, int width, int height) throws Exception {
        Bitmap bitmap = createQrCode(url, width, width);
        printBitmap(bitmap);
    }


    @Override
    public void printImage(String filePath, int width, int height) throws Exception {
        Bitmap bitmap = ratio2(filePath, width, height);
        if(bitmap == null){
            return;
        }
        printBitmap(bitmap);
    }

    @Override
    public void printBarcode(String barcode) throws Exception {
        Bitmap bitmap = createBarcode2(barcode, BARCODE_WIDTH, BARCODE_HEIGHT, false, 0, null);
        printBitmap(bitmap);
    }

    @Override
    public void printBitmap(Bitmap bitmap) throws Exception {
        int pagerWidth = getPagerWidth();
        ImageView imageView = new ImageView(mContext);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(pagerWidth, bitmap.getHeight()));
        imageView.setBackgroundColor(Color.TRANSPARENT);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.layout(0, 0, pagerWidth, bitmap.getHeight());

        mPrintViewList.add(imageView);
    }

    @Override
    public void printBarcode(String barcode, int width, int height) throws Exception {
        Bitmap bitmap = createBarcode2(barcode, width, height, false, 0, null);
        printBitmap(bitmap);
    }

    @Override
    public void openCashBox() throws Exception {

    }

    @Override
    public void cut() throws Exception {
        if(mPrintViewList == null || mPrintViewList.isEmpty()){
            return;
        }

        nextLine();
        nextLine();
        nextLine();

        Log.d("printer", "start cut....................");
        for(int i=0; i<mPrintViewList.size(); i+= mPrintMaxLine){
            
            int start = i;
            int end = i + mPrintMaxLine;
            if(end >= mPrintViewList.size()){
                end = mPrintViewList.size();
            }
            
            List<View> viewList = mPrintViewList.subList(start, end);

            int pagerWidth = getPagerWidth();
            FrameLayout layout = new FrameLayout(mContext);
            layout.setLayoutParams(new FrameLayout.LayoutParams(pagerWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.setBackgroundColor(Color.WHITE);

            int height = 0;
            int startY = 0;
            for(int y=0; y<viewList.size(); y++){
                View view = viewList.get(y);

                if(view instanceof FrameLayout){
                    FrameLayout frameLayout = ((FrameLayout)view);
                    List<View> childList = new ArrayList<>();
                    for(int k=0; k<frameLayout.getChildCount(); k++){
                        View child = frameLayout.getChildAt(k);
                        childList.add(child);
                    }
                    frameLayout.removeAllViews();

                    for(View child : childList){
                        child.setTranslationX(child.getLeft());
                        child.setTranslationY(startY);
                        layout.addView(child);
                    }
                }else {
                    view.setTranslationX(0);
                    view.setTranslationY(startY);
                    layout.addView(view);
                }

                height += view.getHeight() + LINE_H;
                startY += view.getHeight() + LINE_H;
            }
            layout.layout(0, 0, pagerWidth, height);

            Bitmap bitmap = viewToBitmap(layout);
            if(bitmap == null){
                continue;
            }
            sendBitmap(bitmap);
        }
    }

    protected int getTotalLen(){
        if(mFont == FONT_LARGE){ //大号字体
            return Math.round(mTotalWord * 0.5F);
        }else if(mFont == FONT_MEDIUM ){ //中号字体
            return Math.round(mTotalWord * 0.75F);
        }else {
            return mTotalWord;
        }
    }

    private TextView newTextView(){
        return newTextView(getPagerWidth());
    }

    private TextView newTextView(int width){
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSize());
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);

        return textView;
    }

    private int getFontSize(){
        switch (mFont){
            case FONT_MEDIUM:
                return DEFAULT_FONT_MEDIUM;
            case FONT_LARGE:
                return DEFAULT_FONT_LARGE_SIZE;
            default:
                return DEFAULT_FONT_SIZE;
        }
    }

    private int getPagerWidth(){
        return 384;
    }

    private int getGravity(int align){
        switch (align){
            case ALIGN_RIGHT:
                return mIsUyghur? Gravity.LEFT : Gravity.RIGHT;
            case ALIGN_CENTER:
                return Gravity.CENTER;
            default:
                return mIsUyghur? Gravity.RIGHT : Gravity.LEFT;
        }
    }

    public void sendBitmap(Bitmap bitmap) throws Exception {

    }

    public static String getLang(){
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        return TextUtils.isEmpty(country)? language : language + "_" + country;
    }

    @SuppressLint("Range")
    public static int getTextViewHeight(TextView textView, int width){

        //设置textView的宽度为屏幕宽度，模式为macth_parent (View.MeasureSpec.EXACTLY)
        int w = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        //设置textView的高度为屏幕wrap_content (-2) ,模式为View.MeasureSpec.AT_MOST
        int h = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST);

        //测量宽高  传入的宽高一定要设置模式，否则就是默认模式
        textView.measure(w,h);
        return textView.getMeasuredHeight();
    }

    @SuppressLint("Range")
    public static int getTextViewWidth(TextView textView, int height){

        //设置textView的宽度为屏幕宽度，模式为macth_parent (View.MeasureSpec.EXACTLY)
        int w = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST);
        //设置textView的高度为屏幕wrap_content (-2) ,模式为View.MeasureSpec.AT_MOST
        int h = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST);

        //测量宽高  传入的宽高一定要设置模式，否则就是默认模式
        textView.measure(w,h);
        return textView.getMeasuredWidth();
    }

    public static int toInt(double value){
        BigDecimal bg = new BigDecimal(value);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static void reverse(Object[] array){
        if(array != null && array.length > 0){
            reverse(array, 0, array.length);
        }
    }

    public static void reverse(Object[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array != null) {
            int i = startIndexInclusive < 0 ? 0 : startIndexInclusive;

            for(int j = Math.min(array.length, endIndexExclusive) - 1; j > i; ++i) {
                Object tmp = array[j];
                array[j] = array[i];
                array[i] = tmp;
                --j;
            }

        }
    }

    public static void reverse(int[] array) {
        if (array != null) {
            reverse((int[])array, 0, array.length);
        }
    }

    public static void reverse(int[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array != null) {
            int i = startIndexInclusive < 0 ? 0 : startIndexInclusive;

            for(int j = Math.min(array.length, endIndexExclusive) - 1; j > i; ++i) {
                int tmp = array[j];
                array[j] = array[i];
                array[i] = tmp;
                --j;
            }

        }
    }

    public static Bitmap createQrCode(String str, int width, int height){
        return createQrCode(str, width, height, false);
    }

    public static Bitmap createQrCode(String str, int width, int height, boolean noBorder) {
        return createQrCode(str, width, height, noBorder, Color.BLACK, Color.WHITE);
    }

    public static Bitmap createQrCode(String str, int width, int height, boolean noBorder, int color, int bgColor) {
        BitMatrix result = null;
        try {
            Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, width, height, hints);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (noBorder) {
            result = deleteWhite(result);//删除白边
        }

        width = result.getWidth();
        height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? color : bgColor;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }

    public static Bitmap ratio2(String imgPath, float pixelW, float pixelH) {
        if(TextUtils.isEmpty(imgPath)){
            return null;
        }
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;

        BitmapFactory.decodeFile(imgPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        newOpts.inSampleSize = (int)Math.ceil(newOpts.outHeight / pixelH);//设置缩放比例
        return BitmapFactory.decodeFile(imgPath, newOpts);
    }

    public static Bitmap createBarcode2( String content, int widthPix, int heightPix, boolean isShowContent, float fontSize, String gravity) {
        if (TextUtils.isEmpty(content)){
            return null;
        }

        int srcWidthPix = widthPix;

        if(isShowContent){
            heightPix = toInt(heightPix - fontSize);
        }

        try {
            widthPix = getBarCodeNoPaddingWidth(widthPix * 2, widthPix * 2, content);

            //配置参数
            Map<EncodeHintType,Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 容错级别 这里选择最高H级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 0);
            MultiFormatWriter writer = new MultiFormatWriter();

            // 图像数据转换，使用了矩阵转换 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = Color.BLACK; // 黑色
                    } else {
                        pixels[y * widthPix + x] = Color.WHITE;// 白色
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.RGB_565);
            bitmap.setDensity(Bitmap.DENSITY_NONE);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
            bitmap = getBitmap(bitmap, srcWidthPix, bitmap.getHeight());
            if (isShowContent){
                bitmap = showContent(bitmap, content, fontSize, gravity);
            }
            return bitmap;
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return null;
    }

    private static int getBarCodeNoPaddingWidth(int expectWidth,int maxWidth, String contents){
        boolean[] code = new Code128Writer(). encode(contents);

        int inputWidth = code.length;

        //code:210000000000000082 code.length:134 expectWidth:397 maxWidth:435
        // Add quiet zone on both sides.
        //int fullWidth = inputWidth + 0;

        double outputWidth = (double) Math.max(expectWidth, inputWidth);
        double multiple = outputWidth / inputWidth;

        //优先取大的
        int returnVal =0;
        int ceil = (int) Math.ceil(multiple);
        if(inputWidth * ceil <= maxWidth){
            returnVal =  inputWidth * ceil;
        }else {
            int floor = (int) Math.floor(multiple);
            returnVal =  inputWidth * floor;
        }

        return returnVal;
    }

    private static Bitmap showContent(Bitmap bCBitmap , String content, float fontSize, String gravity){
        if (TextUtils.isEmpty(content) || bCBitmap == null){
            return null;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();

        //绘制字符串矩形区域的高度
        int textHeight = (int) (fm.bottom - fm.top);

        //文字宽度
        int textWidth = toInt(paint.measureText(content));

        //绘制文本的基线
        int baseLine = bCBitmap.getHeight() - (int)fm.top;

        //创建一个图层，然后在这个图层上绘制bCBitmap、content
        Bitmap  bitmap = Bitmap.createBitmap(Math.max(bCBitmap.getWidth(), textWidth),bCBitmap.getHeight() + textHeight, Bitmap.Config.RGB_565);
        bitmap.setDensity(Bitmap.DENSITY_NONE);
        bitmap.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas();
        canvas.drawColor(Color.WHITE);
        canvas.setBitmap(bitmap);

        if("center".equals(gravity)){
            canvas.drawBitmap(bCBitmap, (bitmap.getWidth() - bCBitmap.getWidth()) / 2, 0, null);
        }else if("right".equals(gravity)){
            canvas.drawBitmap(bCBitmap, (bitmap.getWidth() - bCBitmap.getWidth()), 0, null);
        }else {
            canvas.drawBitmap(bCBitmap, 0, 0, null);
        }

        canvas.drawText(content,bitmap.getWidth() / 2, baseLine, paint);
        canvas.save();
        canvas.restore();
        return bitmap;
    }

    public static Bitmap getBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scale = (float) newWidth / w;
        float scale2 = (float) newHeight / h;
        // scale = scale < scale2 ? scale : scale2;
        matrix.postScale(scale, scale2);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, false);
        if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled())
        {
            bitmap.recycle();
        }
        return bmp;// Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    public static Bitmap viewToBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.setDrawingCacheBackgroundColor(Color.WHITE);
        Bitmap bitmap = loadBitmapFromView(view);
        view.destroyDrawingCache();

        return bitmap;
    }

    private static Bitmap loadBitmapFromView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        c.drawColor(Color.WHITE);
        /** 如果不设置canvas画布为白色，则生成透明 */

        int widthSpec = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY);
        v.measure(widthSpec, heightSpec);
        v.layout(0, 0, w, h);
        v.draw(c);

        return bmp;
    }

    protected String getLine(int len) {
        String str = "";
        for (int i = 0; i < len; i++) {
            str += LINE;
        }
        return str;
    }
}
