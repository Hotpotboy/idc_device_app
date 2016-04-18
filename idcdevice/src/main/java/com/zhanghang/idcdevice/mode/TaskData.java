package com.zhanghang.idcdevice.mode;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016-04-02.
 */
public class TaskData implements Serializable {
    /**任务ID*/
    private long taskId;
    /**任务类型*/
    private String taskType;
    /**任务状态*/
    private String taskState;
    /**负责组*/
    private String responseGroup;
    /**负责人*/
    private String responsePeople;
    /**计划开始时间*/
    private long planedStartTime;
    /**计划结束时间*/
    private long planedEndTime;
    /**实际开始时间*/
    private long realStartTime;
    /**实际结束时间*/
    private long realEndTime;
    /**任务处理信息*/
    private String dealInfo;
    /**处理结果*/
    private String dealResult;
    /**实施组*/
    private String dealGroup;
    /**实施人*/
    private String dealPeople;
    /**任务名称*/
    private String taskName;
    /**详细描述*/
    private String details;
    /**巡检项目*/
    private ArrayList<PatrolItemData> patrolItems;

    public ArrayList<PatrolItemData> getPatrolItems() {
        return patrolItems;
    }

    public void setPatrolItems(ArrayList<PatrolItemData> patrolItems) {
        this.patrolItems = patrolItems;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDealPeople() {
        return dealPeople;
    }

    public void setDealPeople(String dealPeople) {
        this.dealPeople = dealPeople;
    }

    public String getDealGroup() {
        return dealGroup;
    }

    public void setDealGroup(String dealGroup) {
        this.dealGroup = dealGroup;
    }

    public String getDealResult() {
        return dealResult;
    }

    public void setDealResult(String dealResult) {
        this.dealResult = dealResult;
    }

    public String getDealInfo() {
        return dealInfo;
    }

    public void setDealInfo(String dealInfo) {
        this.dealInfo = dealInfo;
    }

    public long getRealEndTime() {
        return realEndTime;
    }

    public void setRealEndTime(long realEndTime) {
        this.realEndTime = realEndTime;
    }

    public long getRealStartTime() {
        return realStartTime;
    }

    public void setRealStartTime(long realStartTime) {
        this.realStartTime = realStartTime;
    }

    public long getPlanedEndTime() {
        return planedEndTime;
    }

    public void setPlanedEndTime(long planedEndTime) {
        this.planedEndTime = planedEndTime;
    }

    public long getPlanedStartTime() {
        return planedStartTime;
    }

    public void setPlanedStartTime(long planedStartTime) {
        this.planedStartTime = planedStartTime;
    }

    public String getResponsePeople() {
        return responsePeople;
    }

    public void setResponsePeople(String responsePeople) {
        this.responsePeople = responsePeople;
    }

    public String getResponseGroup() {
        return responseGroup;
    }

    public void setResponseGroup(String responseGroup) {
        this.responseGroup = responseGroup;
    }

    public String getTaskState() {
        return taskState;
    }

    public void setTaskState(String taskState) {
        this.taskState = taskState;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
}
