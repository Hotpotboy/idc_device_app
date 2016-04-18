package com.zhanghang.idcdevice.fragment;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.mode.DeviceData;
import com.zhanghang.self.base.BaseFragment;

public class DeviceFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    /**左边布局的最大权重*/
    private final int MAX_WEIGHT = 5;
    /**左边布局*/
    private RelativeLayout mLeftLayout;
    /**详情页面的父视图*/
    private FrameLayout mDetailParentLaytou;
    /**
     * 设备列表页面
     */
    private DeviceListFragment mDeviceListFragment;
    /**
     * 设备详情页面
     */
    private DeviceDetailFragment mDeviceDetailFragment;
    /**标题栏右上角是否显示新增（当前是否未在新增）*/
    private boolean isShowAdd = true;
    /**展开/隐藏左边设备列表fragment的动画每一帧的回调*/
    private Runnable mShowOrHiddenLeft = new Runnable() {
        @Override
        public void run() {
            boolean isGoOn = showOrHiddenLeft(isShowAdd);
            if(isGoOn){//如果继续
                mLeftLayout.postDelayed(this, 20);
            }else{//结束动画
                if(!isShowAdd){//新增
                    mDeviceDetailFragment.addData();
                }else{//取消新增
                    updateFragments(0);
                }
            }
        }
    };

    @Override
    protected void initView() {
        mLeftLayout = (RelativeLayout) findViewById(R.id.fragment_devices_left);
        mDetailParentLaytou = (FrameLayout) findViewById(R.id.fragment_devices_detail);
        mDeviceListFragment = new DeviceListFragment();
        mDeviceDetailFragment  = new DeviceDetailFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_devices_list,mDeviceListFragment);
        fragmentTransaction.replace(R.id.fragment_devices_detail,mDeviceDetailFragment);
        fragmentTransaction.commit();
        updateFragments(0);
        mDeviceListFragment.setOnItemClickListener(this);
    }

    private void updateFragments(final int pos) {
        DeviceData data = mDeviceListFragment.getCurrentData(pos);
        if(data!=null) {
            mDeviceDetailFragment.setData(data);
        }else{
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    updateFragments(pos);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == mActivity.RESULT_OK) {
            switch (requestCode) {

            }
        }
    }

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_devices;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        updateFragments(position);
    }

    /**
     * 处理新增设备
     */
    public void dealAddDevice(boolean isCancel){
        if(isShowAdd !=isCancel) {
            mLeftLayout.removeCallbacks(mShowOrHiddenLeft);//结束当前动画
            isShowAdd = isCancel;
            mLeftLayout.post(mShowOrHiddenLeft);//重新开始动画
        }
    }

    public boolean showOrHiddenLeft(boolean isShow){
        float weight;//当前权重
        ViewGroup.LayoutParams params = mLeftLayout.getLayoutParams();
        if(params!=null&&params instanceof LinearLayout.LayoutParams) {
            weight = ((LinearLayout.LayoutParams)params).weight;
            if (isShow&&weight<MAX_WEIGHT){//显示
                weight+=0.5;
            }else if(weight>0&&weight<=MAX_WEIGHT){//隐藏
                weight-=0.5;
            }
            ((LinearLayout.LayoutParams) params).weight = weight;
            mLeftLayout.setLayoutParams(params);
            mLeftLayout.requestLayout();
            if(weight>0&&weight<MAX_WEIGHT) {
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
}
