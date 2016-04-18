package com.zhanghang.idcdevice.fragment;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.adapter.DeviceAdapter;
import com.zhanghang.idcdevice.mode.DeviceData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-04-11.
 */
public class DeviceListFragment extends BaseListFragment<DeviceData> {
    @Override
    void loadData() {
        mDatas = new ArrayList<>();
        for(int i=0;i<9;i++) {
            DeviceData deviceData = new DeviceData();
            deviceData.setDeviceId(i+1);
            deviceData.setDeviceName("IBM网络服务器【" + i + "】");
            deviceData.setDeviceNum("000000000000000" + (i + 1));
            deviceData.setAssetNum((785469 + i) + "");
            deviceData.setAssetSerialNum((785469 + i) + "");
            deviceData.setEntityAssetNum("IBM-PSO123456-" + i + "");
            deviceData.setAssetType1("电脑");
            deviceData.setAssetType2("网络电脑");
            deviceData.setAssetType3("网络服务器电脑");
            deviceData.setDeviceModel("网络服务器电脑");
            deviceData.setCity("北京");
            deviceData.setIdcRoom("服务器机房1");
            deviceData.setCabinet("第" + (1 + i / 3) + "机柜");
            deviceData.setPosition("第" + (1 + i % 3) + "位置");
            mDatas.add(deviceData);
        }
        if(mListAdapter ==null){
            mListAdapter = new DeviceAdapter(mActivity,mDatas);
            mListView.setAdapter(mListAdapter);
        }else{
            mListAdapter.setDatas(mDatas);
        }
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
            mDatas.set(0,data);
            List<Fragment> fragmentList = getFragmentManager().getFragments();
            for(Fragment item:fragmentList){
                if(item instanceof MainFragment){
                    ((MainFragment)item).clickAdd();
                    break;
                }
            }
        }else {
            for (int i = 0; i < size; i++) {
                if (mDatas.get(i).getDeviceId() == data.getDeviceId()) {
                    if (type==Const.DataOperation.DELETE) mDatas.remove(i);
                    else mDatas.set(i, data);
                    break;
                }
            }
        }
        mListAdapter.notifyDataSetChanged();
    }
}
