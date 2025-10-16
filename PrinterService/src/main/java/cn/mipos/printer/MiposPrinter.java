package cn.mipos.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;

import java.util.concurrent.locks.ReentrantLock;

import cn.mipos.service.BitmapConvertor;
import cn.mipos.service.PosService;

/**
 * 想米自带打印机
 */

public class MiposPrinter extends BasePrinter {

    private static ReentrantLock sReentrantLock = new ReentrantLock();

    private static PosService mPosService;

    private BitmapConvertor mBitmapConvertor;

    public MiposPrinter(Context context){
        super(context);
        mBitmapConvertor = new BitmapConvertor();
        if(mPosService == null){
            mPosService = new PosService(context);
        }
    }

    @Override
    public void openCashBox() throws Exception {
        mPosService.openCashBox();
    }

    @Override
    public void sendBitmap(Bitmap bitmap) throws Exception {
        byte[] b = mBitmapConvertor.toBytes(bitmap);
        Process.setThreadPriority(-20);
        int ret = mPosService.printerPrint(b, b.length);

        if(ret != MiposPrinterErrorEnum.OK.getCode()){
            String error = MiposPrinterErrorEnum.getDescByCode(ret);
            throw new Exception(error);
        }

        Thread.sleep(150);
    }

    @Override
    public void cut() throws Exception {
        sReentrantLock.lock();
        super.cut();
        sReentrantLock.unlock();
    }
}
