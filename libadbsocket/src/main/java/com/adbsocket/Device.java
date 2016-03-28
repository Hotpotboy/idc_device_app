package com.adbsocket;

/**
 * Created by Administrator on 2016-03-26.
 */
public class Device {
    private String deviceId;
    private int port;
    private String state;
    public Device(){

    }

    public Device(String info){
        String[] infos = info.split(",");
        deviceId = infos[0];
        port = Integer.valueOf(infos[1]);
        state = infos[2];
    }

    @Override
    public String toString(){
        return deviceId+","+port+","+state;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int _port) {
        this.port = _port;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof Device)) return false;
        if(object==null) return false;
        return deviceId.equals(((Device) object).getDeviceId());
    }
}
