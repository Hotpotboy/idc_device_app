package com.zhanghang.idcdevice.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
//        for(int i=0;i<9;i++) {
//            DeviceData deviceData = new DeviceData();
//            deviceData.setDeviceId(i+1);
//            deviceData.setDeviceName("IBM网络服务器【" + i + "】");
//            deviceData.setDeviceNum("000000000000000" + (i + 1));
//            deviceData.setAssetNum((785469 + i) + "");
//            deviceData.setAssetSerialNum((785469 + i) + "");
//            deviceData.setEntityAssetNum("IBM-PSO123456-" + i + "");
//            deviceData.setAssetType1("电脑");
//            deviceData.setAssetType2("网络电脑");
//            deviceData.setAssetType3("网络服务器电脑");
//            deviceData.setDeviceModel("网络服务器电脑");
//            deviceData.setCity("北京");
//            deviceData.setIdcRoom("服务器机房1");
//            deviceData.setCabinet("第" + (1 + i / 3) + "机柜");
//            deviceData.setPosition("第" + (1 + i % 3) + "位置");
//            mDatas.add(deviceData);
//        }
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

    private DeviceFragment getDeviceFragment(){
        List<Fragment> fragments = getFragmentManager().getFragments();
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
