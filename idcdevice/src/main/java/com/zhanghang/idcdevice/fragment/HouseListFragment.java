package com.zhanghang.idcdevice.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.adapter.HouseAdapter;
import com.zhanghang.idcdevice.db.HouseTable;
import com.zhanghang.idcdevice.mode.room.HouseData;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-29.
 */
public class HouseListFragment extends BaseListFragment<HouseData> implements DeviceApplication.OnDataDownFinishedListener {
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
            mDatas = HouseTable.getTaskTableInstance().selectAllDatas(HouseData.class);
            if(mDatas!=null&&mDatas.size()>0){
                showList(true);
                if(mListAdapter ==null){
                    mListAdapter = new HouseAdapter(mActivity,mDatas);
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
    protected void showList(boolean isShowList){
        super.showList(isShowList);
        if(!isShowList){//如果没有数据，就表示此次盘点任务没有指定相关的机房
            mNoDataOperationButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDown() {
        loadData();
    }
}
