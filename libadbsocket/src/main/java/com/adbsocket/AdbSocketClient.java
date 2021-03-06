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

    static void initPipe() {
        try {
            sPie = Pipe.open();
            sPie.source().configureBlocking(false);
        } catch (IOException e) {
            AdbSocketUtils.printLog(true, e);
        }
    }

    public static void main(String[] args) {
        try{
            int type = Integer.valueOf(args[0]);
            AdbSocketUtils.setUrlType(type);
        }catch (Exception e){
        }
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
        AdbSocketUtils.printLog(true, "PC手机服务助手开始启动……");
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
                AdbSocketUtils.printLog(true, e);
            }
        }
        mScannerThread = null;
        mSocketThread = null;
        AdbSocketUtils.printLog(true, "PC手机服务助手停止……");
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
            AdbSocketUtils.printLog(true, "注册通道");
            sPie.source().register(selector, SelectionKey.OP_READ);
            synchronized (AdbSocketUtils.sLock) {
                isRegister = true;
                AdbSocketUtils.sLock.notify();
            }
            return true;
        } catch (ClosedChannelException e) {
            AdbSocketUtils.printLog(true, e);
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
            writeToAdbSocket(AdbSocketUtils.CONNECTIONED_COMMANDE + "", socketChannel);
            AdbSocketUtils.printLog(true, "建立连接成功……");
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
                AdbSocketUtils.printLog(true, "发现设备【" + deviceStr + "】");
                Device device = new Device(deviceStr);
//                if (!mDevices.contains(device)) {
                if (mSocketChannel == null) {
                    conncetion(device);//链接设备
                }
            }
        } else {
            super.readForChannal(readableChannel);
        }
    }

    private void conncetion(Device device) throws IOException, InterruptedException {
        Runtime.getRuntime().exec(AdbSocketScannerThread.ADB_PATH + " -s " + device.getDeviceId() + " forward tcp:" + device.getPort() + " tcp:" + AdbSocketUtils.SERVER_PORT);
        AdbSocketUtils.printLog(true, AdbSocketScannerThread.ADB_PATH + " -s " + device.getDeviceId() + " forward tcp:" + device.getPort() + " tcp:" + AdbSocketUtils.SERVER_PORT);
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
    protected void resolveCommand(int code, String command) {
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
                result = code + "?" + netResult;
                break;
            case AdbSocketUtils.UPLOAD_DB_COMMAND://上传数据库
                netResult = getMsgFromNet(AdbSocketUtils.HttpMethod.POST,AdbSocketUtils.UPLOAD_URL,command);
                result = code + "?" + netResult;
                break;
            case AdbSocketUtils.CLOSE_CONNECTION_COMMAND://手机主动关闭Socket连接
                NetResponseModel netResponseModel = new NetResponseModel();
                netResponseModel.setErroCode(AdbSocketUtils.NET_RESPONSE_SUC);
                result = code + "?"+netResponseModel.toString();
                break;
        }
        if (mSocketChannel != null && mSocketChannel.isConnected()) {
            try {
                writeToAdbSocket(result, mSocketChannel);
            } catch (IOException e) {
                AdbSocketUtils.printLog(true, e);
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
                AdbSocketUtils.printLog(true, "encode网络参数错误");
                AdbSocketUtils.printLog(true, e);
            }
        }
        AdbSocketUtils.printLog(false, "请求网络encode后的参数为【" + param + "】");
        HttpURLConnection conn = null;
        try {
            conn = AdbSocketUtils.getNetUrl(method, urlName, param);
        } catch (IOException e) {
            AdbSocketUtils.printLog(true, e);
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
                AdbSocketUtils.printLog(true, e);
                netResponseModel.setErroCode(AdbSocketUtils.NET_REQUEST_FAIL_ERRO);
                netResponseModel.setContent(AdbSocketUtils.getErroInfoByCode(AdbSocketUtils.NET_REQUEST_FAIL_ERRO));
                return netResponseModel.toString();
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        AdbSocketUtils.printLog(true, e);
                    }
                }
            }
        }
        //获取返回状态
        int responseCode = -1;
        try {
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            AdbSocketUtils.printLog(true, e);
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
                AdbSocketUtils.printLog(true, e);
                inStream = conn.getErrorStream();
                isErro = true;
            }
            byte[] data = null;
            try {
                data = readContentFromInputStream(inStream);
            } catch (IOException e) {
                AdbSocketUtils.printLog(true, e);
                netResponseModel.setErroCode(AdbSocketUtils.NET_READ_RESPONSE_ERRO);
                netResponseModel.setContent(AdbSocketUtils.getErroInfoByCode(AdbSocketUtils.NET_READ_RESPONSE_ERRO));
                return netResponseModel.toString();
            }
            String result = null;
            try {
                result = new String(data, AdbSocketUtils.CHARSET);
            } catch (UnsupportedEncodingException e) {
                AdbSocketUtils.printLog(true, "encode网络结果错误");
                AdbSocketUtils.printLog(true, e);
            }
            AdbSocketUtils.printLog(false, "获取返回结果【" + result + "】");
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
