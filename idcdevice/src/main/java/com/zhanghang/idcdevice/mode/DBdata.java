package com.zhanghang.idcdevice.mode;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.self.utils.PreferenceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2016-04-24.
 */
public class DBdata {
    /**所有的设备信息*/
    private ArrayList<DeviceData> devices;
    /**扫描二维码与设备信息的映射*/
    private static HashMap<String,DeviceData> mCachedDevices = new HashMap<>();
    /**所有的机柜信息*/
    private ArrayList<DeviceData> cabinets;
    /**扫描二维码与机柜信息的映射*/
    private static HashMap<String,DeviceData> mCachedCabinets = new HashMap<>();
    /**所有的机房信息*/
    private ArrayList<DeviceData> idcRooms;
    /**所有的任务信息*/
    private ArrayList<TaskData> tasks;
    /**所有的巡检项信息*/
    private ArrayList<PatrolItemData> patrols;

    static{
        String caced = PreferenceUtil.getStringInPreferce(DeviceApplication.getInstance(), Const.PREFERENCE_FILE_NAME,Const.PREFERENCE_KEY_ALL_DATA_INFOS,null);
        if(!TextUtils.isEmpty(caced)){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.readValue(caced,DBdata.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<PatrolItemData> getPatrols() {
        return patrols;
    }

    public void setPatrols(ArrayList<PatrolItemData> patrols) {
        this.patrols = patrols;
    }

    public ArrayList<TaskData> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<TaskData> tasks) {
        this.tasks = tasks;
    }

    public ArrayList<DeviceData> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<DeviceData> _devices) {
        this.devices = _devices;
        if(devices!=null&&devices.size()>0){
            mCachedDevices.clear();
            for(DeviceData item:devices){
                mCachedDevices.put(item.getAssetNum(),item);
            }
        }
    }

    public ArrayList<DeviceData> getCabinets() {
        return cabinets;
    }

    public void setCabinets(ArrayList<DeviceData> _cabinets) {
        this.cabinets = _cabinets;
        if(cabinets!=null&&cabinets.size()>0){
            mCachedCabinets.clear();
            for(DeviceData item:cabinets){
                mCachedCabinets.put(item.getAssetNum(),item);
            }
        }
    }

    public ArrayList<DeviceData> getIdcRooms() {
        return idcRooms;
    }

    public void setIdcRooms(ArrayList<DeviceData> idcRooms) {
        this.idcRooms = idcRooms;
    }

    /**
     * 根据二维码扫描结果查询对应的设备（机房信息）
     * @param assetNum   二维码扫描结果
     * @param type       0:表示获取设备信息；非0表示获取机柜信息
     * @return
     */
    public static DeviceData getDeviceDataFromCached(String assetNum,int type){
        HashMap<String,DeviceData> cache;
        if(type==0){//设备缓存
            cache = mCachedDevices;
        }else{//机柜缓存
            cache = mCachedCabinets;
        }
        return cache.get(assetNum);
    }

    public static void cleanAllCached(){
        PreferenceUtil.updateStringInPreferce(DeviceApplication.getInstance(), Const.PREFERENCE_FILE_NAME, Const.PREFERENCE_KEY_ALL_DATA_INFOS, "");//清空
        mCachedCabinets.clear();
        mCachedDevices.clear();
    }
}
