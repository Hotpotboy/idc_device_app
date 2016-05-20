package com.zhanghang.idcdevice.adbsocket;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.adbsocket.AdbSocketConnectionThread;
import com.adbsocket.AdbSocketServer;
import com.adbsocket.AdbSocketUtils;
import com.zhanghang.idcdevice.DeviceApplication;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by Administrator on 2016-03-27.
 */
public class AdbSocketService extends Service {
    private static boolean isConnectonPc = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case AdbSocketUtils.CONNECTIONED_COMMANDE:
                    Toast.makeText(DeviceApplication.getInstance(), "手机已连接电脑!", Toast.LENGTH_LONG).show();
                    break;
                case AdbSocketUtils.PC_RETURN_COMMAND://PC端返回的数据
                    int code = message.arg1;
                    String command = (String) message.obj;
                    Request.recevieResult(code, command);
                    break;
                case AdbSocketUtils.CONNECTION_LOST_COMMANDE:
                    break;
                case AdbSocketUtils.CLOSE_CONNECTION_COMMAND://关闭连接
                    stopSelf();
                    break;
            }
        }
    };
    /**
     * adb socket服务端
     */
    private AndroidAdbSocketServer mAdbSocketServer;
    private String TAG = AdbSocketService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static synchronized void setIsConnectonPc(boolean _isConnectionPc) {
        isConnectonPc = _isConnectionPc;
    }

    public static synchronized boolean isConnectionPc() {
        return isConnectonPc;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        super.onStartCommand(intent, flag, startId);
        if (mAdbSocketServer == null) {
            mAdbSocketServer = new AndroidAdbSocketServer(mHandler);
            mAdbSocketServer.start();//开启监听线程
            Log.i(TAG,"[adb socket:]_______________________________开启监听线程……");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mAdbSocketServer.interrupt();
        mAdbSocketServer = null;
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }
}
