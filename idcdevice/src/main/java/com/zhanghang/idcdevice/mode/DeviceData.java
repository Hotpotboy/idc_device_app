package com.zhanghang.idcdevice.mode;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-04-02.
 * 设备数据模板
 */
public class DeviceData implements Serializable,Cloneable {
    /**设备ID*/
    private long deviceId;
    /**设备编号*/
    private String deviceNum;
    /**资产分类1*/
    private String assetType1;
    /**资产分类2*/
    private String assetType2;
    /**资产分类3*/
    private String assetType3;
    /**资产编号*/
    private String assetNum;
    /**实物资产编号*/
    private String entityAssetNum;
    /**资产序列号*/
    private String assetSerialNum;
    /**设备名称*/
    private String deviceName;//
    /**设备类型*/
    private String deviceModel;//
    /**城市*/
    private String city;
    /**机房（库房）*/
    private String idcRoom;
    /**机柜*/
    private String cabinet;
    /**位置*/
    private String position;

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceNum() {
        return deviceNum;
    }

    public void setDeviceNum(String deviceNum) {
        this.deviceNum = deviceNum;
    }

    public String getAssetType1() {
        return assetType1;
    }

    public void setAssetType1(String assetType1) {
        this.assetType1 = assetType1;
    }

    public String getAssetType2() {
        return assetType2;
    }

    public void setAssetType2(String assetType2) {
        this.assetType2 = assetType2;
    }

    public String getAssetType3() {
        return assetType3;
    }

    public void setAssetType3(String assetType3) {
        this.assetType3 = assetType3;
    }

    public String getAssetNum() {
        return assetNum;
    }

    public void setAssetNum(String assetNum) {
        this.assetNum = assetNum;
    }

    public String getEntityAssetNum() {
        return entityAssetNum;
    }

    public void setEntityAssetNum(String entityAssetNum) {
        this.entityAssetNum = entityAssetNum;
    }

    public String getAssetSerialNum() {
        return assetSerialNum;
    }

    public void setAssetSerialNum(String assetSerialNum) {
        this.assetSerialNum = assetSerialNum;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIdcRoom() {
        return idcRoom;
    }

    public void setIdcRoom(String idcRoom) {
        this.idcRoom = idcRoom;
    }

    public String getCabinet() {
        return cabinet;
    }

    public void setCabinet(String cabinet) {
        this.cabinet = cabinet;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        DeviceData result = (DeviceData) super.clone();
        result.setDeviceNum(TextUtils.isEmpty(getDeviceNum())?"":new String(getDeviceNum()));
        result.setDeviceName(TextUtils.isEmpty(getDeviceName()) ? "" : new String(getDeviceName()));
        result.setDeviceModel(TextUtils.isEmpty(getDeviceModel())?"":new String(getDeviceModel()));
        result.setEntityAssetNum(TextUtils.isEmpty(getEntityAssetNum())?"":new String(getEntityAssetNum()));
        result.setAssetNum(TextUtils.isEmpty(getAssetNum())?"":new String(getAssetNum()));
        result.setAssetSerialNum(TextUtils.isEmpty(getAssetSerialNum())?"":new String(getAssetSerialNum()));
        result.setAssetType1(TextUtils.isEmpty(getAssetType1())?"":new String(getAssetType1()));
        result.setAssetType2(TextUtils.isEmpty(getAssetType2())?"":new String(getAssetType2()));
        result.setAssetType3(TextUtils.isEmpty(getAssetType3())?"":new String(getAssetType3()));
        result.setCity(TextUtils.isEmpty(getCity())?"":new String(getCity()));
        result.setIdcRoom(TextUtils.isEmpty(getIdcRoom())?"":new String(getIdcRoom()));
        result.setCabinet(TextUtils.isEmpty(getCabinet())?"":new String(getCabinet()));
        result.setPosition(TextUtils.isEmpty(getPosition())?"":new String(getPosition()));
        return result;
    }
}
