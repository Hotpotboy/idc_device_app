package com.zhanghang.idcdevice.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.MainActivity;
import com.zhanghang.idcdevice.PublicDialog;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.idcdevice.view.PatrolItemProgress;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.utils.SystemUtils;
import com.zhanghang.self.utils.camera.CameraUtils;

import java.util.Date;

/**
 * Created by Administrator on 2016-03-29.
 */
public class PatrolItemsFragment extends BaseFragment implements View.OnClickListener,ViewPager.OnPageChangeListener {
    /**设备ID*/
    private TextView mDeviceIdView;
    /**任务名称*/
    private TextView mTaskNameView;
    /**实施员工*/
    private TextView mDealPeopleView;
    /**开始时间*/
    private TextView mRealStartTimeView;
    /**进度条*/
    private PatrolItemProgress mPatrolItemProgress;
    /**标题左*/
    private TextView mTitileLeft;
    /**标题右*/
    private TextView mTitileRight;
    /**标题中间*/
    private TextView mTitileCenter;
    /**巡检项父视图*/
    private ViewPager mPatrolItemViewPager;
    /**任务数据*/
    private TaskData mData;
    /**设备ID*/
    private String mDeviceId;
    /**对话框*/
    private PublicDialog mDialog;
    /**巡检项适配器*/
    private PatrolItemAdpter mPatrolItemAdpter;
    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_patrol;
    }

    @Override
    protected void initDataFromArguments(Bundle arguments){
        mData = (TaskData) arguments.getSerializable(Const.INTENT_KEY_TASK_DATA);
        mDeviceId = arguments.getString(Const.INTENT_KEY_DEVICE_ID);
    }

    @Override
    protected void initView(){
        mDialog = new PublicDialog(mActivity);
        //标题
        mTitileLeft = (TextView) findViewById(R.id.fragment_title_left);
        mTitileCenter = (TextView) findViewById(R.id.fragment_title_center);
        mTitileRight = (TextView) findViewById(R.id.fragment_title_right);

        //任务信息
        mDeviceIdView = (TextView) findViewById(R.id.fragment_patrol_deviceID);
        mTaskNameView = (TextView) findViewById(R.id.fragment_patrol_taskName);
        mDealPeopleView = (TextView) findViewById(R.id.fragment_patrol_dealPeople);
        mRealStartTimeView = (TextView) findViewById(R.id.fragment_patrol_startTime);

        //进度条
        mPatrolItemProgress = (PatrolItemProgress) findViewById(R.id.fragment_patrol_dealProgress);

        //巡检信息
        mPatrolItemViewPager = (ViewPager) findViewById(R.id.fragment_patrol_viewPager);

        mTitileLeft.setOnClickListener(this);
        mTitileRight.setOnClickListener(this);

        mPatrolItemViewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void initData(){
        mTitileCenter.setText("巡检任务");
        mTitileLeft.setText("上一项");
        mTitileLeft.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTitileRight.setText("下一项");
        mTitileRight.setVisibility(View.VISIBLE);
        initData(mDeviceIdView, R.string.she_bei_bian_hao_s, mDeviceId);
        initData(mTaskNameView, R.string.ren_wu_ming_cheng_s, mData.getTaskName());
        initData(mDealPeopleView,R.string.shi_shi_yuan_gong_s,mData.getDealPeople());
        String startTime;
        if(mData.getRealStartTime()<=0){
            long currentTime = SystemClock.currentThreadTimeMillis();
            mData.setRealStartTime(currentTime);
            startTime = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss,new Date(currentTime));
        }else{
            long currentTime = mData.getRealStartTime();
            startTime = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss,new Date(currentTime));
        }
        initData(mRealStartTimeView, R.string.kai_shi_shi_jian_s, startTime);
        mPatrolItemAdpter = new PatrolItemAdpter();
        mPatrolItemViewPager.setAdapter(mPatrolItemAdpter);
        //巡检项总数
        mPatrolItemProgress.setTotalCount(mData.getPatrolItems().size());
    }

    private void initData(TextView view,int formatStrId,String value){
        String content = String.format(getResources().getString(formatStrId),value);
        SpannableString contentSpannable = Const.changeSubColor(content,value, getResources().getColor(R.color.idc_000000));
        view.setText(contentSpannable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fragment_title_left://上一项
                clickPreItem();
                break;
            case R.id.fragment_title_right://下一项
                clickNextItem();
                break;
        }
    }

    private void clickNextItem(){
        int total = mData.getPatrolItems().size();
        if(mPatrolItemProgress.getCurrentIndx()>=total-2){
            mDialog.dismiss();
            mDialog.setContent("已达到最后一项,\n是否保存此次巡检所有的结果?")
                    .showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savePatrols();
                    mDialog.dismiss();
                    Intent inten = new Intent(mActivity, MainActivity.class);
                    inten.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    inten.putExtra(Const.INTENT_KEY_TASK_DATA,mData);
                    startActivity(inten);
                }
            }).show();
        }else{
            goOrBackItem(true);//下一项
        }
    }

    private void clickPreItem(){
        if(mPatrolItemProgress.getCurrentIndx()<0){
            mDialog.dismiss();
            mDialog.setContent("已达到第一项,是否退出?")
                    .showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    mActivity.finish();
                }
            }).show();
        }else{
            goOrBackItem(false);//上一项
        }
    }

    /**
     * 下(上)一项
     * @param isGo   true为下一项，否则为上一项
     */
    private void goOrBackItem(boolean isGo){
        int currentIndex = mPatrolItemProgress.getCurrentIndx();
        int currentPager = mPatrolItemViewPager.getCurrentItem();
        if(!isGo){//上一项
            mPatrolItemProgress.setCurrentIndx(--currentIndex);
            if(currentPager!=(currentIndex+1)) mPatrolItemViewPager.setCurrentItem(--currentPager);
        }else{//下一项
            mPatrolItemProgress.setCurrentIndx(++currentIndex);
            if(currentPager!=(currentIndex+1)) mPatrolItemViewPager.setCurrentItem(++currentPager);
        }
    }

    private void savePatrols(){
        SparseArray<LinearLayout> allItemView = mPatrolItemAdpter.mViews;//所有巡检项对应的item
        if(allItemView!=null&&allItemView.size()>0){
            int size = allItemView.size();
            for(int i=0;i<size;i++){
                PatrolItemData patrolItemData = mData.getPatrolItems().get(i);
                TextView patrolItemNormalView = (TextView) mPatrolItemAdpter.mViews.get(i).findViewById(R.id.public_patrol_nomal);//是否正常按钮
                EditText editText = (EditText) mPatrolItemAdpter.mViews.get(i).findViewById(R.id.public_patrol_record);//记录编辑框
                patrolItemData.setIsNormal(patrolItemNormalView.isSelected());
                if(editText!=null&&!TextUtils.isEmpty(editText.getText())){
                    patrolItemData.setRecordValue(editText.getText().toString());
                }
            }
        }
        mData.setDealResult("已处理");
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        int current = mPatrolItemProgress.getCurrentIndx();
        if(position>(current+1)){
            clickNextItem();
        }else if(position<(current+1)){
            clickPreItem();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private class PatrolItemAdpter extends PagerAdapter implements View.OnClickListener{
        private SparseArray<LinearLayout> mViews;
        public PatrolItemAdpter(){
            mViews = new SparseArray<>();
        }

        @Override
        public int getCount() {
            return mData.getPatrolItems().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object&&view!=null&&object!=null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if(mViews.get(position)==null) {
                LinearLayout view = (LinearLayout) LayoutInflater.from(mActivity).inflate(R.layout.public_patrol_item, null);
                PatrolItemData data = mData.getPatrolItems().get(position);
                //初始化视图
                initView(data, view);
                //填充数据
                fillData(data, view);
                mViews.put(position, view);
            }
            container.addView(mViews.get(position));
            return mViews.get(position);
        }

        private void initView(final PatrolItemData data,View view){
            boolean isNormal = data.isNormal();
            TextView patrolItemNormalView = (TextView) view.findViewById(R.id.public_patrol_nomal);
            TextView patrolItemUnNormalView = (TextView) view.findViewById(R.id.public_patrol_unnomal);
            updateNormalView(isNormal,patrolItemNormalView,patrolItemUnNormalView);
            patrolItemNormalView.setOnClickListener(this);
            patrolItemUnNormalView.setOnClickListener(this);
        }

        /**
         * 更新是否正常的两个按钮显示界面（背景、字体颜色等）
         * @param isNormal                是否正常
         * @param patrolItemNormalView    正常按钮
         * @param patrolItemUnNormalView  非正常按钮
         */
        private void updateNormalView(boolean isNormal,TextView patrolItemNormalView,TextView patrolItemUnNormalView){
            if(isNormal){
                patrolItemNormalView.setSelected(true);
                patrolItemNormalView.setTextColor(getResources().getColor(R.color.idc_f5f5f5));
                patrolItemUnNormalView.setSelected(false);
                patrolItemUnNormalView.setTextColor(getResources().getColor(R.color.idc_af0012));
            }else {
                patrolItemNormalView.setSelected(false);
                patrolItemNormalView.setTextColor(getResources().getColor(R.color.idc_af0012));
                patrolItemUnNormalView.setSelected(true);
                patrolItemUnNormalView.setTextColor(getResources().getColor(R.color.idc_f5f5f5));
            }
        }

        private void fillData(PatrolItemData data,View view){
            TextView patrolItemNameView = (TextView) view.findViewById(R.id.public_patrol_patrolItemName);
            TextView patrolItemStudioView = (TextView) view.findViewById(R.id.public_patrol_patrolStudio);
            TextView patrolItemStepView = (TextView) view.findViewById(R.id.public_patrol_patrolStep);
            TextView patrolItemDetailView = (TextView) view.findViewById(R.id.public_patrol_patrolDetail);
            initData(patrolItemNameView, R.string.xun_jian_ming_cheng_s, data.getPatrolItemName());
            initData(patrolItemStudioView, R.string.xun_jian_zhi_biao_s, data.getPatrolStuido());
            initData(patrolItemStepView, R.string.xun_jian_cuo_shi_s, data.getPatrolStep());
            initData(patrolItemDetailView, R.string.xun_jian_xiang_qing_s, data.getPatrolDetail());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViews.get(position));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.public_patrol_unnomal:
                case R.id.public_patrol_nomal:
                    if(!v.isSelected()){
                        int position = mPatrolItemViewPager.getCurrentItem();
                        TextView patrolItemNormalView = (TextView) mViews.get(position).findViewById(R.id.public_patrol_nomal);
                        TextView patrolItemUnNormalView = (TextView) mViews.get(position).findViewById(R.id.public_patrol_unnomal);
                        updateNormalView(!patrolItemNormalView.isSelected(),patrolItemNormalView,patrolItemUnNormalView);
                    }
                    break;
            }
        }
    }
}
