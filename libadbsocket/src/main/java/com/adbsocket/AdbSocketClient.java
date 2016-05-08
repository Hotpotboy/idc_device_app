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
 * PC手机助手主体类
 */
public class AdbSocketClient extends AdbSocketConnectionThread {
    static Pipe sPie;

    static void initPipe(){
        try {
            sPie = Pipe.open();
            sPie.source().configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isRegister = false;
    /**
     * 扫描USB连接线线程，频率为10秒一次
     */
    public static AdbSocketScannerThread mScannerThread;
    /**
     * Socket PC端通信线程
     */
    public static AdbSocketClient mSocketThread;
    private SocketChannel mSocketChannel;

    public static void main(String[] args) {
        startWork();
    }

    /**
     * 启动PC手机助手
     */
    public static void startWork() {
        isRegister = false;
        mScannerThread = new AdbSocketScannerThread();
        mSocketThread = new AdbSocketClient();
        mScannerThread.start();
        mSocketThread.start();
        System.out.println("[adb socket:]_______________________________PC手机服务助手开始启动……");
    }

    /**
     * 安全退出PC手机助手
     */
    public static void stopWork() {
        mScannerThread.interrupt();
        mSocketThread.interrupt();
        while (!mScannerThread.isStop()||!mSocketThread.isStop()){
            try {
                Thread.sleep(AdbSocketUtils.SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mScannerThread = null;
        mSocketThread = null;
        System.out.println("[adb socket:]_______________________________PC手机服务助手停止……");
    }

    @Override
    protected void endThread(){
        try {
            if (mSocketChannel != null) {
                mSocketChannel.close();
            }
            if (sPie != null) {
                if (sPie.source() != null) sPie.source().close();
                if (sPie.sink() != null) sPie.sink().close();
            }
        }catch (Exception e){
        }
    }

    @Override
    protected boolean onPreLoop() {
        super.onPreLoop();
        try {
            initPipe();
            System.out.println("注册通道");
            sPie.source().register(selector, SelectionKey.OP_READ);
            synchronized (AdbSocketUtils.sLock) {
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
        if (socketChannel.isConnectionPending()) {
            isConnected = socketChannel.finishConnect();
        }
        if (isConnected) {
            mSocketChannel = socketChannel;
            writeContent(AdbSocketUtils.CONNECTIONED_COMMANDE + "", socketChannel);
            System.out.println("[adb socket:]_______________________________建立连接成功……");
        }
    }

    @Override
    protected void writeForChannal(WritableByteChannel writableChannel) throws IOException {

    }

    @Override
    protected void readForChannal(ReadableByteChannel readableChannel) throws IOException, InterruptedException {
        if (sPie.source().equals(readableChannel)) {//find a device
            ByteBuffer byteBuffer = ByteBuffer.allocate(64);

            int count = sPie.source().read(byteBuffer);
            byteBuffer.flip();
            byte[] bytes = new byte[count];
            byteBuffer.get(bytes);
            String deviceStr = new String(bytes, "UTF-8");
            deviceStr = deviceStr.replaceAll(" ", "");
            if (deviceStr.length() > 0) {
                System.out.println("[adb socket:]_______________________________【" + deviceStr + "】");
                Device device = new Device(deviceStr);
//                if (!mDevices.contains(device)) {
                if (mSocketChannel == null) {
                    conncetion(device);//链接设备
                }
            }
        } else {
            String command = readDataFromChannel(readableChannel);
            Object[] commands = resolveRawCommand(command);
            if (commands != null) {
                resolveCommand((int) commands[0], (String) commands[1]);
            }
        }
    }

    private void conncetion(Device device) throws IOException, InterruptedException {
        Runtime.getRuntime().exec(AdbSocketScannerThread.ADB_PATH + " -s " + device.getDeviceId() + " forward tcp:" + device.getPort() + " tcp:" + AdbSocketUtils.SERVER_PORT);
        System.out.println("[adb socket:]_______________________________" + AdbSocketScannerThread.ADB_PATH + " -s " + device.getDeviceId() + " forward tcp:" + device.getPort() + " tcp:" + AdbSocketUtils.SERVER_PORT);
        Thread.sleep(3000);
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);//非阻塞
        socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        socketChannel.socket().setKeepAlive(true);
        socketChannel.connect(new InetSocketAddress(AdbSocketUtils.SERVER_IP, device.getPort()));
    }

    /**
     * 解析接收到的命令
     *
     * @param code    命令码
     * @param command 除命令码之外的整个命令
     */
    private void resolveCommand(int code, String command) {
        String result = "";
        switch (code) {
            case AdbSocketUtils.LOGIN_IN_COMMANDE://登陆
                result = code + "?";
                break;
            case AdbSocketUtils.GET_ALL_INFOS_COMMANDE://获取所有数据
                result =code + "?{\"devices\":[{ \"deviceId\":1, \"deviceName\":\"IBM网络服务器【1】\", \"deviceNum\":\"0000000000000001\", \"assetNum\":\"7854691\", \"assetSerialNum\":\"7854691\", \"entityAssetNum\":\"IBM-PSO123456-1\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第1机柜\", \"position\":\"第1位置\" }, { \"deviceId\":2, \"deviceName\":\"IBM网络服务器【2】\", \"deviceNum\":\"0000000000000002\", \"assetNum\":\"7854692\", \"assetSerialNum\":\"7854692\", \"entityAssetNum\":\"IBM-PSO123456-2\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第1机柜\", \"position\":\"第2位置\" }, { \"deviceId\":3, \"deviceName\":\"IBM网络服务器【3】\", \"deviceNum\":\"0000000000000003\", \"assetNum\":\"7854693\", \"assetSerialNum\":\"7854693\", \"entityAssetNum\":\"IBM-PSO123456-3\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第2机柜\", \"position\":\"第1位置\" }, { \"deviceId\":4, \"deviceName\":\"IBM网络服务器【4】\", \"deviceNum\":\"0000000000000004\", \"assetNum\":\"7854694\", \"assetSerialNum\":\"7854694\", \"entityAssetNum\":\"IBM-PSO123456-4\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第2机柜\", \"position\":\"第2位置\" }, { \"deviceId\":5, \"deviceName\":\"IBM网络服务器【5】\", \"deviceNum\":\"0000000000000005\", \"assetNum\":\"7854695\", \"assetSerialNum\":\"7854695\", \"entityAssetNum\":\"IBM-PSO123456-5\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第3机柜\", \"position\":\"第1位置\" },{ \"deviceId\":6, \"deviceName\":\"IBM网络服务器【6】\", \"deviceNum\":\"0000000000000006\", \"assetNum\":\"7854696\", \"assetSerialNum\":\"7854696\", \"entityAssetNum\":\"IBM-PSO123456-6\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第3机柜\", \"position\":\"第2位置\" }],\"tasks\": [ {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务1\",\"taskId\":1 }, {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务2\",\"taskId\":2 }, {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务3\",\"taskId\":3 }, {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务4\",\"taskId\":4 }, {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务5\",\"taskId\":5 }],\"patrols\":[ {\"deviceType\":1,\"enable\":1,\"patrolId\":1,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":2,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":3,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":4,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":5,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":6,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":7,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":8,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":9,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":10,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":11,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":12,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":13,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":14,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":5,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":16,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":17,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":18,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":19,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":20,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":21,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":5 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":22,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":5 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":23,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":5 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":24,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":5 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":25,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":5 }]}";
                break;
            case AdbSocketUtils.UPLOAD_DB_COMMAND://上传数据库
                System.out.println("上传的数据："+command);
                result = code+"";
                break;
            case AdbSocketUtils.CLOSE_CONNECTION_COMMAND://手机主动关闭Socket连接
                result = code+"";
                break;
        }
        if (mSocketChannel != null && mSocketChannel.isConnected()) {
            try {
                writeContent(result, mSocketChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(code==AdbSocketUtils.CLOSE_CONNECTION_COMMAND){
                //重启服务
                new Thread(){
                    @Override
                    public void run(){
                        stopWork();
//                        String restartAdbCommand = AdbSocketScannerThread.ADB_PATH +" kill-server \n\r"+AdbSocketScannerThread.ADB_PATH +" start-server \n\r";
//                        try {
//                            Process process = Runtime.getRuntime().exec(restartAdbCommand);
//                            Thread.sleep(10000);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        startWork();
                    }
                }.start();
            }
        }
    }

    /**
     * 处理USB数据线断开时的异常
     */
    protected void dealConnectionCloseException() {
        if (mSocketChannel != null) {
            try {
                selector.selectedKeys().remove(mSocketChannel.keyFor(selector));
                mSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mSocketChannel = null;
    }
}
