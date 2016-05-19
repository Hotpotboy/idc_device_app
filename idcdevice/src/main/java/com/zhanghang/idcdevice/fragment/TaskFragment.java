package com.zhanghang.idcdevice.fragment;

import android.os.Bundle;
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
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle onSavedInstance){
        ((DeviceApplication)DeviceApplication.getInstance()).addDataDownFinishedListener(this);
        return super.onCreateView(inflater, container, onSavedInstance);
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
            mDatas = TaskTable.getTaskTableInstance().selectAllDatas(TaskData.class);
            if(mDatas!=null&&mDatas.size()>0){
                showList(true);
                for(int i = 0;i<mDatas.size();i++){
                    TaskData data = mDatas.get(i);
                    ArrayList<PatrolItemData> items = Const.getPatrolItemDataByTaskId(data.getTaskId()+"");
                    data.setPatrolItems(items);
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
    public void onDown() {
        loadData();
    }
}
