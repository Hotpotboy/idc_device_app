package com.adbsocket;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class AdbSocketScannerThread extends Thread {
    public static final String ADB_PATH = new File("").getAbsolutePath()+File.separator+"libadbsocket"+File.separator+"adb"+File.separator+"adb";
    /**enter char*/
    private static final String ENTER_STR = System.getProperty("line.separator");
    /**\t char */
    private static final char T_CHAR =9;
    /**base port*/
    private static int port = 9090;
    private boolean isStop;

    @Override
    public void run(){
        setName("扫描设备线程");
        setIsStop(false);
        while (!isInterrupted()) {
            findDevices();
            try {
                Thread.sleep(AdbSocketUtils.SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        setIsStop(true);
    }

    /**
     * get result
     * @param channel
     * @return
     * @throws IOException
     */
    private String getResultFromAdb(ReadableByteChannel channel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        channel.read(byteBuffer);
        byte[] bytes = new byte[byteBuffer.position()];
        byteBuffer.flip();
        byteBuffer.get(bytes);
        return new String(bytes,"utf-8");
    }

    public void findDevices(){
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(ADB_PATH + " devices");
            ReadableByteChannel channel = Channels.newChannel(process.getInputStream());//get channel
            String result = getResultFromAdb(channel);
            System.out.println(result);
            if(result!=null&&result.indexOf(ENTER_STR)>=0) {
                String[] lines = result.split(ENTER_STR);
                //relove every line
                for (String item : lines) {
                    if(item.indexOf(T_CHAR)>0){
                        String[] deviceInfo = item.split(String.valueOf(T_CHAR));
                        if ("device".equals(deviceInfo[1])&&deviceInfo.length==2) {
                            Device device = new Device();
                            device.setDeviceId(deviceInfo[0]);
                            device.setState(deviceInfo[1]);
                            device.setPort(port);
                            byte[] infoBytes = device.toString().getBytes();
                            if(AdbSocketClient.sPie!=null){
                                ByteBuffer byteBuffer = ByteBuffer.wrap(infoBytes);
                                synchronized (AdbSocketUtils.sLock){
                                    if(!AdbSocketClient.isRegister){
                                        AdbSocketUtils.sLock.wait();
                                    }
                                }
                                System.out.println("往通道里写");
                                AdbSocketClient.sPie.sink().write(byteBuffer);//send data to connection thread by pipe
                            }
                            port++;
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

    public synchronized boolean isStop() {
        return isStop;
    }

    public synchronized void setIsStop(boolean isStop) {
        this.isStop = isStop;
    }
}
