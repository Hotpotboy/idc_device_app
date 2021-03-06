package com.zhanghang.idcdevice.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
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
import com.zhanghang.idcdevice.db.PatrolItemTable;
import com.zhanghang.idcdevice.db.TaskTable;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.idcdevice.view.PatrolItemProgress;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.utils.SystemUtils;

import java.util.Date;

/**
 * Created by Administrator on 2016-03-29.
 */
public class PatrolItemsFragment extends BaseFragment implements View.OnClickListener, ViewPager.OnPageChangeListener {
    /**
     * 资产分类
     */
    private TextView mAssetTypeView;
    /**
     * 任务名称
     */
    private TextView mTaskNameView;
    /**
     * 实施员工
     */
    private TextView mDealPeopleView;
    /**
     * 计划开始时间
     */
    private TextView mPlanedStartTimeView;
    /**
     * 计划结束时间
     */
    private TextView mPlanedEndTimeView;
    /**
     * 任务详情
     */
    private TextView mTaskDetailView;
    /**
     * 进度条
     */
    private PatrolItemProgress mPatrolItemProgress;
    /**
     * 标题左
     */
    private TextView mTitileLeft;
    /**
     * 标题右
     */
    private TextView mTitileRight;
    /**
     * 标题中间
     */
    private TextView mTitileCenter;
    /**
     * 巡检项父视图
     */
    private ViewPager mPatrolItemViewPager;
    /**
     * 任务数据
     */
    private TaskData mData;
    /**设备ID*/
//    private String mDeviceId;
    /**
     * 对话框
     */
    private PublicDialog mDialog;
    /**
     * 巡检项适配器
     */
    private PatrolItemAdpter mPatrolItemAdpter;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_patrol;
    }

    @Override
    protected void initDataFromArguments(Bundle arguments) {
        mData = (TaskData) arguments.getSerializable(Const.INTENT_KEY_TASK_DATA);
//        mDeviceId = arguments.getString(Const.INTENT_KEY_DEVICE_ID);
    }

    @Override
    protected void initView() {
        mDialog = new PublicDialog(mActivity);
        //标题
        mTitileLeft = (TextView) findViewById(R.id.fragment_title_left);
        mTitileCenter = (TextView) findViewById(R.id.fragment_title_center);
        mTitileRight = (TextView) findViewById(R.id.fragment_title_right);

        //任务信息
        mAssetTypeView = (TextView) findViewById(R.id.fragment_patrol_assetType);
        mTaskNameView = (TextView) findViewById(R.id.fragment_patrol_taskName);
        mDealPeopleView = (TextView) findViewById(R.id.fragment_patrol_dealPeople);
        mPlanedStartTimeView = (TextView) findViewById(R.id.fragment_patrol_startTime);
        mPlanedEndTimeView = (TextView) findViewById(R.id.fragment_patrol_endTime);
        mTaskDetailView = (TextView) findViewById(R.id.fragment_patrol_taskDetail);

        //进度条
        mPatrolItemProgress = (PatrolItemProgress) findViewById(R.id.fragment_patrol_dealProgress);

        //巡检信息
        mPatrolItemViewPager = (ViewPager) findViewById(R.id.fragment_patrol_viewPager);

        mTitileLeft.setOnClickListener(this);
        mTitileRight.setOnClickListener(this);

        mPatrolItemViewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void initData() {
        mTitileCenter.setText("巡检任务");
        mTitileLeft.setText("上一项");
        mTitileLeft.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTitileRight.setText("下一项");
        mTitileRight.setVisibility(View.VISIBLE);
        initData(mAssetTypeView, R.string.zi_chan_fen_lei_s, Const.isNullForDBData(mData.getAssetType())?getString(R.string.kong_shu_ju):mData.getAssetType(), R.color.idc_000000);
        initData(mTaskNameView, R.string.ren_wu_ming_cheng_s, mData.getTaskName(), R.color.idc_000000);
        initData(mDealPeopleView, R.string.shi_shi_ren_s, Const.isNullForDBData(mData.getDealPeople())?getString(R.string.kong_shu_ju):mData.getDealPeople(), R.color.idc_000000);
        if (mData.getRealStartTime() <= 0) {
            long currentTime = SystemClock.currentThreadTimeMillis();
            mData.setRealStartTime(currentTime);
        }
        long planedStartTime = mData.getPlanedStartTime();
        String planedStartTimeStr;
        if (planedStartTime <= 0) {
            planedStartTimeStr = getResources().getString(R.string.kong_shu_ju);
        } else {
            planedStartTimeStr = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss, new Date(planedStartTime));
        }
        initData(mPlanedStartTimeView, R.string.ji_hua_kai_shi_shi_jian_s, planedStartTimeStr, R.color.idc_000000);
        long planedEndTime = mData.getPlanedEndTime();
        String planedEndTimeStr;
        if (planedEndTime <= 0) {
            planedEndTimeStr = getResources().getString(R.string.kong_shu_ju);
        } else {
            planedEndTimeStr = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss, new Date(planedEndTime));
        }
        initData(mPlanedEndTimeView, R.string.ji_hua_jie_su_shi_jian_s, planedEndTimeStr, R.color.idc_000000);
        String detail = mData.getDetails();
        if(Const.isNullForDBData(detail)){
            detail = getString(R.string.kong_shu_ju);
        }
        initData(mTaskDetailView, R.string.xiang_xi_miao_shu_s, detail, R.color.idc_000000);
        mPatrolItemAdpter = new PatrolItemAdpter();
        mPatrolItemViewPager.setAdapter(mPatrolItemAdpter);
        //巡检项总数
        mPatrolItemProgress.setTotalCount(mData.getPatrolItems().size());
    }

    private void initData(TextView view, int formatStrId, String value, int colorId) {
        String content = String.format(getResources().getString(formatStrId), value);
        SpannableString contentSpannable = Const.changeSubColor(content, value, getResources().getColor(colorId));
        view.setText(contentSpannable);
    }

    private void initClickableData(TextView view, int formatStrId, String value, int colorId) {
        String content = String.format(getResources().getString(formatStrId), value);
        SpannableString contentSpannable = Const.changeClickColor(content, value, getResources().getColor(colorId), new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                String content = mData.getDetails();
                mDialog.setContent(Const.isNullForDBData(content) ? mActivity.getString(R.string.kong_shu_ju) : content).showCancelButton(View.GONE, null).showSureButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                }).show();
            }
        });
        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(contentSpannable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_title_left://上一项
                clickPreItem();
                break;
            case R.id.fragment_title_right://下一项
                clickNextItem();
                break;
        }
    }

    private void clickNextItem() {
        int total = mData.getPatrolItems().size();
        if (mPatrolItemProgress.getCurrentIndx() >= total - 2) {
            mDialog.dismiss();
            mDialog.setContent("已达到最后一项,\n是否保存此次巡检所有的结果?")
                    .showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savePatrols();
                    mDialog.dismiss();
                    Intent inten = new Intent(mActivity, MainActivity.class);
                    inten.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    inten.putExtra(Const.INTENT_KEY_TASK_DATA,mData);
                    startActivity(inten);
                }
            }).show();
        } else {
            goOrBackItem(true);//下一项
        }
    }

    private void clickPreItem() {
        if (mPatrolItemProgress.getCurrentIndx() < 0) {
            mDialog.dismiss();
            mDialog.setContent("已达到第一项,是否退出?")
                    .showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    mActivity.finish();
                }
            }).show();
        } else {
            goOrBackItem(false);//上一项
        }
    }

    /**
     * 下(上)一项
     *
     * @param isGo true为下一项，否则为上一项
     */
    private void goOrBackItem(boolean isGo) {
        int currentIndex = mPatrolItemProgress.getCurrentIndx();
        int currentPager = mPatrolItemViewPager.getCurrentItem();
        if (!isGo) {//上一项
            mPatrolItemProgress.setCurrentIndx(--currentIndex);
            if (currentPager != (currentIndex + 1))
                mPatrolItemViewPager.setCurrentItem(--currentPager);
        } else {//下一项
            mPatrolItemProgress.setCurrentIndx(++currentIndex);
            if (currentPager != (currentIndex + 1))
                mPatrolItemViewPager.setCurrentItem(++currentPager);
        }
    }

    private void savePatrols() {
        SparseArray<LinearLayout> allItemView = mPatrolItemAdpter.mViews;//所有巡检项对应的item
        if (allItemView != null && allItemView.size() > 0) {
            int size = allItemView.size();
            for (int i = 0; i < size; i++) {
                PatrolItemData patrolItemData = mData.getPatrolItems().get(i);
                TextView patrolItemNormalView = (TextView) mPatrolItemAdpter.mViews.get(i).findViewById(R.id.public_patrol_nomal);//是否正常按钮
                EditText editText = (EditText) mPatrolItemAdpter.mViews.get(i).findViewById(R.id.public_patrol_record);//记录编辑框
                patrolItemData.setIsNormal(patrolItemNormalView.isSelected() ? 1 : 0);
                if (editText != null && !TextUtils.isEmpty(editText.getText())) {
                    patrolItemData.setRecordValue(editText.getText().toString());
                }
                String selection = PatrolItemTable.getPatrolItemTableInstance().getComlueInfos()[2].getName() + "=?";
                String[] args = new String[1];
                args[0] = patrolItemData.getId() + "";
                try {
                    PatrolItemTable.getPatrolItemTableInstance().updateData(patrolItemData, selection, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        mData.setDealResult("已处理");
        mData.setTaskState(Const.TASK_STATE_DEALED);
        mData.setRealEndTime(System.currentTimeMillis());
        mData.setDealInfo("此任务已被" + Const.getUserName(mActivity) + "处理!");
        String selection = TaskTable.getTaskTableInstance().getComlueInfos()[16].getName() + "=?";
        String[] args = new String[1];
        args[0] = mData.getTaskId() + "";
        try {
            TaskTable.getTaskTableInstance().updateData(mData, selection, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        int current = mPatrolItemProgress.getCurrentIndx();
        if (position > (current + 1)) {
            clickNextItem();
        } else if (position < (current + 1)) {
            clickPreItem();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private class PatrolItemAdpter extends PagerAdapter implements View.OnClickListener {
        private SparseArray<LinearLayout> mViews;

        public PatrolItemAdpter() {
            mViews = new SparseArray<>();
        }

        @Override
        public int getCount() {
            return mData.getPatrolItems().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object && view != null && object != null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mViews.get(position) == null) {
                LinearLayout view = (LinearLayout) LayoutInflater.from(mActivity).inflate(R.layout.public_patrol_item, null);
                PatrolItemData data = mData.getPatrolItems().get(position);
                //初始化视图
                initView(view);
                //填充数据
                fillData(data, view);
                mViews.put(position, view);
            }
            container.addView(mViews.get(position));
            return mViews.get(position);
        }

        private void initView(View view) {
            TextView patrolItemNormalView = (TextView) view.findViewById(R.id.public_patrol_nomal);
            TextView patrolItemUnNormalView = (TextView) view.findViewById(R.id.public_patrol_unnomal);
            TextView patrolItemDetailView = (TextView) view.findViewById(R.id.public_patrol_patrolDetail);
            updateNormalView(true, patrolItemNormalView, patrolItemUnNormalView);//默认为正常
            patrolItemNormalView.setOnClickListener(this);
            patrolItemUnNormalView.setOnClickListener(this);
        }

        /**
         * 更新是否正常的两个按钮显示界面（背景、字体颜色等）
         *
         * @param isNormal               是否正常
         * @param patrolItemNormalView   正常按钮
         * @param patrolItemUnNormalView 非正常按钮
         */
        private void updateNormalView(boolean isNormal, TextView patrolItemNormalView, TextView patrolItemUnNormalView) {
            if (isNormal) {
                patrolItemNormalView.setSelected(true);
                patrolItemNormalView.setTextColor(getResources().getColor(R.color.idc_f5f5f5));
                patrolItemUnNormalView.setSelected(false);
                patrolItemUnNormalView.setTextColor(getResources().getColor(R.color.idc_000000));
            } else {
                patrolItemNormalView.setSelected(false);
                patrolItemNormalView.setTextColor(getResources().getColor(R.color.idc_000000));
                patrolItemUnNormalView.setSelected(true);
                patrolItemUnNormalView.setTextColor(getResources().getColor(R.color.idc_f5f5f5));
            }
        }

        private void fillData(PatrolItemData data, View view) {
            TextView patrolItemNameView = (TextView) view.findViewById(R.id.public_patrol_patrolItemName);
            TextView patrolItemStudioView = (TextView) view.findViewById(R.id.public_patrol_patrolStudio);
            TextView patrolItemStepView = (TextView) view.findViewById(R.id.public_patrol_patrolStep);
            TextView patrolItemDetailView = (TextView) view.findViewById(R.id.public_patrol_patrolDetail);
            String nullData = getString(R.string.kong_shu_ju);
            initData(patrolItemNameView, R.string.xun_jian_xiang_ming_cheng_s, Const.isNullForDBData(data.getPatrolItemName()) ? nullData : data.getPatrolItemName(), R.color.idc_000000);
            initData(patrolItemStudioView, R.string.xun_jian_zhi_biao_s, Const.isNullForDBData(data.getPatrolStuido()) ? nullData : data.getPatrolStuido(), R.color.idc_000000);
            initData(patrolItemStepView, R.string.xun_jian_cuo_shi_s, Const.isNullForDBData(data.getPatrolStep()) ? nullData : data.getPatrolStep(), R.color.idc_000000);
            initData(patrolItemDetailView, R.string.xiang_xi_shuo_ming_s,  Const.isNullForDBData(data.getPatrolDetail()) ? nullData : data.getPatrolDetail(), R.color.idc_000000);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViews.get(position));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.public_patrol_patrolDetail:
                    mDialog.dismiss();
                    int i = mPatrolItemViewPager.getCurrentItem();
                    PatrolItemData data = mData.getPatrolItems().get(i);
                    String content = data.getPatrolDetail();
                    mDialog.setContent(Const.isNullForDBData(content) ? mActivity.getString(R.string.kong_shu_ju) : content).showCancelButton(View.GONE, null).showSureButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDialog.dismiss();
                        }
                    }).show();
                    break;
                case R.id.public_patrol_unnomal:
                case R.id.public_patrol_nomal:
                    if (!v.isSelected()) {
                        int position = mPatrolItemViewPager.getCurrentItem();
                        TextView patrolItemNormalView = (TextView) mViews.get(position).findViewById(R.id.public_patrol_nomal);
                        TextView patrolItemUnNormalView = (TextView) mViews.get(position).findViewById(R.id.public_patrol_unnomal);
                        updateNormalView(!patrolItemNormalView.isSelected(), patrolItemNormalView, patrolItemUnNormalView);
                    }
                    break;
            }
        }
    }
}
