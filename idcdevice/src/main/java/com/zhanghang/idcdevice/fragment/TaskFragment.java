package com.zhanghang.idcdevice.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.adapter.TaskAdapter;
import com.zhanghang.idcdevice.db.PatrolItemTable;
import com.zhanghang.idcdevice.db.TaskTable;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-29.
 */
public class TaskFragment extends BaseListFragment<TaskData> implements DeviceApplication.OnDataDownFinishedListener {
    /**
     * 任务类型，默认为盘点任务
     */
    private String mTaskType = Const.TASK_TYPE_XUNJIAN;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle onSavedInstance){
        ((DeviceApplication)DeviceApplication.getInstance()).addDataDownFinishedListener(this);
        return super.onCreateView(inflater, container, onSavedInstance);
    }

    @Override
    protected void initDataFromArguments(Bundle arguments){
        mTaskType = arguments.getString(Const.INTENT_KEY_TASK_TYPE,Const.TASK_TYPE_XUNJIAN);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        ((DeviceApplication)DeviceApplication.getInstance()).removeDataDownFinishedListener(this);
    }
    @Override
    public void loadData(){
        mDatas = new ArrayList<>();
        try {
            String selection = TaskTable.getTaskTableInstance().getComlueInfos()[19].getName()+" = ?";
            String[] args = new String[1];
            args[0] = mTaskType;
            mDatas = TaskTable.getTaskTableInstance().selectDatas(selection,args,null,null,null,TaskData.class);
            if(mDatas!=null&&mDatas.size()>0){
                showList(true);
                if(TextUtils.equals(mTaskType,Const.TASK_TYPE_XUNJIAN)) {//如果是巡检任务则需查询对应的巡检项集合
                    for (int i = 0; i < mDatas.size(); i++) {
                        TaskData data = mDatas.get(i);
                        ArrayList<PatrolItemData> items = Const.getPatrolItemDataByTaskId(data.getTaskId() + "");
                        data.setPatrolItems(items);
                    }
                }
                if(mListAdapter ==null){
                    mListAdapter = new TaskAdapter(mActivity,mDatas);
                    mListView.setAdapter(mListAdapter);
                }else{
                    mListAdapter.setDatas(mDatas);
                }
            }else {
                showList(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDownorUploadFinish() {
        loadData();
    }
}
