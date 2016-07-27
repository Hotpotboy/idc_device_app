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
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.zhanghang.idcdevice.adbsocket.AdbSocketService;
import com.zhanghang.idcdevice.adbsocket.Request;
import com.zhanghang.idcdevice.db.DeviceTable;
import com.zhanghang.idcdevice.db.PandianResultTable;
import com.zhanghang.idcdevice.db.PatrolItemTable;
import com.zhanghang.idcdevice.db.TaskTable;
import com.zhanghang.idcdevice.mode.DBdata;
import com.zhanghang.idcdevice.mode.DeviceData;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.idcdevice.mode.UploadDBData;
import com.zhanghang.idcdevice.mode.pandian.PandianResultData;
import com.zhanghang.self.base.BaseApplication;
import com.zhanghang.self.db.BaseSQLiteHelper;
import com.zhanghang.self.utils.PopupWindowUtils;
import com.zhanghang.self.utils.PreferenceUtil;
import com.zxing.util.GenerateQRCode;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-27.
 */
public class DeviceApplication extends BaseApplication {
    private ArrayList<OnDataDownFinishedListener> mOnDataDownFinishedListeners = new ArrayList<>();
    private static String TAG = "DeviceApplication.class";
    /**
     * 监听USB插入广播
     */
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
        instance = this;
        Intent intent = new Intent(this, AdbSocketService.class);
        startService(intent);
        mUsbBroadcastReceiver = new USBBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_STATE");
        registerReceiver(mUsbBroadcastReceiver, intentFilter);
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
            //机房信息
            ArrayList<DeviceData> devices = dBdata.getIdcRooms();
            if (devices != null && devices.size() > 0) {
                for (DeviceData item : devices) {
                    try {
                        item.setId(BaseSQLiteHelper.getId());
                        DeviceTable.getDeviceTableInstance().insertData(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "机房信息【" + item.getDeviceId() + "," + item.getDeviceName() + "】插入数据库失败,原因：" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                tip = "机房信息为空!";
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
            } else {
                tip += "任务信息为空!";
            }
            //巡检项信息
            ArrayList<PatrolItemData> patrolItemDatas = dBdata.getPatrols();
            if (patrolItemDatas != null && patrolItemDatas.size() > 0) {
                for (PatrolItemData item : patrolItemDatas) {
                    try {
                        item.setId(BaseSQLiteHelper.getId());
                        if (item.getEnable() == -1) {
                            item.setEnable(1);//默认为启用
                        }
                        PatrolItemTable.getPatrolItemTableInstance().insertData(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "巡检项信息【" + item.getPatrolId() + "," + item.getPatrolItemName() + "】插入数据库失败,原因：" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                tip += "巡检项信息为空!";
            }
            if (!TextUtils.isEmpty(tip)) {
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
            ArrayList<PandianResultData> pandianResultDatas = PandianResultTable.getPandianTableInstance().selectAllDatas(PandianResultData.class);
            ArrayList<TaskData> taskDatas = TaskTable.getTaskTableInstance().selectAllDatas(TaskData.class);
            ArrayList<PatrolItemData> patrolItemDatas = PatrolItemTable.getPatrolItemTableInstance().selectAllDatas(PatrolItemData.class);
            UploadDBData dBdata = new UploadDBData();
//            dBdata.setDevices(deviceDatas);
            //上传巡检项信息
            dBdata.setPatrols(patrolItemDatas);
            //上传任务信息
            if (taskDatas != null && taskDatas.size() > 0) {
                for (TaskData taskData : taskDatas) {
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
            //上传盘点结果数据
            if (pandianResultDatas != null && pandianResultDatas.size() > 0) {
                ArrayList<DeviceData> pandianResults = new ArrayList<>();
                for (PandianResultData item : pandianResultDatas) {
                    DeviceData deviceData = item.converToDeviceData();
                    pandianResults.add(deviceData);
                }
                dBdata.setDevices(pandianResults);
            }
            //转换为字符串
            ObjectMapper objectMapper = new ObjectMapper();
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
        String[] fields = {"id", "dealInfo", "dealResult", "patrolItems", "realEndTime", "realStartTime", "taskState"};
        filters.addFilter(TaskData.class.getName(), SimpleBeanPropertyFilter.serializeAllExcept(fields));
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
            public void onSuccess(final String result) {
                ((TextView) netLoadingWindow.getViewById(R.id.net_loading_tip)).setText("获取数据成功，正在本地化......");
                AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            final DBdata dBdata = objectMapper.readValue(result, DBdata.class);
                            saveDatasFromPC(dBdata);
                            PreferenceUtil.updateStringInPreferce(instance, Const.PREFERENCE_FILE_NAME, Const.PREFERENCE_KEY_ALL_DATA_INFOS, "");//清空
                            PreferenceUtil.updateStringInPreferce(instance, Const.PREFERENCE_FILE_NAME, Const.PREFERENCE_KEY_ALL_DATA_INFOS, result);//更新
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(activity, "解析数据失败……", Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        netLoadingWindow.getPopupWindow().dismiss();
                        if (result) {
                            if (mOnDataDownFinishedListeners.size() > 0) {
                                for (OnDataDownFinishedListener item : mOnDataDownFinishedListeners)
                                    item.onDownorUploadFinish();
                            }
                        }
                    }
                };
                task.execute();

            }

            @Override
            public void onFail(String erroInfo) {
                netLoadingWindow.getPopupWindow().dismiss();
                Toast.makeText(activity, "与PC通信失败,【" + erroInfo + "】……", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 解析从二维码扫描页面返回的数据
     *
     * @param result    二维码扫描结果
     * @param asyncTask 此异步任务的执行参数为二维码扫描对应的值
     */
    public <U, R> void resolveScannerResult(String result, AsyncTask<String, U, R> asyncTask) {

        if (!TextUtils.isEmpty(result) && result.indexOf("&") >= 0) {
            final String[] resultArray = result.split("&");
            String md5Result = GenerateQRCode.getMD5(resultArray[1]);
            if (resultArray[0].equals(md5Result)) {//签名正确
                asyncTask.execute(resultArray[1]);
            } else {
                Toast.makeText(this, "二维码格式有误!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "二维码格式有误!", Toast.LENGTH_LONG).show();
        }
    }

    public void uploadDataToPc(final Activity activity){
        if(Const.isConnetionToPc()) {
            final PublicDialog dialog = new PublicDialog(activity);
            dialog.setContent("即将上传本地数据库!").showCancelButton(View.GONE, null).showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    final PopupWindowUtils netLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, activity, activity.getWindow().getDecorView(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    ((TextView) netLoadingWindow.getViewById(R.id.net_loading_tip)).setText("正在上传数据库......");
                    netLoadingWindow.showAtLocation();
                    final String datas = ((DeviceApplication) DeviceApplication.getInstance()).sendDataToPc();
                    if (TextUtils.isEmpty(datas)) {
                        netLoadingWindow.getPopupWindow().dismiss();
                        Toast.makeText(activity, "从数据库中获取的数据为空!", Toast.LENGTH_LONG).show();
                    } else {
                        Request.addRequestForCode(AdbSocketUtils.UPLOAD_DB_COMMAND, datas, new Request.CallBack() {
                            @Override
                            public void onSuccess(String result) {
                                netLoadingWindow.getPopupWindow().dismiss();
                                /**删除相关表*/
                                DeviceTable.getDeviceTableInstance().deleteTable();
                                TaskTable.getTaskTableInstance().deleteTable();
                                PatrolItemTable.getPatrolItemTableInstance().deleteTable();
                                PandianResultTable.getPandianTableInstance().deleteTable();
                                DBdata.cleanAllCached();//清空缓存
                                if (mOnDataDownFinishedListeners.size() > 0) {
                                    for (OnDataDownFinishedListener item : mOnDataDownFinishedListeners)
                                        item.onDownorUploadFinish();
                                }
                                Toast.makeText(activity, "成功上传数据库!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFail(String erroInfo) {
                                netLoadingWindow.getPopupWindow().dismiss();
                                Toast.makeText(activity, "上传数据库失败!原因【"+erroInfo+"】"+erroInfo, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).show();
        }else{
            Toast.makeText(activity, "上传数据之前，请用USB连接线与PC端相连接……", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 数据下载完成后回调接口
     */
    public interface OnDataDownFinishedListener {
        void onDownorUploadFinish();
    }
}
