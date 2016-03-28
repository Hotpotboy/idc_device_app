package com.adbsocket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by Administrator on 2016-03-27.
 */
public class AdbSocketClient extends AdbSocketConnectionThread {
    static Pipe sPie;

    static {
        try {
            sPie=Pipe.open();
            sPie.source().configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isRegister = false;

    public static void main(String[] args){
        new AdbSocketScannerThread().start();
        new AdbSocketClient().start();
    }

    @Override
    protected boolean onPreLoop() {
        try {
            System.out.println("注册通道");
            sPie.source().register(selector, SelectionKey.OP_READ);
            synchronized (AdbSocketUtils.sLock){
                isRegister = true;
                AdbSocketUtils.sLock.notify();
            }
            return true;
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void acceptForChannal(ServerSocketChannel serverSocketChannel) throws IOException {

    }

    @Override
    protected void connectionForChannal(SocketChannel socketChannel) throws IOException {
        //如果正在连接，则完成连接
        boolean isConnected = false;
        if(socketChannel.isConnectionPending()){
            isConnected = socketChannel.finishConnect();
        }
        if(isConnected) {
            System.out.println("[adb socket:]_______________________________建立连接成功……");
            writeContent(AdbSocketUtils.CONNECTIONED_COMMANDE + AdbSocketUtils.END_COMMAND, socketChannel);
        }
    }

    @Override
    protected void writeForChannal(WritableByteChannel writableChannel) throws IOException {

    }

    @Override
    protected void readForChannal(ReadableByteChannel readableChannel) throws IOException, InterruptedException {
        if(sPie.source().equals(readableChannel)){//find a device
            ByteBuffer byteBuffer = ByteBuffer.allocate(64);

            int count = sPie.source().read(byteBuffer);
            byteBuffer.flip();
            byte[] bytes = new byte[count];
            byteBuffer.get(bytes);
            String deviceStr = new String(bytes,"UTF-8");
            deviceStr = deviceStr.replaceAll(" ","");
            if(deviceStr.length()>0){
                System.out.println("---------------------------【"+deviceStr+"】");
                Device device = new Device(deviceStr);
                if (!mDevices.contains(device)) {
                    conncetion(device);//链接设备
                }
            }
        }else{

        }
    }
    private void conncetion(Device device) throws IOException, InterruptedException {
        Runtime.getRuntime().exec(AdbSocketScannerThread.ADB_PATH + " -s " + device.getDeviceId() + " forward tcp:" + device.getPort() + " tcp:" + AdbSocketUtils.SERVER_PORT);
        System.out.println(AdbSocketScannerThread.ADB_PATH + " -s " + device.getDeviceId() + " forward tcp:" + device.getPort() + " tcp:" + AdbSocketUtils.SERVER_PORT);
        Thread.sleep(3000);
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);//非阻塞
        socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        socketChannel.socket().setKeepAlive(true);
        socketChannel.connect(new InetSocketAddress(AdbSocketUtils.SERVER_IP, device.getPort()));
    }
}
