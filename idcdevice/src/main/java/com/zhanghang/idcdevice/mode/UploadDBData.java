package com.zhanghang.idcdevice.mode;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016-07-23.
 */
public class UploadDBData {
    /**所有的盘点的结果信息*/
    private ArrayList<DeviceData> devices;
    /**所有的任务信息*/
    private ArrayList<TaskData> tasks;
    /**所有的巡检项信息*/
    private ArrayList<PatrolItemData> patrols;

    public ArrayList<DeviceData> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<DeviceData> devices) {
        this.devices = devices;
    }

    public ArrayList<TaskData> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<TaskData> tasks) {
        this.tasks = tasks;
    }

    public ArrayList<PatrolItemData> getPatrols() {
        return patrols;
    }

    public void setPatrols(ArrayList<PatrolItemData> patrols) {
        this.patrols = patrols;
    }
}
