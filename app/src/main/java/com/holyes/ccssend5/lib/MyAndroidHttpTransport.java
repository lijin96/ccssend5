package com.holyes.ccssend5.lib;

import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.ServiceConnection;

import java.io.IOException;

/**
 * @ClassName: MyAndroidHttpTransport
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:27
 */
public class MyAndroidHttpTransport  extends HttpTransportSE{
    private int timeout = 30000; //默认超时时间为30s

    public MyAndroidHttpTransport(String url) {
        super(url);
    }

    public MyAndroidHttpTransport(String url, int timeout) {
        super(url);
        this.timeout = timeout;
    }



    protected ServiceConnection getServiceConnection(String url) throws IOException {
//	        ServiceConnectionSE serviceConnection = new ServiceConnectionSE(url);
//	        serviceConnection.setConnectionTimeOut(timeout);
//	        return new ServiceConnectionSE(url);
        ServiceConnectionSE serviceConnection =new ServiceConnectionSE(this.url,timeout);

        return serviceConnection;

    }


}
