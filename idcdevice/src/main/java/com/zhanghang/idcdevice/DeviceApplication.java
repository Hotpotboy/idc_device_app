package com.zhanghang.idcdevice;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adbsocket.AdbSocketUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.zhanghang.idcdevice.adbsocket.AdbSocketService;
import com.zhanghang.idcdevice.adbsocket.Request;
import com.zhanghang.idcdevice.db.DeviceTable;
import com.zhanghang.idcdevice.db.PatrolItemTable;
import com.zhanghang.idcdevice.db.TaskTable;
import com.zhanghang.idcdevice.mode.DBdata;
import com.zhanghang.idcdevice.mode.DeviceData;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.base.BaseApplication;
import com.zhanghang.self.db.BaseSQLiteHelper;
import com.zhanghang.self.utils.PopupWindowUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-27.
 */
public class DeviceApplication extends BaseApplication {
    private ArrayList<OnDataDownFinishedListener> mOnDataDownFinishedListeners = new ArrayList<>();
    private static String TAG = "DeviceApplication.class";
    /**监听USB插入广播*/
    private USBBroadcastReceiver mUsbBroadcastReceiver;

    public void addDataDownFinishedListener(OnDataDownFinishedListener listener) {
        if (!mOnDataDownFinishedListeners.contains(listener)) {
            mOnDataDownFinishedListeners.add(listener);
        }
    }

    public void removeDataDownFinishedListener(OnDataDownFinishedListener listener) {
        if (mOnDataDownFinishedListeners.contains(listener)) {
            mOnDataDownFinishedListeners.remove(listener);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, AdbSocketService.class);
        startService(intent);
        mUsbBroadcastReceiver = new USBBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_STATE");
        registerReceiver(mUsbBroadcastReceiver,intentFilter);
    }

    public void stop(final Activity activity) {
        mOnDataDownFinishedListeners.clear();
        unregisterReceiver(mUsbBroadcastReceiver);
        if (Const.isConnetionToPc()) {
            Request.addRequestForCode(AdbSocketUtils.CLOSE_CONNECTION_COMMAND, "", null);
            activity.finish();
            activity.moveTaskToBack(false);
        } else {
            activity.finish();
            activity.moveTaskToBack(false);
        }
    }

