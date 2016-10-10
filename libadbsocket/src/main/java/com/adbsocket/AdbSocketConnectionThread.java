package com.adbsocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Administrator on 2016-03-26.
 */
public abstract class AdbSocketConnectionThread extends Thread {
    static Selector selector;

    static void initSelector() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            AdbSocketUtils.printLog(true, e);
        }
    }
    /**PC端与手机端的连接通道*/
    protected SocketChannel mSocketChannel;

    private boolean isStop;

    @Override
    public void run() {
        try {
            setName("管理连接线程");
            if (!onPreLoop()) return;
            setIsStop(true);
            while (!isInterrupted()) {
                dealWhenIDEL();
                int result = invokeSelector();
                if (result == -1) {
                    break;//some erros happens
                } else if (result == 0) {
                    continue;//no intresting thing
                } else {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    iteratorSelectedKeys(selectedKeys);
                }
            }
        } catch (ClosedChannelException e) {
            AdbSocketUtils.printLog(true, e);
        } catch (IOException e) {
            AdbSocketUtils.printLog(true, e);
        } finally {
            dealWhenIDEL();
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    AdbSocketUtils.printLog(true, e);
                } catch (Exception e) {
                    AdbSocketUtils.printLog(true, e);
                }
            }
            endThread();
            setIsStop(true);
        }
    }

    /**
     * 线程结束前，最后回调的函数
     */
    protected void endThread() {

    }

    /**
     * 选择器此次轮询无兴趣集合发生时回调的方法
     */
    protected void dealWhenIDEL() {

    }

    /**
     * 选择器轮询选择之前调用的方法
     *
     * @return true标识选择器将轮询，false标识不轮询直接退出线程
     * @throws ClosedChannelException
     */
    protected boolean onPreLoop() {
        initSelector();
        return true;
    }

    /**
     * 选择器进行选择操作
     *
     * @return
     * @throws IOException
     */
    private int invokeSelector() throws IOException {
        try {
            return selector.select(AdbSocketUtils.SLEEP_TIME/10);
        } catch (IOException e) {
            AdbSocketUtils.printLog(true, e);
            selector.close();
            return -1;
        }
    }

    /**
     * 处理所有就绪状态的通道
     *
     * @param selectionKeySet 所有的就绪状态的key的集合
     */
    private void iteratorSelectedKeys(Set<SelectionKey> selectionKeySet) {
        Iterator<SelectionKey> it = selectionKeySet.iterator();
        try {
            if (it != null) {
                while (it.hasNext()) {
                    SelectionKey item = it.next();
                    if (item != null) {
                        it.remove();//删除已选的key，防止重复处理
                        if (item.isReadable()) {
                            readForChannal((ReadableByteChannel) item.channel());
                        } else if (item.isWritable()) {
                            writeForChannal((WritableByteChannel) item.channel());
                        } else if (item.isConnectable()) {
                            connectionForChannal((SocketChannel) item.channel());
                        } else if (item.isAcceptable()) {
                            acceptForChannal((ServerSocketChannel) item.channel());
                        }
                    }
                }
            }
        } catch (IOException e) {
            AdbSocketUtils.printLog(true, e);
            if (e.toString().indexOf("远程主机强迫关闭了一个现有的连接") > 0
                    || e.toString().indexOf("Connection refused") > 0) {//USB连接断开
                dealConnectionCloseException();
            }
        } catch (InterruptedException e) {
            AdbSocketUtils.printLog(true, e);
        } catch (Exception e) {
            AdbSocketUtils.printLog(true, e);
        }
    }

    /**
     * 处理USB数据线断开时的异常
     */
    protected void dealConnectionCloseException(){
        if(mSocketChannel!=null) {
            try {
                selector.selectedKeys().remove(mSocketChannel.keyFor(selector));
                mSocketChannel.close();
            } catch (IOException e) {
                AdbSocketUtils.printLog(true, e);
            }
        }
        mSocketChannel = null;
    }

    /**
     * 当服务端的socket接收到一个连接时调用
     *
     * @param serverSocketChannel
     */
    protected abstract void acceptForChannal(ServerSocketChannel serverSocketChannel) throws IOException, InterruptedException;

    /**
     * 当客户端的socket成功连接时调用
     *
     * @param socketChannel
     */
    protected abstract void connectionForChannal(SocketChannel socketChannel) throws IOException, InterruptedException;

    /**
     * 当一个通道可写时调用
     *
     * @param writableChannel
     */
    protected abstract void writeForChannal(WritableByteChannel writableChannel) throws IOException, InterruptedException;

    /**
     * 当一个通道可读时调用
     *
     * @param readableChannel
     */
    protected void readForChannal(ReadableByteChannel readableChannel) throws IOException, InterruptedException{
        String command = readDataFromChannel(readableChannel);
        Object[] commands = resolveRawCommand(command);
        if (commands != null) {
            resolveCommand((int) commands[0], (String) commands[1]);
        }
    }

    /**
     * 解析接收到的命令
     *
     * @param code    命令码
     * @param command 除命令码之外的整个命令
     */
    protected abstract void resolveCommand(int code, String command);

    public static void writeToAdbSocket(String content, WritableByteChannel writableByteChannel) throws IOException {
        ByteBuffer byteBuffer = null;
        content += AdbSocketUtils.END_COMMAND;
        try {
            byteBuffer = ByteBuffer.wrap(content.getBytes(AdbSocketUtils.CHARSET));
        } catch (UnsupportedEncodingException e) {
            byteBuffer = ByteBuffer.wrap(content.getBytes());
        }
        if (byteBuffer != null) {
            int count;
            do{
                count = writableByteChannel.write(byteBuffer);
            }while (count>0);
            byte[] contentBytes = new byte[byteBuffer.position()];
            byteBuffer.flip();
            byteBuffer.get(contentBytes);
            AdbSocketUtils.printLog(false, "写入内容【" + new String(contentBytes) + "】到adb socket之中");
        }
    }

    /**
     * 从通道中读取数据
     *
     * @param readableChannel
     * @return
     * @throws IOException
     */
    protected String readDataFromChannel(ReadableByteChannel readableChannel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 500);
        byte[] dataFromChannel = new byte[0];
        int count;
        while ((count = readableChannel.read(byteBuffer)) >= 0) {
            if (count == 0) {//是否读取完毕
                String commandData = new String(dataFromChannel, AdbSocketUtils.CHARSET);
                Object[] commands = resolveRawCommand(commandData);
                if (commands != null && commands.length >= 2) {
                    //以是否获取结尾为标识，觉得是否继续登陆
                    if (commands[1]!=null&&!commands[1].toString().endsWith(AdbSocketUtils.END_COMMAND)) {//等待
                        continue;
                    } else {//结束
                        break;
                    }
                } else {
                    break;
                }
            }
            byteBuffer.flip();
            byte[] content = new byte[count];
            byteBuffer.get(content);
            dataFromChannel = mergeByteArray(dataFromChannel, content);
            byteBuffer.clear();
        }
        String commandData = new String(dataFromChannel, AdbSocketUtils.CHARSET);
        int index = commandData.lastIndexOf(AdbSocketUtils.END_COMMAND);
        if(index==-1){//通信数据的格式不对，重启手机助手程序
            dealConnectionCloseException();
            return null;
        }
        commandData = commandData.substring(0,index);
        return commandData;
    }

    private byte[] mergeByteArray(byte[] one, byte[] two) {
        byte[] result = null;
        if (one != null && one.length <= 0) result = two;
        if (two != null && two.length <= 0) result = one;
        if (one == null && two == null) return null;
        result = new byte[one.length + two.length];
        System.arraycopy(one, 0, result, 0, one.length);
        System.arraycopy(two, 0, result, one.length, two.length);
        return result;
    }

    /**
     * 解析原始命令内容
     *
     * @return
     */
    public static Object[] resolveRawCommand(String command) {
        if (AdbSocketUtils.isOKCommand(command)) {
            Object[] result = new Object[2];
            int commandNum = Integer.valueOf(command.substring(0, AdbSocketUtils.COMMAND_LEN));
            result[0] = commandNum;
            String commandParam = command.substring(AdbSocketUtils.COMMAND_LEN);
            if (commandParam != null && commandParam.startsWith("?")) {
                commandParam = commandParam.substring(1);
                result[1] = commandParam;
            } else {
                result[1] = null;
            }
            return result;
        }
        return null;
    }

    public synchronized boolean isStop() {
        return isStop;
    }

    public synchronized void setIsStop(boolean isStop) {
        this.isStop = isStop;
    }
}
