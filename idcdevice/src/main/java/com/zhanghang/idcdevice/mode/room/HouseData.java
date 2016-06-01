package com.zhanghang.idcdevice.mode.room;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zhanghang.idcdevice.mode.BaseData;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-04-02.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HouseData extends BaseData implements Serializable {
    /**楼栋名*/
    private String buildName;
    /**楼层名*/
    private String floorName;
    private long houseId;
    /**房间名*/
    private String name;
    /**二维码*/
    private String qrCode;

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getHouseId() {
        return houseId;
    }

    public void setHouseId(long houseId) {
        this.houseId = houseId;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }
}
