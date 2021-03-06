package com.adbsocket;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2016-03-27.
 */
public class AdbSocketUtils {
    private static int sUrlType = 0;
    static final Object sLock = new Object();
    static final int SERVER_PORT = 8080;
    static final String SERVER_IP = "127.0.0.1";
    public static final String CHARSET = "UTF-8";
    /**
     * 日志
     */
    private static Logger sLog;
    /**日志级别,
     * FINER显示所有的日志；
     * FINE显示除异常之外所有的日志；
     * INFO只显示方法{@link #printLog(boolean, Object)}第一个参数为true的日志
     * */
    private static Level sLevel = Level.FINER;
    /**
     * 登陆接口
     */
    public static final String LOGIN_IN_URL = "idc-webapp/user/login";
    /**
     * 登出接口
     */
    public static final String LOGIN_OUT_URL = "idc-webapp/user/logout";
    /**
     * 下载数据接口
     */
    public static final String DOWNLOAD_URL = "idc-webapp/task/queryTask";
    /**
     * 上传数据接口
     */
    public static final String UPLOAD_URL = "idc-webapp/task/uploadTask";
    /**
     * 线程休眠毫秒数
     */
    public static int SLEEP_TIME = 5000;
    /**
     * 命令的位数
     */
    public static final int COMMAND_LEN = 3;
    /**
     * 每一个命令的结束符
     */
    public static final String END_COMMAND = "_ADBCONNECTIONEND";
    /**
     * 建立链接命令
     */
    public static final int CONNECTIONED_COMMANDE = 600;
    /**
     * 连接丢失
     */
    public static final int CONNECTION_LOST_COMMANDE = 601;
    /**
     * PC_返回命令
     */
    public static final int PC_RETURN_COMMAND = 602;
    /**
     * 登陆命令,登陆命令的格式为{“userName”:”yhm”,”password”:”123456"}
     */
    public static final int LOGIN_IN_COMMANDE = PC_RETURN_COMMAND + 1;
    /**
     * 获取巡检、设备信息的命令，此命令分为两步，第一步先传递获取内容的总长度，第二步在传递获取内容
     */
    public static final int GET_ALL_INFOS_COMMANDE = PC_RETURN_COMMAND + 2;
    /**
     * 上传数据库命令，此命令分为两步，第一步先传递上传数据的总长度，第二步在传递上传数据
     */
    public static final int UPLOAD_DB_COMMAND = PC_RETURN_COMMAND + 3;
    /**
     * 手机关闭连接命令
     */
    public static final int CLOSE_CONNECTION_COMMAND = PC_RETURN_COMMAND + 4;
    /**
     * 登出命令
     */
    public static final int LOGIN_OUT_COMMANDE = PC_RETURN_COMMAND + 5;
    /**
     * 与网络服务端连接，参数为空错误
     */
    public static final int NET_NULL_PARAM_ERRO = 800;
    /**
     * 与网络服务端连接，服务器连接失败错误
     */
    public static final int NET_FAIL_CONNEC_ERRO = NET_NULL_PARAM_ERRO + 1;
    /**
     * 与网络服务端连接，发送请求失败错误
     */
    public static final int NET_REQUEST_FAIL_ERRO = NET_NULL_PARAM_ERRO + 2;
    /**
     * 与网络服务端连接，连接超时错误
     */
    public static final int NET_CONN_TIMEOUT_ERRO = NET_NULL_PARAM_ERRO + 3;
    /**
     * 与网络服务端连接，不能获取响应错误
     */
    public static final int NET_RESPONSE_FAIL_ERRO = NET_NULL_PARAM_ERRO + 4;
    /**
     * 与网络服务端连接，读取响应错误错误
     */
    public static final int NET_READ_RESPONSE_ERRO = NET_NULL_PARAM_ERRO + 5;
    /**
     * 与网络服务端连接，来自于网络服务程序的错误
     */
    public static final int NET_RESPONSE_ERRO = NET_NULL_PARAM_ERRO + 6;
    /**
     * 与网络服务端连接，通信成功
     */
    public static final int NET_RESPONSE_SUC = NET_NULL_PARAM_ERRO + 7;
    /**
     * 错误码与描述信息的映射数组，以{@link #NET_NULL_PARAM_ERRO}为基数索引
     */
    static final String[] NET_ERRO_INFOS = {"请求参数为空!", "与网络接口所在的服务端连接失败!", "发送请求失败!",
            "连接超时，请检查网络!", "不能从HTTP连接中获取响应!", "读取响应数据错误!", ""};

