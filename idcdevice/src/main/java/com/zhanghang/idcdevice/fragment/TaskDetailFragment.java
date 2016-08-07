package com.zhanghang.idcdevice.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.base.BaseFragment;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-29.
 */
public class TaskDetailFragment extends BaseFragment implements View.OnClickListener {
    /**
     * 设备ID
     */
    private TextView mDeviceIdView;
    /**
     * 任务名称
     */
    private TextView mTaskNameView;
    /**
     * 任务类别
     */
    private TextView mTaskTypeView;
    /**
     * 任务状态
     */
    private TextView mTaskStateView;
    /**
     * 负责组别
     */
    private TextView mResponseGroupView;
    /**
     * 负责员工
     */
    private TextView mResponsePeopleView;
    /**
     * 实施组别
     */
    private TextView mDealGroupView;
    /**
     * 实施员工
     */
    private TextView mDealPeopleView;
    /**
     * 实际开始
     */
    private TextView mRealStartTimeView;
    /**
     * 处理信息
     */
    private TextView mDealInfoView;
    /**
     * 处理结果
     */
    private TextView mDealResultView;
    /**
     * 实际结束
     */
    private TextView mRealEndTimeView;
    /**
     * 计划开始
     */
    private TextView mPlanStartTimeView;
    /**
     * 计划结束
     */
    private TextView mPlanEndTimeView;
    /**
     * 任务详情
     */
    private TextView mTaskDetailView;
    /**
     * 巡检项详情布局
     */
    LinearLayout mPatrolInfosView;
    /**
     * 任务数据
     */
    TaskData mData;
    private TextView mTitileLeft;
    private TextView mTitileCenter;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_task_detail;
    }

    @Override
    protected void initDataFromArguments(Bundle arguments) {
        mData = (TaskData) arguments.getSerializable(Const.INTENT_KEY_TASK_DATA);
    }

    void initTitle(){//标题
        mTitileLeft = (TextView) findViewById(R.id.fragment_title_left);
        mTitileCenter = (TextView) findViewById(R.id.fragment_title_center);
    }

    @Override
    protected void initView() {
        initTitle();
        //任务信息
        mDeviceIdView = (TextView) findViewById(R.id.fragment_task_detail_deviceID);
        mTaskNameView = (TextView) findViewById(R.id.fragment_task_detail_taskName);
        mDealPeopleView = (TextView) findViewById(R.id.fragment_task_detail_dealPeople);
        mRealStartTimeView = (TextView) findViewById(R.id.fragment_task_detail_startTime);
        mTaskStateView = (TextView) findViewById(R.id.fragment_task_detail_taskState);
        mTaskTypeView = (TextView) findViewById(R.id.fragment_task_detail_taskType);
        mResponseGroupView = (TextView) findViewById(R.id.fragment_task_detail_responseGroup);
        mResponsePeopleView = (TextView) findViewById(R.id.fragment_task_detail_responsePeople);
        mDealGroupView = (TextView) findViewById(R.id.fragment_task_detail_dealGroup);
        mDealInfoView = (TextView) findViewById(R.id.fragment_task_detail_dealInfo);
        mDealResultView = (TextView) findViewById(R.id.fragment_task_detail_dealResult);
        mRealEndTimeView = (TextView) findViewById(R.id.fragment_task_detail_endTime);
        mPlanEndTimeView = (TextView) findViewById(R.id.fragment_task_detail_planEnd);
        mPlanStartTimeView = (TextView) findViewById(R.id.fragment_task_detail_planStart);
        mPatrolInfosView = (LinearLayout) findViewById(R.id.fragment_task_detail_patrolsInfos);
        mTaskDetailView = (TextView) findViewById(R.id.fragment_task_detail_taskDetail);
    }

    /**
     * 初始化标题数据
     */
    void initTitleData(){
        //相关标题
        mTitileCenter.setText("任务详情");
        mTitileLeft.setText("返回");
        mTitileLeft.setBackgroundColor(Color.TRANSPARENT);
        mTitileLeft.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        initTitleData();
        //具体数据
        initData(mDeviceIdView, R.string.she_bei_bian_hao_s,  Const.isNullForDBData(mData.getAssetNum()) ? getString(R.string.kong_shu_ju) : mData.getAssetNum(), R.color.idc_000000);
        initData(mTaskNameView, R.string.ren_wu_ming_cheng_s, mData.getTaskName(), R.color.idc_000000);
        initData(mDealPeopleView, R.string.shi_shi_yuan_gong_s, Const.isNullForDBData(mData.getDealPeople()) ? getString(R.string.kong_shu_ju) : mData.getDealPeople(), R.color.idc_000000);
        String time = Const.getDataString(mData.getRealStartTime());
        initData(mRealStartTimeView, R.string.shi_ji_kai_shi_s, Const.isNullForDBData(time) ? getString(R.string.kong_shu_ju) : time, R.color.idc_000000);
        time = Const.getDataString(mData.getRealEndTime());
        initData(mRealEndTimeView, R.string.shi_ji_jie_su_s, Const.isNullForDBData(time) ? getString(R.string.kong_shu_ju) : time, R.color.idc_000000);
        time = Const.getDataString(mData.getPlanedEndTime());
        initData(mPlanEndTimeView, R.string.ji_hua_jie_su_s, Const.isNullForDBData(time) ? getString(R.string.kong_shu_ju) : time, R.color.idc_000000);
        time = Const.getDataString(mData.getPlanedStartTime());
        initData(mPlanStartTimeView, R.string.ji_hua_kai_shi_s, Const.isNullForDBData(time) ? getString(R.string.kong_shu_ju) : time, R.color.idc_000000);
        initData(mDealInfoView, R.string.chu_li_xin_xi_s, Const.isNullForDBData(mData.getDealInfo()) ? getString(R.string.kong_shu_ju) : mData.getDealInfo(), R.color.idc_000000);
        initData(mDealResultView, R.string.chu_li_jie_guo_s, Const.isNullForDBData(mData.getDealResult()) ? getString(R.string.kong_shu_ju) : mData.getDealResult(), R.color.idc_000000);
        initData(mTaskTypeView, R.string.ren_wu_lei_bie_s, Const.isNullForDBData(mData.getTaskType()) ? getString(R.string.kong_shu_ju) : Const.getTaskTypeNameByTaskTypNum(mData.getTaskType()), R.color.idc_000000);
        initData(mTaskStateView, R.string.ren_wu_zhuang_tai_s, Const.isNullForDBData(mData.getTaskState()) ? getString(R.string.kong_shu_ju) : mData.getTaskState(), R.color.idc_000000);
        initData(mResponseGroupView, R.string.fu_ze_zu_bie_s, Const.isNullForDBData(mData.getResponseGroup()) ? getString(R.string.kong_shu_ju) : mData.getResponseGroup(), R.color.idc_000000);
        initData(mResponsePeopleView, R.string.fu_ze_ren_yuan_s, Const.isNullForDBData(mData.getResponsePeople()) ? getString(R.string.kong_shu_ju) : mData.getResponsePeople(), R.color.idc_000000);
        initData(mDealGroupView, R.string.shi_shi_zu_bie_s, Const.isNullForDBData(mData.getDealGroup()) ? getString(R.string.kong_shu_ju) : mData.getDealInfo(), R.color.idc_000000);
        mTaskDetailView.setText(Const.isNullForDBData(mData.getDetails()) ? getString(R.string.kong_shu_ju) : mData.getDetails());
        initPatrolInfos();
    }

    private void initData(TextView view, int formatStrId, String value, int colorId) {
        String content = String.format(getResources().getString(formatStrId), value);
        SpannableString contentSpannable = Const.changeSubColor(content, value, getResources().getColor(colorId));
        view.setText(contentSpannable);
    }

    /**
     * 加载此任务的巡检项
     */
    void initPatrolInfos() {
        mPatrolInfosView.removeAllViews();
        ArrayList<PatrolItemData> patrolItemDatas = mData.getPatrolItems();
        if (patrolItemDatas != null && patrolItemDatas.size() > 0) {
            int i=0;
            for(PatrolItemData item:patrolItemDatas){
                View view = getView(i,item);
                mPatrolInfosView.addView(view);
                i++;
            }
        } else {
            View view = getView(0,null);
            mPatrolInfosView.addView(view);
        }
    }

    /**
     * 根据每一个巡检项的数据，生成展示其的视图
     * @param patrolItemData
     * @return
     */
    private View getView(int i,PatrolItemData patrolItemData) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.item_task_detail_patrol, null);
        TextView nameView = (TextView) view.findViewById(R.id.item_task_detail_patrol_name);
        TextView valueView = (TextView) view.findViewById(R.id.item_task_detail_patrol_value);
        if (patrolItemData != null) {
            valueView.setVisibility(View.VISIBLE);
            nameView.setText((i+1)+"、"+patrolItemData.getPatrolItemName());
            String value = patrolItemData.getNormal() == 1 ? "正常" : "不正常";
            value += Const.isNullForDBData(patrolItemData.getRecordValue()) ? "" : "(" + patrolItemData.getRecordValue() + ")";
            valueView.setText(value);
        } else {
            valueView.setVisibility(View.GONE);
            nameView.setText("无数据!");
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_title_left:
                mActivity.finish();
                break;
        }
    }
}
