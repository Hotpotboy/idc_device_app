package com.adbsocket;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by Administrator on 2016-03-27.
 */
public class AdbSocketUtils {
    static final Object sLock = new Object();
    static final int SERVER_PORT = 8080;
    static final String SERVER_IP = "127.0.0.1";
    public static final String CHARSET = "UTF-8";
    /**命令的位数*/
    public static final int COMMAND_LEN = 3;
    /**每一个命令的结束符*/
    public static final String END_COMMAND = "_END";
    /**建立链接命令*/
    public static final int CONNECTIONED_COMMANDE = 100;
}