    /**
     * 保存来至于PC的数据
     */
    public void saveDatasFromPC(DBdata dBdata) {
        if (dBdata != null) {
            String tip = "";
            //设备信息
            ArrayList<DeviceData> devices = dBdata.getDevices();
            if (devices != null && devices.size() > 0) {
                for (DeviceData item : devices) {
                    try {
                        item.setId(BaseSQLiteHelper.getId());
                        DeviceTable.getDeviceTableInstance().insertData(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "设备信息【" + item.getDeviceId() + "," + item.getDeviceName() + "】插入数据库失败,原因：" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }else{
                tip="设备信息为空!";
            }
            //任务信息
            ArrayList<TaskData> tasks = dBdata.getTasks();
            if (tasks != null && tasks.size() > 0) {
                for (TaskData item : tasks) {
                    try {
                        item.setId(BaseSQLiteHelper.getId());
                        TaskTable.getTaskTableInstance().insertData(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "任务信息【" + item.getTaskId() + "," + item.getTaskName() + "】插入数据库失败,原因：" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }else{
                tip+="任务信息为空!";
            }
            //巡检项信息
            ArrayList<PatrolItemData> patrolItemDatas = dBdata.getPatrols();
            if (patrolItemDatas != null && patrolItemDatas.size() > 0) {
                for (PatrolItemData item : patrolItemDatas) {
                    try {
                        item.setId(BaseSQLiteHelper.getId());
                        if(item.getEnable()==-1){
                            item.setEnable(1);//默认为启用
                        }
                        PatrolItemTable.getPatrolItemTableInstance().insertData(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "巡检项信息【" + item.getPatrolId() + "," + item.getPatrolItemName() + "】插入数据库失败,原因：" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }else{
                tip+="巡检项信息为空!";
            }
            if(!TextUtils.isEmpty(tip)){
                Toast.makeText(this, tip, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 是否已经上传数据了~
     *
     * @return
     */
    public boolean isUploadData() {
        try {
            ArrayList<DeviceData> deviceDatas = DeviceTable.getDeviceTableInstance().selectAllDatas(DeviceData.class);
            ArrayList<TaskData> taskDatas = TaskTable.getTaskTableInstance().selectAllDatas(TaskData.class);
            ArrayList<PatrolItemData> patrolItemDatas = PatrolItemTable.getPatrolItemTableInstance().selectAllDatas(PatrolItemData.class);
            return (deviceDatas == null || deviceDatas.size() <= 0) && (taskDatas == null || taskDatas.size() <= 0) && (patrolItemDatas == null || patrolItemDatas.size() <= 0);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 发送数据到PC端
     *
     * @return
     */
    public String sendDataToPc() {
        String result = null;
        try {
            ArrayList<DeviceData> deviceDatas = DeviceTable.getDeviceTableInstance().selectAllDatas(DeviceData.class);
            ArrayList<TaskData> taskDatas = TaskTable.getTaskTableInstance().selectAllDatas(TaskData.class);
            ArrayList<PatrolItemData> patrolItemDatas = PatrolItemTable.getPatrolItemTableInstance().selectAllDatas(PatrolItemData.class);
            DBdata dBdata = new DBdata();
            dBdata.setDevices(deviceDatas);
            dBdata.setPatrols(patrolItemDatas);
            if(taskDatas!=null&&taskDatas.size()>0){
                for(TaskData taskData:taskDatas){
                    taskData.setTaskState("2");
//                    String status = taskData.getTaskState();
//                    if(Const.TASK_STATE_DEALED.equals(status)){
//                        taskData.setTaskState("2");
//                    }else{
//                        taskData.setTaskState("1");
//                    }
                }
            }
            dBdata.setTasks(taskDatas);
            //转换为字符串
            ObjectMapper objectMapper = new ObjectMapper();
//            filterFileds(objectMapper);
            result = objectMapper.writeValueAsString(dBdata);
            Log.i(TAG, "上传数据长度【" + result.length() + "】");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "从数据库提取数据失败……" + e.toString(), Toast.LENGTH_LONG).show();
        } finally {
            return result;
        }
    }

    private void filterFileds(ObjectMapper mapper) {
        SimpleFilterProvider filters = new SimpleFilterProvider();
        String[] fields = {"id","dealInfo","dealResult","patrolItems","realEndTime","realStartTime","taskState"};
        filters.addFilter(TaskData.class.getName(),SimpleBeanPropertyFilter.serializeAllExcept(fields));
        mapper.setFilters(filters);
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findFilterId(AnnotatedClass ac) {
                return ac.getName();
            }
        });
    }


    /**
     * 从PC端获取数据
     */
    public void getDataFromPC(final Activity activity) {
        final PopupWindowUtils netLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, activity, activity.getWindow().getDecorView(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((TextView) netLoadingWindow.getViewById(R.id.net_loading_tip)).setText("正在获取数据中......");
        netLoadingWindow.showAtLocation();
        Request.addRequestForCode(AdbSocketUtils.GET_ALL_INFOS_COMMANDE, "", new Request.CallBack() {
            @Override
            public void onSuccess(String result) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    final DBdata dBdata = objectMapper.readValue(result, DBdata.class);
                    ((TextView) netLoadingWindow.getViewById(R.id.net_loading_tip)).setText("获取数据成功，正在本地化......");
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            saveDatasFromPC(dBdata);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            netLoadingWindow.getPopupWindow().dismiss();
                            if (mOnDataDownFinishedListeners.size() > 0) {
                                for (OnDataDownFinishedListener item : mOnDataDownFinishedListeners)
                                    item.onDown();
                            }
                        }
                    };
                    task.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "解析数据失败……", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFail(String erroInfo) {
                netLoadingWindow.getPopupWindow().dismiss();
                Toast.makeText(activity, "与PC通信失败,【" + erroInfo + "】……", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 数据下载完成后回调接口
     */
    public interface OnDataDownFinishedListener {
        public void onDown();
    }
}
