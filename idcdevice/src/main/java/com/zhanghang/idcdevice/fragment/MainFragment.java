package com.zhanghang.idcdevice.fragment;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.MainActivity;
import com.zhanghang.idcdevice.R;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.fragment.ViewPagerFragement;
import com.zhanghang.self.utils.camera.CameraUtils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-29.
 */
public class MainFragment extends ViewPagerFragement implements View.OnClickListener {
    /**下方操作按钮的个数*/
    private static final int BUTTON_NUM = 3;
    /**左上角菜单按钮*/
    private TextView menuView;
    /**设备(机房列表)Fragment*/
//    private DeviceListFragment mDeviceFragment;
    /**下方三个按钮*/
    private ButtonHold[] mButtonHolds = new ButtonHold[BUTTON_NUM];
    private TextView mTitileCenter;
    private TextView mTitileRight;

    @Override
    protected ArrayList<BaseFragment> specifyFragmentList() {
        ArrayList<BaseFragment> fragments = new ArrayList<>();
        TaskFragment patrolTaskFragment = getTaskFragment(Const.TASK_TYPE_XUNJIAN);
        fragments.add(patrolTaskFragment);

        TaskFragment pandianTaskFragment = getTaskFragment(Const.TASK_TYPE_PANDIAN);
        fragments.add(pandianTaskFragment);
        return fragments;
    }

    private TaskFragment getTaskFragment(String type){
        TaskFragment taskFragment = new TaskFragment();
        Bundle patrolBundle = new Bundle();
        patrolBundle.putString(Const.INTENT_KEY_TASK_TYPE, type);
        taskFragment.setArguments(patrolBundle);
        return taskFragment;
    }

    @Override
    protected ViewPager specifyViewPager() {
        return (ViewPager) mRootView.findViewById(R.id.fragment_main_viewPager);
    }

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initView(){
        super.initView();
        //标题
        mTitileCenter = (TextView) findViewById(R.id.fragment_title_center);
        mTitileRight = (TextView) findViewById(R.id.fragment_title_right);
        menuView = (TextView) findViewById(R.id.fragment_title_left);
        menuView.setOnClickListener(this);
        mTitileRight.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData(); //初始化下方三个按钮
        mButtonHolds[0] = new ButtonHold(0,R.id.fragment_main_task_button_line,R.id.fragment_main_task_button_image,R.id.fragment_main_task_button_value);
        //设备按钮
        mButtonHolds[1] = new ButtonHold(1,R.id.fragment_main_device_count_button_line,R.id.fragment_main_device_count_button_image,R.id.fragment_main_device_count_button_value);
        //"我的"按钮
        mButtonHolds[2] = new ButtonHold(2,R.id.fragment_main_my_button_line,R.id.fragment_main_my_button_image,R.id.fragment_main_my_button_value);
        //默认第一个被选中
        mButtonHolds[getCurrentItem()].setSelected(true);

        ((MainActivity)mActivity).setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                for (ButtonHold item : mButtonHolds) {
                    if (item.index == getCurrentItem()) {
                        item.setSelected(true);
                    } else {
                        item.setSelected(false);
                    }
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        onPageSelected(0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setCurrentFragment(int index){
        int currentItem = getCurrentItem();
        if(mButtonHolds!=null&&currentItem>=0&&currentItem<mButtonHolds.length) {
            if(mButtonHolds[currentItem]!=null) {
                mButtonHolds[currentItem].setSelected(false);
            }
        }
        super.setCurrentFragment(index);
    }

    @Override
    public void onPageSelected(int position) {
        mButtonHolds[getCurrentItem()].setSelected(false);
        super.onPageSelected(position);
        mButtonHolds[getCurrentItem()].setSelected(true);
        if(position==0){
            mTitileCenter.setText("机房巡检");
            mTitileRight.setVisibility(View.VISIBLE);
            if(mTitileRight.getBackground()==null||!(mTitileRight.getBackground() instanceof BitmapDrawable)){
                mTitileRight.setBackgroundResource(R.drawable.scanner);
            }
        }else if(position==1){
            mTitileCenter.setText("资产盘点");
//            mTitileRight.setText("新增");
            mTitileRight.setVisibility(View.GONE);
        }
    }

    private void click(int position){
        if(position==getCurrentItem()) return;
        if(position!=2) {
            setCurrentFragment(position);
        }else{
            mButtonHolds[getCurrentItem()].setSelected(false);
            ((MainActivity)mActivity).openDrawer();
            mButtonHolds[position].setSelected(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fragment_title_left:
                ((MainActivity)mActivity).openDrawer();
                break;
            case R.id.fragment_title_right:
                if(getCurrentItem()==1){//新增设备
                    clickAdd();
                }else if(getCurrentItem()==0){//扫描二维码
                    if (((DeviceApplication) DeviceApplication.getInstance()).isUploadData()) {
                        Toast.makeText(mActivity, "数据已上传,无法扫描!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    CameraUtils.scannerQRCode(mActivity);
                }
                break;
        }
    }

    public void clickAdd(){
        String content = (String) mTitileRight.getText();
        if(TextUtils.equals(content,"新增")){
            mTitileRight.setText("取消");
        }else{
            mTitileRight.setText("新增");
        }
    }

    private class ButtonHold {
        /**按钮线*/
        private View line;
        /**按钮图片*/
        private ImageView imageView;
        /**按钮文字*/
        private TextView textView;
        private int index;

        public ButtonHold(int _index,int lineViewId,int imageViewId,int textViewId){
            index = _index;
            line = findViewById(lineViewId);
            imageView = (ImageView) findViewById(imageViewId);
            textView = (TextView) findViewById(textViewId);
            setSelected(false);
            ((View)line.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click(index);
                }
            });
        }

        public void setSelected(boolean isSelected){
            int color = Color.TRANSPARENT;
            int textColor = Color.BLACK;
            if(isSelected){
                color = getResources().getColor(R.color.idc_af0012);
                textColor = color;
            }
            line.setBackgroundColor(color);
            imageView.setSelected(isSelected);
            textView.setTextColor(textColor);
        }
    }
}
