package com.zhanghang.idcdevice.adbsocket;

import android.util.SparseArray;

import com.adbsocket.AdbSocketConnectionThread;
import com.adbsocket.AdbSocketUtils;
import com.adbsocket.NetResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by Administrator on 2016-04-20.
 */
public class Request {
    /**
     * 数据未发送
     */
    public static final int STATUS_UNSEND = 1;
    /**
     * 已发送,正在等待响应
     */
    public static final int STATUS_WAITING = 2;
    /**
     * 完成
     */
    public static final int STATUS_END = 3;
    private static SparseArray<Request> sRequests = new SparseArray<>();
    /**
     * 此对象对应的请求码
     */
    private int code;
    /**
     * 当前请求的状态
     */
    private int mStatus = STATUS_UNSEND;
    /**
     * 请求内容（命令+数据）
     */
    private String mContent;
    /**
     * 开始请求的时间
     */
    private long mStartRequestTime;
    /**
     * 响应回调函数
     */
    private CallBack mCallBack;
    /**
     * 发送请求到PC端
     *
     * @param _code 命令码
     * @param param 参数，与HTTP地址?之后的参数格式一致，例如:username=123&password=123
     */
    public synchronized static void addRequestForCode(int _code, String param, CallBack callBack) {
        Request result = null;
        result = sRequests.get(_code);
        if (result == null) {
            result = new Request(_code, param, callBack);
            sRequests.put(_code, result);
        }
    }

    /**
     * 发送所有可发送的请求
     */
    synchronized static void  sendAllRequest(SocketChannel socketChannel) {
        int size = sRequests.size();
        if(size>0){
            for(int i=0;i<size;i++){
                Request request = sRequests.valueAt(i);
                if(request.mStatus==STATUS_UNSEND){//未发送
                    try {
                        AdbSocketConnectionThread.writeToAdbSocket(request.mContent,socketChannel);
                        request.unSendToWaiting();
                    } catch (IOException e) {
                        request.waitingToEnd(false,"连接关闭……");
                    }
                }
            }
        }
    }

    /**
     * 收到响应
     * @param code   发送时的命令码
     * @param params 响应结果
     */
    synchronized static void  recevieResult(int code,String params) {
        int size = sRequests.size();
        if(size>0){
            for(int i=0;i<size;i++){
                Request request = sRequests.valueAt(i);
                if(request.code==code&&request.mStatus==STATUS_WAITING){//已发送正在等待
                    request.waitingToEnd(true,params);
                }
            }
        }
    }

    private Request(int _code, String param, CallBack callBack) {
        code = _code;
        mContent = code + "?" + param;
        mStartRequestTime = System.currentTimeMillis();
        mCallBack = callBack;
    }

    /**
     * 将状态改变为已发送
     */
    void unSendToWaiting() {
        mStatus = STATUS_WAITING;
    }

    /**
     * 将状态改变为完成
     *
     * @param isSuc 是否成功访问PC
     * @param info  如果isSuc为true，则表示PC端返回的结果，否则为错误信息
     */
    void waitingToEnd(boolean isSuc, String info) {
        try {
            if (isSuc) {
                ObjectMapper objectMapper = new ObjectMapper();
                NetResponseModel netResponseMode = objectMapper.readValue(info,NetResponseModel.class);
                if(netResponseMode.getErroCode()== AdbSocketUtils.NET_RESPONSE_SUC) {
                    if (mCallBack != null) {
                        mCallBack.onSuccess(netResponseMode.getContent());
                    }
                }else{
                    if (mCallBack != null) {
                        mCallBack.onFail(netResponseMode.getContent());
                    }
                }
            } else {
                if (mCallBack != null) {
                    mCallBack.onFail(info);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (mCallBack != null) {
                mCallBack.onFail("解析错误!");
            }
        }
        sRequests.remove(code);//清空
    }

    public interface CallBack {
        void onSuccess(String result);

        void onFail(String erroInfo);
    }
}
