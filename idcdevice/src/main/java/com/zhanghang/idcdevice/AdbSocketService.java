package com.zhanghang.idcdevice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.adbsocket.AdbSocketServer;
import com.adbsocket.AdbSocketUtils;
import com.zhanghang.self.utils.PreferenceUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by Administrator on 2016-03-27.
 */
public class AdbSocketService extends Service{
    public static final String IS_CONNECITION_PC_KEY = "is_connection_pc_key";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message){
            switch (message.what){
                case AdbSocketUtils.CONNECTIONED_COMMANDE:
                    Toast.makeText(DeviceApplication.getInstance(),"手机已连接电脑!",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    /**adb socket服务端*/
    private AdbSocketServer mAdbSocketServer;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flag,int startId){
        super.onStartCommand(intent,flag,startId);
        if(mAdbSocketServer ==null){
            mAdbSocketServer = new AdbSocketServer(){
                @Override
                protected void readForChannal(ReadableByteChannel readableChannel) throws IOException, InterruptedException {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(64);
                    byte[] dataFromChannel = new byte[0];
                    int count = 0;
                    while ((count=readableChannel.read(byteBuffer))>0){
                        byteBuffer.flip();
                        byte[] content = new byte[count];
                        byteBuffer.get(content);
                        dataFromChannel = mergeByteArray(dataFromChannel,content);
                        byteBuffer.clear();
                    }
                    String commandData = new String(dataFromChannel, AdbSocketUtils.CHARSET);
                    System.out.println("[adb socket:]_______________________________接收到电脑发送而来的内容【"+commandData+"】……");
                    //解析每一个命令
                    if(!TextUtils.isEmpty(commandData)) {
                        String[] commands = commandData.split(AdbSocketUtils.END_COMMAND);
                        for(String item:commands){
                            dealCommand(item);
                        }
                    }
                }
            };
            mAdbSocketServer.start();//开启监听线程
        }
        return START_STICKY;
    }

    /**
     * 处理命令
     */
    private void dealCommand(String command){
        if(!TextUtils.isEmpty(command)&&command.length()>=AdbSocketUtils.COMMAND_LEN){
            int commandNum = Integer.valueOf(command.substring(0,AdbSocketUtils.COMMAND_LEN));
            if(commandNum==AdbSocketUtils.CONNECTIONED_COMMANDE){//连接命令
                PreferenceUtil.updateBooleanInPreferce(DeviceApplication.getInstance(), DeviceApplication.getInstance().getVersionName(), IS_CONNECITION_PC_KEY, true);
                mHandler.sendEmptyMessage(AdbSocketUtils.CONNECTIONED_COMMANDE);
            }
        }
    }

    private byte[] mergeByteArray(byte[] one,byte[] two){
        byte[] result = null;
        if(one!=null&&one.length<=0) result = two;
        if(two!=null&&two.length<=0) result = one;
        if(one==null&&two==null) return null;
        result = new byte[one.length+two.length];
        System.arraycopy(one,0,result,0,one.length);
        System.arraycopy(two,0,result,one.length,two.length);
        return result;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mAdbSocketServer.interrupt();
        mAdbSocketServer=null;
    }
}
