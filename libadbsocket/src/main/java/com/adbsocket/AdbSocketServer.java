package com.adbsocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by Administrator on 2016-03-27.
 */
public class AdbSocketServer extends AdbSocketConnectionThread {
    private ServerSocketChannel mServerSocketChannel;

    @Override
    protected boolean onPreLoop() {
        try {
            mServerSocketChannel = ServerSocketChannel.open();
            mServerSocketChannel.configureBlocking(false);
            mServerSocketChannel.socket().bind(new InetSocketAddress(AdbSocketUtils.SERVER_PORT));
            mServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void acceptForChannal(ServerSocketChannel serverSocketChannel) throws IOException, InterruptedException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector,SelectionKey.OP_READ);
        System.out.println("[adb socket:]_______________________________接收到一个连接……");
    }

    @Override
    protected void connectionForChannal(SocketChannel socketChannel) throws IOException, InterruptedException {

    }

    @Override
    protected void writeForChannal(WritableByteChannel writableChannel) throws IOException, InterruptedException {

    }

    @Override
    protected void readForChannal(ReadableByteChannel readableChannel) throws IOException, InterruptedException {

    }
}
