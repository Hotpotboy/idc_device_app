package com.zhanghang.idcdevice.adbsocket;

import android.os.Handler;
import android.os.Message;

import com.adbsocket.AdbSocketServer;
import com.adbsocket.AdbSocketUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by hangzhang209526 on 2016/5/20.
 */
public class AndroidAdbSocketServer extends AdbSocketServer {
    /**
     * 与主线程进行通信
     */
    private Handler mHandler;
    public AndroidAdbSocketServer(Handler _handler){
        mHandler = _handler;
    }
    /***
     * 向PC端发送消息
     */
    @Override
    protected void dealWhenIDEL() {
        Request.sendAllRequest(mSocketChannel);
    }

    @Override
    /**
     * 处理USB数据线断开时的异常
     */
    protected void dealConnectionCloseException() {
        super.dealConnectionCloseException();
        AdbSocketService.setIsConnectonPc(false);//断开连接
        mHandler.sendEmptyMessage(AdbSocketUtils.CONNECTION_LOST_COMMANDE);
    }

    @Override
    protected void connectionForChannal(SocketChannel socketChannel) throws IOException, InterruptedException {

    }

    @Override
    protected void writeForChannal(WritableByteChannel writableChannel) throws IOException, InterruptedException {

    }

    /**
     * 处理命令
     */
    protected void resolveCommand(int code, String command) {
        if (code == AdbSocketUtils.CONNECTIONED_COMMANDE) {//连接命令
            AdbSocketService.setIsConnectonPc(true);
            mHandler.sendEmptyMessage(AdbSocketUtils.CONNECTIONED_COMMANDE);
        } else if (code >= AdbSocketUtils.PC_RETURN_COMMAND) {//pc返回命令
            if (code == AdbSocketUtils.CLOSE_CONNECTION_COMMAND) {//关闭连接
                super.dealConnectionCloseException();
                AdbSocketService.setIsConnectonPc(false);//断开连接
                mHandler.sendEmptyMessage(AdbSocketUtils.CLOSE_CONNECTION_COMMAND);
            } else {
                Message message = mHandler.obtainMessage(AdbSocketUtils.PC_RETURN_COMMAND);
                message.arg1 = code;
                message.obj = command;
                message.sendToTarget();
            }
        }
    }
}
