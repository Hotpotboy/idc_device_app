package com.zhanghang.idcdevice.mode.pandian;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zhanghang.idcdevice.mode.BaseData;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-04-02.
 * 盘点结果数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PandianResultData extends BaseData implements Serializable {
    /**机房编码*/
    private String buildNum;
    /**机柜编码*/
    private String cupboardNum;
    /**设备编码*/
    private String deviceNum;
    private long id;

    public String getBuildNum() {
        return buildNum;
    }

    public void setBuildNum(String buildNum) {
        this.buildNum = buildNum;
    }

    public String getCupboardNum() {
        return cupboardNum;
    }

    public void setCupboardNum(String cupboardNum) {
        this.cupboardNum = cupboardNum;
    }

    public String getDeviceNum() {
        return deviceNum;
    }

    public void setDeviceNum(String deviceNum) {
        this.deviceNum = deviceNum;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