    /**
     * 根据错误码获取对应的错误描述
     *
     * @param code
     * @return
     */
    public static String getErroInfoByCode(int code) {
        if (code >= NET_NULL_PARAM_ERRO && code < NET_ERRO_INFOS.length + NET_NULL_PARAM_ERRO) {
            return NET_ERRO_INFOS[code - NET_NULL_PARAM_ERRO];
        } else {
            return null;
        }
    }

    /**
     * 默认为0，即http://115.28.15.123:8080/
     * 如果为1，则地址为http://21.5.18.90:8088/
     * 如果为2，则地址为http://21.2.6.239:8088/
     * @param type
     */
    public static void setUrlType(int type){
        if(type<0) type=0;
        if(type>2) type=0;
        sUrlType = type;
        AdbSocketUtils.printLog(true, "设置网络环境为:"+sUrlType);
    }

    /**
     * 获取网络接口的连接
     *
     * @param method  访问接口的方式（POST、GET方式）
     * @param urlName 接口名
     * @param param   接口的参数或者上传的数据
     * @return
     */
    public static HttpURLConnection getNetUrl(HttpMethod method, String urlName, String param) throws IOException {
        String path = "http://115.28.15.123:8080/";//"http://21.5.18.90:8088/";//"http://115.28.15.123:8080/";//"http://21.2.6.239:8088/"
        if(sUrlType==1){
            path = "http://21.5.18.90:8088/";
        }else if(sUrlType==2){
            path = "http://21.2.6.239:8088/";
        }
        if (method == HttpMethod.GET) path += (urlName + "?" + param);
        else if (method == HttpMethod.POST) path += urlName;
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        if (method == HttpMethod.GET) conn.setRequestMethod("GET");
        else if (method == HttpMethod.POST) {
            byte[] data = param.getBytes(CHARSET);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            // application/x-javascript text/xml->xml数据
            // application/x-javascript->json对象
            // application/x-www-form-urlencoded->表单数据
            conn.setRequestProperty("Content-Type", "application/json;charset=" + CHARSET);
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        }
        return conn;
    }

    public static boolean isDigitsOnly(CharSequence str) {
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 命令是否合格
     *
     * @param command
     * @return
     */
    public static boolean isOKCommand(String command) {
        return command != null && command.length() >= AdbSocketUtils.COMMAND_LEN;
    }

    /**
     * 打印日志
     *
     * @param isShowScreen 是否显示在屏幕上
     * @param info         日志信息
     */
    public static void printLog(boolean isShowScreen, Object info) {
        if (sLog == null) {
            sLog = Logger.getLogger("PCHelper");
            ConsoleHandler consoleHandler = new ConsoleHandler();
            sLog.addHandler(consoleHandler);
            sLog.setLevel(sLevel);
        }
        String log = "";
        if (info instanceof Exception&&sLevel==Level.FINER) {
            ((Exception) info).printStackTrace();
            return;
        } else if(info instanceof Exception&&sLevel!=Level.FINER){
            return;
        }else {
            log = (String) info;
        }
        if (isShowScreen) sLog.info("[adb socket:info]____________________" + log);
        else sLog.fine("[adb socket:debug]____________________" + log);
    }

    public enum HttpMethod {
        GET,
        POST
    }
}
