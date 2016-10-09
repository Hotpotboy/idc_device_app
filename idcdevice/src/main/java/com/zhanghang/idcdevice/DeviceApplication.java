package com.zhanghang.idcdevice;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adbsocket.AdbSocketUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-27.
 */
public class DeviceApplication extends BaseApplication {
    private static ArrayList<OnDataDownFinishedListener> mOnDataDownFinishedListeners = new ArrayList<>();
    private static String TAG = "DeviceApplication.class";
    /**
     * 监听USB插入广播
     */
    private USBBroadcastReceiver mUsbBroadcastReceiver;

    private Handler mHandler = new Handler();

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
                        String selection = DeviceTable.getDeviceTableInstance().getComlueInfos()[8].getName() + " = ?";
                        String[] args = {item.getDeviceId() + ""};
                        int count = DeviceTable.getDeviceTableInstance().selectDatas(selection, args, null, null, null, DeviceData.class).size();
                        if (count > 0) continue;
                        item.setId(BaseSQLiteHelper.getId());
                        DeviceTable.getDeviceTableInstance().insertData(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "机房信息【" + item.getDeviceId() + "," + item.getDeviceName() + "】插入数据库失败,原因：" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
//                tip = "机房信息为空!";
            }
            //任务信息
            ArrayList<TaskData> tasks = dBdata.getTasks();
            if (tasks != null && tasks.size() > 0) {
                for (TaskData item : tasks) {
                    try {
                        String selection = TaskTable.getTaskTableInstance().getComlueInfos()[16].getName() + " = ?";
                        String[] args = {item.getTaskId() + ""};
                        int count = TaskTable.getTaskTableInstance().selectDatas(selection, args, null, null, null, TaskData.class).size();
                        if (count > 0) continue;
                        item.setId(BaseSQLiteHelper.getId());
                        TaskTable.getTaskTableInstance().insertData(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "任务信息【" + item.getTaskId() + "," + item.getTaskName() + "】插入数据库失败,原因：" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                tip += "没有新的任务需要被更新!";
            }
            //巡检项信息
            ArrayList<PatrolItemData> patrolItemDatas = dBdata.getPatrols();
            if (patrolItemDatas != null && patrolItemDatas.size() > 0) {
                for (PatrolItemData item : patrolItemDatas) {
                    try {
                        String selection = PatrolItemTable.getPatrolItemTableInstance().getComlueInfos()[2].getName() + " = ?";
                        String[] args = {item.getId() + ""};
                        int count = PatrolItemTable.getPatrolItemTableInstance().selectDatas(selection, args, null, null, null, PatrolItemData.class).size();
                        if (count > 0) continue;
                        if (item.getEnable() == -1) {
                            item.setEnable(1);//默认为启用
                        }
                        PatrolItemTable.getPatrolItemTableInstance().insertData(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "巡检项信息【" + item.getId() + "," + item.getPatrolItemName() + "】插入数据库失败,原因：" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
//                tip += "巡检项信息为空!";
            }
            if (!TextUtils.isEmpty(tip)) {
                final String finalTip = tip;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(instance, finalTip, Toast.LENGTH_LONG).show();
                    }
                });
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
    public String sendDataToPc(String taskId) {
        String result = null;
        try {
            UploadDBData dBdata = new UploadDBData();
            //上传任务信息
            ArrayList<TaskData> taskDatas = null;
            if (TextUtils.isEmpty(taskId)) {
                taskDatas = TaskTable.getTaskTableInstance().selectAllDatas(TaskData.class);
            } else {
                String selection = TaskTable.getTaskTableInstance().getComlueInfos()[16].getName() + " = ?";
                String[] args = {taskId};
                taskDatas = TaskTable.getTaskTableInstance().selectDatas(selection, args, null, null, null, TaskData.class);
            }
            if (taskDatas != null && taskDatas.size() > 0) {
                for (TaskData taskData : taskDatas) {
                    String stateStr = taskData.getTaskState();
                    String value = Const.TASK_STATE_DEALED.equals(stateStr) ? "2" : "1";
                    taskData.setTaskState(value);
                }
            }
            dBdata.setTasks(taskDatas == null ? new ArrayList<TaskData>() : taskDatas);
            //上传巡检项信息
            ArrayList<PatrolItemData> patrolItemDatas = null;
            if (TextUtils.isEmpty(taskId)) {
                patrolItemDatas = PatrolItemTable.getPatrolItemTableInstance().selectAllDatas(PatrolItemData.class);
            } else {
                String selection = PatrolItemTable.getPatrolItemTableInstance().getComlueInfos()[11].getName() + " = ?";
                String[] args = {taskId};
                patrolItemDatas = PatrolItemTable.getPatrolItemTableInstance().selectDatas(selection, args, null, null, null, PatrolItemData.class);
            }
            dBdata.setPatrols(patrolItemDatas == null ? new ArrayList<PatrolItemData>() : patrolItemDatas);
            //上传盘点结果数据
            ArrayList<PandianResultData> pandianResultDatas = null;
            if (TextUtils.isEmpty(taskId)) {
                pandianResultDatas = PandianResultTable.getPandianTableInstance().selectAllDatas(PandianResultData.class);
            } else {
                String selection = PandianResultTable.getPandianTableInstance().getComlueInfos()[5].getName() + " = ?";
                String[] args = {taskId};
                pandianResultDatas = PandianResultTable.getPandianTableInstance().selectDatas(selection, args, null, null, null, PandianResultData.class);
            }
            ArrayList<DeviceData> pandianResults = new ArrayList<>();
            if (pandianResultDatas != null && pandianResultDatas.size() > 0) {
                for (PandianResultData item : pandianResultDatas) {
                    DeviceData deviceData = item.converToDeviceData();
                    pandianResults.add(deviceData);
                }
            }
            dBdata.setDevices(pandianResults);
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

    /**
     * 从PC端获取数据
     *
     * @param type 获取任务的类型；空表示获取所有任务
     */
    public void getDataFromPC(final Activity activity, String type) {
        final PopupWindowUtils netLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, activity, activity.getWindow().getDecorView(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((TextView) netLoadingWindow.getViewById(R.id.net_loading_tip)).setText("正在获取数据中......");
        netLoadingWindow.showAtLocation();
        String userName = Const.getUserName(this);
        String param = TextUtils.isEmpty(type) ? "{\"userName\":\"" + userName + "\"}" : "{\"userName\":\"" + userName + "\",\"taskType\":\"" + type + "\"}";
        Request.addRequestForCode(AdbSocketUtils.GET_ALL_INFOS_COMMANDE, param, new Request.CallBack() {
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
                            return false;
                        }
                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        netLoadingWindow.getPopupWindow().dismiss();
                        if (result) {
                            invokeDataDownFinishedListener();
                        } else {
                            Toast.makeText(activity, "解析数据失败……", Toast.LENGTH_LONG).show();
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

    public static void invokeDataDownFinishedListener() {
        if (mOnDataDownFinishedListeners.size() > 0) {
            for (OnDataDownFinishedListener item : mOnDataDownFinishedListeners)
                item.onDownorUploadFinish();
        }
    }

    /**
     * 解析从二维码扫描页面返回的数据
     *
     * @param result    二维码扫描结果
     * @param asyncTask 此异步任务的执行参数为二维码扫描对应的值
     */
    public <U, R> void resolveScannerResult(String result, AsyncTask<String, U, R> asyncTask) {

        if (!TextUtils.isEmpty(result)) {
//            final String[] resultArray = result.split("&");
//            String md5Result = GenerateQRCode.getMD5(resultArray[1]);
            if (asyncTask != null) {
                asyncTask.execute(result);
            }
        } else {
            Toast.makeText(this, "二维码格式有误!", Toast.LENGTH_LONG).show();
        }
    }

    public void uploadDataToPc(final Activity activity) {
        if (Const.isConnetionToPc()) {
            final PublicDialog dialog = new PublicDialog(activity);
            dialog.setContent("即将上传本地数据库!").showCancelButton(View.GONE, null).showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    final PopupWindowUtils netLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, activity, activity.getWindow().getDecorView(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    ((TextView) netLoadingWindow.getViewById(R.id.net_loading_tip)).setText("正在上传数据库......");
                    netLoadingWindow.showAtLocation();
                    final String datas = sendDataToPc(null);
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
                                invokeDataDownFinishedListener();
                                Toast.makeText(activity, "成功上传数据库!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFail(String erroInfo) {
                                netLoadingWindow.getPopupWindow().dismiss();
                                Toast.makeText(activity, "上传数据库失败!原因【" + erroInfo + "】" + erroInfo, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).show();
        } else {
            Toast.makeText(activity, "上传数据之前，请用USB连接线与PC端相连接……", Toast.LENGTH_LONG).show();
        }
    }

    public void loginOut(final Activity activity) {
        final PublicDialog publicDialog = new PublicDialog(activity);
        publicDialog.setContent("是否退出App？").showCancelButton().showSureButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Const.setUserName(activity, "");//清空用户
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                activity.finish();
                publicDialog.dismiss();
            }
        }).show();
    }

    /**
     * 数据下载完成后回调接口
     */
    public interface OnDataDownFinishedListener {
        void onDownorUploadFinish();
    }
}
