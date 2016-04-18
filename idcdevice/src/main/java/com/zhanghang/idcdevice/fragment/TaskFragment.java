package com.zhanghang.idcdevice.fragment;

import com.zhanghang.idcdevice.adapter.TaskAdapter;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-29.
 */
public class TaskFragment extends BaseListFragment<TaskData> {
    @Override
    void loadData(){
        mDatas = new ArrayList<>();
        for(int i=0;i<10;i++) {
            TaskData taskData = new TaskData();
            taskData.setTaskName("日常巡检任务"+i);
            taskData.setTaskId(i+1);
            taskData.setDetails("详情详情详情详情详情详情……");
            ArrayList<PatrolItemData> patrolItemDatas = new ArrayList<>();
            for(int j=0;j<10;j++) {
                PatrolItemData patrolItemData = new PatrolItemData();
                patrolItemData.setEnable(true);
                patrolItemData.setPatrolId(j);
                patrolItemData.setPatrolItemName("检测是否能联网？"+(j+1));
                patrolItemData.setPatrolDetail("通过访问浏览器，以及检测网线是否正常等手段来确定是否能上网!");
                patrolItemDatas.add(patrolItemData);
            }
            taskData.setPatrolItems(patrolItemDatas);
            mDatas.add(taskData);
        }
        if(mListAdapter ==null){
            mListAdapter = new TaskAdapter(mActivity,mDatas);
            mListView.setAdapter(mListAdapter);
        }else{
            mListAdapter.setDatas(mDatas);
        }
    }

    public void updateList(TaskData data){
        if(data!=null){
            for(int i=0;i<mDatas.size();i++){
                if(mDatas.get(i).getTaskId() == data.getTaskId()){
                    mDatas.set(i,data);
                    mListAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }
}
