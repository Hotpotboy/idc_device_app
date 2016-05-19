package com.zhanghang.idcdevice.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.adapter.DeviceAdapter;
import com.zhanghang.idcdevice.db.DeviceTable;
import com.zhanghang.idcdevice.mode.DeviceData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-04-11.
 */
public class DeviceListFragment extends BaseListFragment<DeviceData> implements DeviceApplication.OnDataDownFinishedListener {
    private FragmentManager mFragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle onSavedInstance){
        ((DeviceApplication)DeviceApplication.getInstance()).addDataDownFinishedListener(this);
        return super.onCreateView(inflater, container, onSavedInstance);
    }
    @Override
    void loadData() {
        mDatas = new ArrayList<>();
        try {
            mDatas = DeviceTable.getDeviceTableInstance().selectDatas("", null, "", "", "", DeviceData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DeviceFragment deviceFragment = getDeviceFragment();
        if(mDatas!=null&&mDatas.size()>0) {
            deviceFragment.showList(true);
            if (mListAdapter == null) {
                mListAdapter = new DeviceAdapter(mActivity, mDatas);
                mListView.setAdapter(mListAdapter);
            } else {
                mListAdapter.setDatas(mDatas);
            }
        }else{
            deviceFragment.showList(false);
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        ((DeviceApplication)DeviceApplication.getInstance()).removeDataDownFinishedListener(this);
    }

    private DeviceFragment getDeviceFragment(){
//        if(mFragmentManager==null){
            mFragmentManager = getFragmentManager();
//        }
        List<Fragment> fragments = mFragmentManager.getFragments();
        for(Fragment item:fragments){
            if(item instanceof DeviceFragment){
                return (DeviceFragment) item;
            }
        }
        return null;
    }

    /**
     * 获取指定位置的数据
     * @return
     */
    public DeviceData getCurrentData(int position){
        if(mDatas==null||mDatas.size()<=0){
            return null;
        }else if(position<=0){
            position = 0;
        }else if(position>=mDatas.size()){
            position = mDatas.size()-1;
        }
        return mDatas.get(position);
    }

    public void setOnItemClickListener(final AdapterView.OnItemClickListener listener){
        if(listener!=null){
            if(mListView!=null) {
                mListView.setOnItemClickListener(listener);
            }else {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        setOnItemClickListener(listener);
                    }
                });
            }
        }
    }

    /**
     * 更新数据
     * @param data
     * @param type  true表示删除数据，否则为更新数据
     */
    public void updateData(DeviceData data,Const.DataOperation type){
        int size = mDatas.size();
        if(type== Const.DataOperation.ADD){
            mDatas.add(0, data);
            List<Fragment> fragmentList = getFragmentManager().getFragments();
            for(Fragment item:fragmentList){
                if(item instanceof MainFragment){
                    ((MainFragment)item).clickAdd();
                    break;
                }
            }
            try {
                DeviceTable.getDeviceTableInstance().insertData(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            for (int i = 0; i < size; i++) {
                if (mDatas.get(i).getDeviceId() == data.getDeviceId()) {
                    try {
                        String[] args = new String[1];
                        args[0]=data.getDeviceId()+"";
                        if (type==Const.DataOperation.DELETE) {
                            mDatas.remove(i);
                            DeviceTable.getDeviceTableInstance().deleteData("deviceId=?",args);
                        } else {
                            mDatas.set(i, data);
                            DeviceTable.getDeviceTableInstance().updateData(data,"deviceId=?",args);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDown() {
        loadData();
    }
}
