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
    static {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 缓存设备
     */
    ArrayList<Device> mDevices = new ArrayList<>();

    @Override
    public void run(){
        try {
            setName("管理连接线程");
            if(!onPreLoop()) return;
            while (!isInterrupted()){
                int result = invokeSelector();
                if(result==-1) {
                    break;//some erros happens
                }else if(result==0){
                    continue;//no intresting thing
                }else{
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    iteratorSelectedKeys(selectedKeys);
                }
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择器轮询选择之前调用的方法
     * @return true标识选择器将轮询，false标识不轮询直接退出线程
     * @throws ClosedChannelException
     */
    protected abstract boolean onPreLoop();

    /**
     * 选择器进行选择操作
     * @return
     * @throws IOException
     */
    private int invokeSelector()  throws IOException {
        try {
           return selector.select();
        } catch (IOException e) {
            e.printStackTrace();
            selector.close();
            return -1;
        }
    }

    /**
     * 处理所有就绪状态的通道
     * @param selectionKeySet    所有的就绪状态的key的集合
     */
    private void iteratorSelectedKeys(Set<SelectionKey> selectionKeySet) {
        Iterator<SelectionKey> it = selectionKeySet.iterator();
        try {
        if(it!=null){
            while (it.hasNext()){
                SelectionKey item = it.next();
                if(item!=null){
                    it.remove();//删除已选的key，防止重复处理
                    if(item.isReadable()){
                        readForChannal((ReadableByteChannel) item.channel());
                    }else if(item.isWritable()){
                        writeForChannal((WritableByteChannel) item.channel());
                    }else if(item.isConnectable()){
                        connectionForChannal((SocketChannel) item.channel());
                    }else if(item.isAcceptable()){
                        acceptForChannal((ServerSocketChannel) item.channel());
                    }
                }
            }
        }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当服务端的socket接收到一个连接时调用
     * @param serverSocketChannel
     */
    protected abstract void acceptForChannal(ServerSocketChannel serverSocketChannel) throws IOException, InterruptedException;
    /**
     * 当客户端的socket成功连接时调用
     * @param socketChannel
     */
    protected abstract void connectionForChannal(SocketChannel socketChannel) throws IOException, InterruptedException;
    /**
     * 当一个通道可写时调用
     * @param writableChannel
     */
    protected abstract void writeForChannal(WritableByteChannel writableChannel) throws IOException, InterruptedException;
    /**
     * 当一个通道可读时调用
     * @param readableChannel
     */
    protected abstract void readForChannal(ReadableByteChannel readableChannel) throws IOException, InterruptedException;

    protected void writeContent(String content,WritableByteChannel writableByteChannel) throws IOException {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = ByteBuffer.wrap(content.getBytes(AdbSocketUtils.CHARSET));
        } catch (UnsupportedEncodingException e) {
            byteBuffer = ByteBuffer.wrap(content.getBytes());
        }
        if(byteBuffer!=null) {
            writableByteChannel.write(byteBuffer);
            byte[] contentBytes = new byte[byteBuffer.position()];
            byteBuffer.flip();
            byteBuffer.get(contentBytes);
            System.out.println("写入内容【"+new String(contentBytes)+"】到adb socket之中");
        }
    }
}
