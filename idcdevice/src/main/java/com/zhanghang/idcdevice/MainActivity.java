package com.zhanghang.idcdevice;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Intents;
import com.zhanghang.idcdevice.db.TaskTable;
import com.zhanghang.idcdevice.fragment.TaskFragment;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.utils.PopupWindowUtils;
import com.zhanghang.self.utils.camera.CameraUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**
     * 抽屉视图
     */
    private DrawerLayout mDrawerLayout;
    private PopupWindowUtils mNetLoadingWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
        mNetLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, this, getWindow().getDecorView(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public void setDrawerListener(final DrawerLayout.DrawerListener listener) {
        if (mDrawerLayout != null) {
            if (listener != null) {
                mDrawerLayout.setDrawerListener(listener);
            }
        } else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    setDrawerListener(listener);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment item : fragments) {
            if (item instanceof TaskFragment) {
                ((TaskFragment) item).loadData();
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CameraUtils.SCANNER_QR_CODE_REQUEST_CODE://二维码扫描页面
                    String result = data.getStringExtra(Intents.Scan.RESULT);
                    mNetLoadingWindow.showAtLocation();
                    ((TextView) mNetLoadingWindow.getViewById(R.id.net_loading_tip)).setText("解析二维码成功，正在查询符合条件的任务......");
                    ((DeviceApplication)DeviceApplication.getInstance()).resolveScannerResult(result, new XunJianTask());//解析结果
                    break;
            }
        }
    }

    /***
     * 用以解析巡检任务的二维码任务
     */
    private class XunJianTask extends AsyncTask<String, Void, ArrayList<TaskData>>{

        @Override
        protected ArrayList<TaskData> doInBackground(String... params) {
            //根据计划开始时间、计划结束时间、设备类型查询符合条件的任务，如果没有，则查询只符合设备类型的任务；
            //所有的查询结果以计划开始时间排序
            String selections = TaskTable.getTaskTableInstance().getComlueInfos()[0].getName() + "=? AND "
                    + TaskTable.getTaskTableInstance().getComlueInfos()[9].getName() + "<= ? AND "
                    + TaskTable.getTaskTableInstance().getComlueInfos()[8].getName() + ">= ?";
            String[] args = new String[3];
            long currentTime = System.currentTimeMillis();
            args[0] = params[1];
            args[1] = currentTime + "";
            args[2] = currentTime + "";
            try {
                ArrayList<TaskData> datas = TaskTable.getTaskTableInstance().selectDatas(selections, args, null, null, TaskTable.getTaskTableInstance().getComlueInfos()[9].getName(), TaskData.class);
                if (datas != null && datas.size() > 0) {
                    return datas;
                } else {
                    selections = TaskTable.getTaskTableInstance().getComlueInfos()[0].getName() + "=?";
                    args = new String[1];
                    args[0] = params[1];
                    datas = TaskTable.getTaskTableInstance().selectDatas(selections, args, null, null, TaskTable.getTaskTableInstance().getComlueInfos()[9].getName(), TaskData.class);
                    if (datas != null && datas.size() > 0) {
                        return datas;
                    } else {
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void onPostExecute(ArrayList<TaskData> datas) {
            mNetLoadingWindow.getPopupWindow().dismiss();
            try {
                if (datas == null || datas.size() <= 0) {
                    Toast.makeText(MainActivity.this, "此设备无相应的任务，请扫描下一个设备!", Toast.LENGTH_LONG).show();
                    return;
                }
                TaskData taskData = null;
                for (TaskData item : datas) {
                    if (!Const.isDealed(item)) {
                        taskData = item;
                        break;
                    }
                }
                if (taskData != null) {
                    taskData.setRealStartTime(System.currentTimeMillis());
                    taskData.setDealPeople(Const.getUserName(MainActivity.this));
                    ArrayList<PatrolItemData> items = Const.getPatrolItemDataByTaskId(taskData.getTaskId() + "");
                    taskData.setPatrolItems(items);
                    //前往任务详情页
                    //保存数据到数据库
                    taskData.setRealStartTime(System.currentTimeMillis());
                    taskData.setDealPeople(Const.getUserName(MainActivity.this));
                    String selection = TaskTable.getTaskTableInstance().getComlueInfos()[14].getName() + "=?";
                    String[] args = new String[1];
                    args[0] = taskData.getTaskId() + "";
                    try {
                        TaskTable.getTaskTableInstance().updateData(taskData, selection, args);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(MainActivity.this, FragmentActivity.class);
                    intent.putExtra(Const.INTENT_KEY_LOAD_FRAGMENT, FragmentActivity.LOAD_PATROL_ITEM_FRAGMENT);
                    intent.putExtra(Const.INTENT_KEY_TASK_DATA, taskData);
//        intent.putExtra(Const.INTENT_KEY_DEVICE_ID, resultArray[1]);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "此设备无相应的任务，请扫描下一个设备!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "此设备无相应的任务，请扫描下一个设备!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
