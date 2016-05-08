package com.adbsocket;

/**
 * Created by Administrator on 2016-03-27.
 */
public class AdbSocketUtils {
    static final Object sLock = new Object();
    static final int SERVER_PORT = 8080;
    static final String SERVER_IP = "127.0.0.1";
    public static final String CHARSET = "UTF-8";
    /**线程休眠毫秒数*/
    public static int SLEEP_TIME = 5000;
    /**命令的位数*/
    public static final int COMMAND_LEN = 3;
    /**每一个命令的结束符*/
//    public static final String END_COMMAND = "_END";
    /**建立链接命令*/
    public static final int CONNECTIONED_COMMANDE = 100;
    /**连接丢失*/
    public static final int CONNECTION_LOST_COMMANDE = 101;
    /**PC_返回命令*/
    public static final int PC_RETURN_COMMAND = 102;
    /**登陆命令,登陆命令的格式为102?userName=123&password=123_END*/
    public static final int LOGIN_IN_COMMANDE = PC_RETURN_COMMAND+1;
    /**获取巡检、设备信息的命令*/
    public static final int GET_ALL_INFOS_COMMANDE = PC_RETURN_COMMAND + 2;
    /**上传数据库命令*/
    public static final int UPLOAD_DB_COMMAND = PC_RETURN_COMMAND+3;
    /**手机关闭连接命令*/
    public static final int CLOSE_CONNECTION_COMMAND = PC_RETURN_COMMAND + 4;

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
     * @param command
     * @return
     */
    public static boolean isOKCommand(String command){
        return command!=null&&command.length()>=AdbSocketUtils.COMMAND_LEN;
    }
}
