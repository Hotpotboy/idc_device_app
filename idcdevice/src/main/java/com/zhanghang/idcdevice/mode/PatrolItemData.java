package com.zhanghang.idcdevice.mode;

import android.text.Editable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-04-02.
 * 巡检(维护)项数据膜拜
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatrolItemData extends BaseData  implements Serializable {
    /**此巡检项对应的设备类别*/
    private String deviceType;
    /**启动状态*/
    private int enable=-1;
    private long id;
    /**是否正常*/
    private int isNormal;
    /**巡检区域*/
    private String patrolArea;
    /**详细描述*/
    private String patrolDetail;
    /**巡检项Id*/
    private long patrolId;
    /**巡检项名称*/
    private String patrolItemName;
    /**巡检措施*/
    private String patrolStep;
    /**巡检指标*/
    private String patrolStuido;
    /**记录值*/
    private String recordValue;
    /**对应的任务ID*/
    private long taskId;

    public String getPatrolItemName() {
        return patrolItemName;
    }

    public void setPatrolItemName(String patrolItemName) {
        this.patrolItemName = patrolItemName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public String getPatrolStuido() {
        return patrolStuido;
    }

    public void setPatrolStuido(String patrolStuido) {
        this.patrolStuido = patrolStuido;
    }

    public String getPatrolStep() {
        return patrolStep;
    }

    public void setPatrolStep(String patrolStep) {
        this.patrolStep = patrolStep;
    }

    public String getPatrolDetail() {
        return patrolDetail;
    }

    public void setPatrolDetail(String patrolDetail) {
        this.patrolDetail = patrolDetail;
    }

    public String getPatrolArea() {
        return patrolArea;
    }

    public void setPatrolArea(String patrolArea) {
        this.patrolArea = patrolArea;
    }

    public long getPatrolId() {
        return patrolId;
    }

    public void setPatrolId(long patrolId) {
        this.patrolId = patrolId;
    }

    public int getNormal() {
        return isNormal;
    }

    public void setIsNormal(int isNormal) {
        this.isNormal = isNormal;
    }

    public String getRecordValue() {
        return recordValue;
    }

    public void setRecordValue(String recordValue) {
        this.recordValue = recordValue;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
}
