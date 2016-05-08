package com.zhanghang.idcdevice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.Intents;
import com.zhanghang.idcdevice.db.TaskTable;
import com.zhanghang.idcdevice.fragment.PatrolItemsFragment;
import com.zhanghang.idcdevice.fragment.TaskFragment;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.utils.camera.CameraUtils;
import com.zxing.util.GenerateQRCode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**
     * 抽屉视图
     */
    private DrawerLayout mDrawerLayout;
    /**
     * 从扫描二维码页面返回回来时，缓存的任务数据
     */
//    private TaskData mCacheTaskData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
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

//    public void gotoScannQRCode(TaskData data) {
//        CameraUtils.scannerQRCode(this);
//        mCacheTaskData = data;
//    }

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
                    if (!TextUtils.isEmpty(result) && result.indexOf("&") >= 0) {
                        String[] resultArray = result.split("&");
                        String md5Result = GenerateQRCode.getMD5(resultArray[1]);
                        if (resultArray[0].equals(md5Result)) {//签名正确
                            //根据计划开始时间、计划结束时间、设备类型查询符合条件的任务，如果没有，则查询只符合设备类型的任务；
                            //所有的查询结果以计划开始时间排序
                            String selections = TaskTable.getTaskTableInstance().getComlueInfos()[5].getName()+"=? AND "
                                               +TaskTable.getTaskTableInstance().getComlueInfos()[9].getName()+"<= ? AND "
                                               +TaskTable.getTaskTableInstance().getComlueInfos()[8].getName()+">= ?";
                            String[] args = new String[3];
                            long currentTime = System.currentTimeMillis();
                            args[0] = resultArray[1];
                            args[1] = currentTime + "";
                            args[2] = currentTime + "";
                            try {
                                ArrayList<TaskData> datas = TaskTable.getTaskTableInstance().selectDatas(selections, args, null, null, TaskTable.getTaskTableInstance().getComlueInfos()[9].getName(), TaskData.class);
                                if(datas!=null&&datas.size()>0){
                                    selecteFitData(datas);
                                }else{
                                    selections = TaskTable.getTaskTableInstance().getComlueInfos()[5].getName()+"=?";
                                    args = new String[1];
                                    args[0] = resultArray[1];
                                    datas = TaskTable.getTaskTableInstance().selectDatas(selections, args, null, null, TaskTable.getTaskTableInstance().getComlueInfos()[9].getName(), TaskData.class);
                                    if(datas!=null&&datas.size()>0) {
                                        selecteFitData(datas);
                                    }else{
                                        Toast.makeText(this,"此设备无相应的任务，请扫描下一个设备!",Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this,"此设备无相应的任务，请扫描下一个设备!",Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Toast.makeText(this,"二维码格式有误!",Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(this,"二维码格式有误!",Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    /**
     * 从数据库查询出来的结果集中，选择第一个非处理的任务进行处理
     * @param datas
     */
    private void selecteFitData(ArrayList<TaskData> datas) throws Exception {
        TaskData taskData = null;
        for(TaskData item:datas){
            if(!Const.isDealed(item)){
                taskData = item;
                break;
            }
        }
        if(taskData!=null) {
            taskData.setRealStartTime(System.currentTimeMillis());
            taskData.setDealPeople(Const.CURRENT_USER_NAME);
            ArrayList<PatrolItemData> items = Const.getPatrolItemDataByTaskId(taskData.getTaskId() + "");
            taskData.setPatrolItems(items);
            gotoTaskDetail(taskData);
        }else{
            Toast.makeText(this,"此设备无相应的任务，请扫描下一个设备!",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 前往任务详情页
     * @param data
     */
    private void gotoTaskDetail(TaskData data){
        //保存数据到数据库
        data.setRealStartTime(System.currentTimeMillis());
        data.setDealPeople(Const.CURRENT_USER_NAME);
        String selection = TaskTable.getTaskTableInstance().getComlueInfos()[14].getName()+"=?";
        String[] args = new String[1];
        args[0] = data.getTaskId()+"";
        try {
            TaskTable.getTaskTableInstance().updateData(data,selection,args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, FragmentActivity.class);
        intent.putExtra(Const.INTENT_KEY_LOAD_FRAGMENT, FragmentActivity.LOAD_PATROL_ITEM_FRAGMENT);
        intent.putExtra(Const.INTENT_KEY_TASK_DATA, data);
//        intent.putExtra(Const.INTENT_KEY_DEVICE_ID, resultArray[1]);
        startActivity(intent);
    }
}
