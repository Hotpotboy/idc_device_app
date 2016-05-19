package com.zhanghang.idcdevice.adbsocket;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
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
    //    public static final String IS_CONNECITION_PC_KEY = "is_connection_pc_key";
    private static boolean isConnectonPc = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case AdbSocketUtils.CONNECTIONED_COMMANDE:
                    Toast.makeText(DeviceApplication.getInstance(), "手机已连接电脑!", Toast.LENGTH_LONG).show();
                    break;
                case AdbSocketUtils.PC_RETURN_COMMAND://PC端返回的数据
                    Object obj = message.obj;
                    if (obj instanceof Object[]) {
                        Object[] commands = (Object[]) obj;
                        Request.recevieResult((int) commands[0], (String) commands[1]);
                    }
                    break;
                case AdbSocketUtils.CONNECTION_LOST_COMMANDE:
                    Request request = (Request) message.obj;
                    if(request!=null) {
                        request.waitingToEnd(false, "Socket连接丢失,请检查USB数据线……");
                    }else{
                        stopSelf();
                    }
                    break;
            }
        }
    };
    /**
     * adb socket服务端
     */
    private AdbSocketServer mAdbSocketServer;

    //    /**等待发送给PC端的请求列表*/
//    private ArrayList<Request> mUnSendingQueue = new ArrayList<>();
//    /**等待PC端响应的请求列表*/
//    private ArrayList<Request> mWaitingQueue = new ArrayList<>();
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
            mAdbSocketServer = new AdbSocketServer() {
                @Override
                protected void readForChannal(ReadableByteChannel readableChannel) throws IOException, InterruptedException {
                    String commandData = readDataFromChannel(readableChannel);
                    System.out.println("[adb socket:]_______________________________接收到电脑发送而来的内容【" + commandData + "】……");
                    dealCommand(commandData);
                }

                /***
                 * 向PC端发送消息
                 */
                @Override
                protected void dealWhenIDEL() {
                    Request.sendAllRequest(mPCSocketChannel);
                }

                @Override
                /**
                 * 处理USB数据线断开时的异常
                 */
                protected void dealConnectionCloseException() {
                    super.dealConnectionCloseException();
                    setIsConnectonPc(false);//断开连接
                    mHandler.sendEmptyMessage(AdbSocketUtils.CONNECTION_LOST_COMMANDE);
                }

                /**
                 * 处理命令
                 */
                private void dealCommand(String command) {
                    Object[] commands = AdbSocketConnectionThread.resolveRawCommand(command);
                    if (commands != null) {
                        if ((int)commands[0] == AdbSocketUtils.CONNECTIONED_COMMANDE) {//连接命令
                            setIsConnectonPc(true);
                            mHandler.sendEmptyMessage(AdbSocketUtils.CONNECTIONED_COMMANDE);
                        }else if ((int) commands[0] >= AdbSocketUtils.PC_RETURN_COMMAND) {//pc返回命令
                            if((int)commands[0]==AdbSocketUtils.CLOSE_CONNECTION_COMMAND){//关闭连接
                                dealConnectionCloseException();
                            }else {
                                Message message = mHandler.obtainMessage(AdbSocketUtils.PC_RETURN_COMMAND);
                                message.obj = commands;
                                message.sendToTarget();
                            }
                        }
                    }
                }
            };
            mAdbSocketServer.start();//开启监听线程
            System.out.println("[adb socket:]_______________________________开启监听线程……");
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
