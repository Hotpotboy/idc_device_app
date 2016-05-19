package com.adbsocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
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
    /**
     * 与扫描USB连接线程通信的通道
     */
    static Pipe sPie;
    /**
     * 是否注册了与扫描USB连接线程通信的通道到选择器之中
     */
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

    static void initPipe() {
        try {
            sPie = Pipe.open();
            sPie.source().configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        while (!mScannerThread.isStop() || !mSocketThread.isStop()) {
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
    protected void endThread() {
        try {
            if (mSocketChannel != null) {
                mSocketChannel.close();
            }
            if (sPie != null) {
                if (sPie.source() != null) sPie.source().close();
                if (sPie.sink() != null) sPie.sink().close();
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected boolean onPreLoop() {
        super.onPreLoop();
        try {
            initPipe();//注册与
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
        String netResult = "";
        switch (code) {
            case AdbSocketUtils.LOGIN_IN_COMMANDE://登陆
                netResult = getMsgFromNet(AdbSocketUtils.HttpMethod.POST, AdbSocketUtils.LOGIN_IN_URL, command);//登陆
                result = code + "?" + netResult;
                break;
            case AdbSocketUtils.LOGIN_OUT_COMMANDE://登出
                netResult = getMsgFromNet(AdbSocketUtils.HttpMethod.POST, AdbSocketUtils.LOGIN_OUT_URL, command);//登出
                result = code + "?" + netResult;
                break;
            case AdbSocketUtils.GET_ALL_INFOS_COMMANDE://获取所有数据
                netResult = getMsgFromNet(AdbSocketUtils.HttpMethod.POST, AdbSocketUtils.DOWNLOAD_URL, command);//下载
//                result =code + "?{\"devices\":[{ \"deviceId\":1, \"deviceName\":\"IBM网络服务器【1】\", \"deviceNum\":\"0000000000000001\", \"assetNum\":\"7854691\", \"assetSerialNum\":\"7854691\", \"entityAssetNum\":\"IBM-PSO123456-1\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第1机柜\", \"position\":\"第1位置\" }, { \"deviceId\":2, \"deviceName\":\"IBM网络服务器【2】\", \"deviceNum\":\"0000000000000002\", \"assetNum\":\"7854692\", \"assetSerialNum\":\"7854692\", \"entityAssetNum\":\"IBM-PSO123456-2\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第1机柜\", \"position\":\"第2位置\" }, { \"deviceId\":3, \"deviceName\":\"IBM网络服务器【3】\", \"deviceNum\":\"0000000000000003\", \"assetNum\":\"7854693\", \"assetSerialNum\":\"7854693\", \"entityAssetNum\":\"IBM-PSO123456-3\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第2机柜\", \"position\":\"第1位置\" }, { \"deviceId\":4, \"deviceName\":\"IBM网络服务器【4】\", \"deviceNum\":\"0000000000000004\", \"assetNum\":\"7854694\", \"assetSerialNum\":\"7854694\", \"entityAssetNum\":\"IBM-PSO123456-4\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第2机柜\", \"position\":\"第2位置\" }, { \"deviceId\":5, \"deviceName\":\"IBM网络服务器【5】\", \"deviceNum\":\"0000000000000005\", \"assetNum\":\"7854695\", \"assetSerialNum\":\"7854695\", \"entityAssetNum\":\"IBM-PSO123456-5\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第3机柜\", \"position\":\"第1位置\" },{ \"deviceId\":6, \"deviceName\":\"IBM网络服务器【6】\", \"deviceNum\":\"0000000000000006\", \"assetNum\":\"7854696\", \"assetSerialNum\":\"7854696\", \"entityAssetNum\":\"IBM-PSO123456-6\", \"assetType1\":\"电脑\", \"assetType2\":\"网络电脑\", \"assetType3\":\"网络服务器电脑\", \"deviceModel\":\"网络服务器电脑\", \"city\":\"北京\", \"idcRoom\":\"服务器机房1\", \"cabinet\":\"第3机柜\", \"position\":\"第2位置\" }],\"tasks\": [ {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务1\",\"taskId\":1 }, {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务2\",\"taskId\":2 }, {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务3\",\"taskId\":3 }, {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务4\",\"taskId\":4 }, {\"planedStartTime\":1450656000000,\"planedEndTime\":1482192000000,\"deviceType\":1,\"details\":\"详情详情详情详情详情详情…\",\"taskName\":\"日常巡检任务5\",\"taskId\":5 }],\"patrols\":[ {\"deviceType\":1,\"enable\":1,\"patrolId\":1,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":2,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":3,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":4,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":5,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":1 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":6,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":7,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":8,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":9,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":10,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":2 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":11,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":12,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":13,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":14,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":5,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":3 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":16,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":17,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":18,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":19,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":20,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":4 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":21,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":5 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":22,\"patrolItemName\":\"检查是否能够开关机?\",\"patrolDetail\":\"通过电源键来确定是否能开关机!\",\"taskId\":5 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":23,\"patrolItemName\":\"检查是否能够扩展内存?\",\"patrolDetail\":\"通过相关检测软件来确定是否能够扩展内存!\",\"taskId\":5 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":24,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":5 }, {\"deviceType\":1,\"enable\":1,\"patrolId\":25,\"patrolItemName\":\"检查是否能够联网?\",\"patrolDetail\":\"通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!\",\"taskId\":5 }]}";
                result = code + "?" + netResult;
                break;
            case AdbSocketUtils.UPLOAD_DB_COMMAND://上传数据库
                System.out.println("上传的数据：" + command);
                netResult = getMsgFromNet(AdbSocketUtils.HttpMethod.POST,AdbSocketUtils.UPLOAD_URL,command);
                result = code + "?" + netResult;
                break;
            case AdbSocketUtils.CLOSE_CONNECTION_COMMAND://手机主动关闭Socket连接
                NetResponseModel netResponseModel = new NetResponseModel();
                netResponseModel.setErroCode(AdbSocketUtils.NET_RESPONSE_SUC);
                result = code + "?"+netResponseModel.toString();
                break;
            case AdbSocketUtils.PRE_ONE_COMMANDE:
                AdbSocketUtils.sPreLen = Integer.valueOf(command);
                result = code + "?";
                break;
        }
        if (mSocketChannel != null && mSocketChannel.isConnected()) {
            try {
                if (code == AdbSocketUtils.GET_ALL_INFOS_COMMANDE) {//需要进入预传输
                    AdbSocketUtils.sPreStr = result;
                    int len = result.getBytes(AdbSocketUtils.CHARSET).length;
                    writeContent(AdbSocketUtils.PRE_ONE_COMMANDE + "?" + len, mSocketChannel);
                }else if(code == AdbSocketUtils.PRE_ONE_COMMANDE){//完成预传输命令
                    NetResponseModel netResponseModel = new NetResponseModel();
                    netResponseModel.setErroCode(AdbSocketUtils.NET_RESPONSE_SUC);
                    result = code + "?"+netResponseModel.toString();
                    writeContent(result, mSocketChannel);
                }else if(code == AdbSocketUtils.PRE_TWO_COMMANDE){//完成下载命令
                    writeContent(AdbSocketUtils.sPreStr, mSocketChannel);
                }else{
                    writeContent(result, mSocketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (code == AdbSocketUtils.CLOSE_CONNECTION_COMMAND) {
                //重启服务
                new Thread() {
                    @Override
                    public void run() {
                        stopWork();
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

    /**
     * 获取网络接口
     *
     * @param param
     * @return 如果是服务端返回错误，则格式为
     * @throws Exception
     */
    private String getMsgFromNet(AdbSocketUtils.HttpMethod method, String urlName, String param) {
        NetResponseModel netResponseModel = new NetResponseModel();
//        if(param!=null&&param.length()>0) {
        if (method == AdbSocketUtils.HttpMethod.GET) {//如果是POST数据
            try {
                param = URLEncoder.encode(param, AdbSocketUtils.CHARSET);
            } catch (UnsupportedEncodingException e) {
                System.out.println("[adb socket:]_______________________________encode错误");
                e.printStackTrace();
            }
        }
        System.out.println("[adb socket:]_______________________________请求网络encode后的参数为【" + param + "】");
        HttpURLConnection conn = null;
        try {
            conn = AdbSocketUtils.getNetUrl(method, urlName, param);
        } catch (IOException e) {
            e.printStackTrace();//连接失败
            netResponseModel.setErroCode(AdbSocketUtils.NET_FAIL_CONNEC_ERRO);
            netResponseModel.setContent(AdbSocketUtils.getErroInfoByCode(AdbSocketUtils.NET_FAIL_CONNEC_ERRO));
            return netResponseModel.toString();
        }
        if (method == AdbSocketUtils.HttpMethod.POST) {//如果是POST数据
            OutputStream outStream = null;
            try {
                outStream = conn.getOutputStream();
                outStream.write(param.getBytes(AdbSocketUtils.CHARSET));
                outStream.flush();
                outStream.close();
            } catch (IOException e) {//发送消息失败
                e.printStackTrace();
                netResponseModel.setErroCode(AdbSocketUtils.NET_REQUEST_FAIL_ERRO);
                netResponseModel.setContent(AdbSocketUtils.getErroInfoByCode(AdbSocketUtils.NET_REQUEST_FAIL_ERRO));
                return netResponseModel.toString();
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //获取返回状态
        int responseCode = -1;
        try {
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            netResponseModel.setErroCode(AdbSocketUtils.NET_RESPONSE_FAIL_ERRO);
            netResponseModel.setContent(AdbSocketUtils.getErroInfoByCode(AdbSocketUtils.NET_RESPONSE_FAIL_ERRO));
            return netResponseModel.toString();
        } else if (responseCode == 200) {

            InputStream inStream = null;
            boolean isErro = false;
            try {
                inStream = conn.getInputStream();
            } catch (SocketTimeoutException e) {//连接超时
                netResponseModel.setErroCode(AdbSocketUtils.NET_CONN_TIMEOUT_ERRO);
                netResponseModel.setContent(AdbSocketUtils.getErroInfoByCode(AdbSocketUtils.NET_CONN_TIMEOUT_ERRO));
                return netResponseModel.toString();
            } catch (IOException e) {
                e.printStackTrace();
                inStream = conn.getErrorStream();
                isErro = true;
            }
            byte[] data = null;
            try {
                data = readContentFromInputStream(inStream);
            } catch (IOException e) {
                e.printStackTrace();
                netResponseModel.setErroCode(AdbSocketUtils.NET_READ_RESPONSE_ERRO);
                netResponseModel.setContent(AdbSocketUtils.getErroInfoByCode(AdbSocketUtils.NET_READ_RESPONSE_ERRO));
                return netResponseModel.toString();
            }
            String result = null;
            try {
                result = new String(data, AdbSocketUtils.CHARSET);
            } catch (UnsupportedEncodingException e) {
                System.out.println("[adb socket:]_______________________________encode错误");
                e.printStackTrace();
            }
            System.out.println("[adb socket:]_______________________________获取返回结果【" + result + "】");
            if (isErro) {
                netResponseModel.setErroCode(AdbSocketUtils.NET_RESPONSE_ERRO);
                netResponseModel.setContent(result);
                return netResponseModel.toString();
            } else {
                netResponseModel.setErroCode(AdbSocketUtils.NET_RESPONSE_SUC);
                netResponseModel.setContent(result);
                return netResponseModel.toString();
            }
        } else {
            netResponseModel.setErroCode(responseCode);
            netResponseModel.setContent("服务端响应错误！【" + responseCode + "】");
            return netResponseModel.toString();
        }
//        }else{
//            netResponseModel.setErroCode(AdbSocketUtils.NET_NULL_PARAM_ERRO);
//            netResponseModel.setContent(AdbSocketUtils.getErroInfoByCode(AdbSocketUtils.NET_NULL_PARAM_ERRO));
//            return  netResponseModel.toString();
//        }
    }

    /**
     * Reads the contents of connection into a byte[].
     */
    private byte[] readContentFromInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        try {
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            bytes.close();
        }
    }
}
