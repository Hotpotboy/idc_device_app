package com.zhanghang.idcdevice.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.PublicDialog;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.base.BaseFragment;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-29.
 */
public class TaskDetailFragment extends BaseFragment implements View.OnClickListener {
    private TextView mTitileLeft;
    private TextView mTitileCenter;
    /**
     * 任务数据
     */
    TaskData mData;
    /**
     * 巡检项详情布局
     */
    LinearLayout mPatrolInfosView;
    /**
     * 资产分类
     */
    private TextView mAssetTypeView;
    /**
     * 任务ID
     */
    private TextView mTaskIdView;
    /**
     * 任务名称
     */
    private TextView mTaskNameView;
    /**
     * 任务详情
     */
    private TextView mTaskDetailView;
    /**
     * 计划开始时间
     */
    private TextView mPlanStartTimeView;
    /**
     * 计划结束时间
     */
    private TextView mPlanEndTimeView;
    /**
     * 实际开始时间
     */
    private TextView mRealStartTimeView;
    /**
     * 实际结束时间
     */
    private TextView mRealEndTimeView;
    /**
     * 负责组别
     */
    private TextView mResponseGroupView;
    /**
     * 负责员工
     */
    private TextView mResponsePeopleView;
    private PublicDialog mDialog;

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
        mDialog = new PublicDialog(mActivity);
        initTitle();
        //巡检项信息
        mPatrolInfosView = (LinearLayout) findViewById(R.id.fragment_task_detail_patrolsInfos);
        //任务信息
        mAssetTypeView = (TextView) findViewById(R.id.fragment_task_detail_assetType);
        mTaskIdView = (TextView) findViewById(R.id.fragment_task_detail_taskId);
        mTaskNameView = (TextView) findViewById(R.id.fragment_task_detail_taskName);
        mTaskDetailView = (TextView) findViewById(R.id.fragment_task_detail_taskDetail);
        mPlanStartTimeView = (TextView) findViewById(R.id.fragment_task_detail_planStart);
        mPlanEndTimeView = (TextView) findViewById(R.id.fragment_task_detail_planEnd);
        mRealStartTimeView = (TextView) findViewById(R.id.fragment_task_detail_realStart);
        mRealEndTimeView = (TextView) findViewById(R.id.fragment_task_detail_realEnd);
        mResponseGroupView = (TextView) findViewById(R.id.fragment_task_detail_responseGroup);
        mResponsePeopleView = (TextView) findViewById(R.id.fragment_task_detail_responsePeople);
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
        initData(mAssetTypeView, R.string.zi_chan_fen_lei_s, Const.isNullForDBData(mData.getAssetType()) ? getString(R.string.kong_shu_ju) : Const.getTaskTypeNameByTaskTypNum(mData.getTaskType()), R.color.idc_000000);
        initData(mTaskIdView, R.string.ren_wu_bian_hao_s,  Const.isNullForDBData(mData.getTaskId()+"") ? getString(R.string.kong_shu_ju) : mData.getAssetNum(), R.color.idc_000000);
        initData(mTaskNameView, R.string.ren_wu_ming_cheng_s, mData.getTaskName(), R.color.idc_000000);
        initClickableData(mTaskDetailView, R.string.xiang_xi_miao_shu_s, getResources().getString(R.string.cha_kan_xiang_qing), R.color.idc_af0012);
        String time = Const.getDataString(mData.getPlanedEndTime());
        initData(mPlanEndTimeView, R.string.ji_hua_jie_su_shi_jian_s, Const.isNullForDBData(time) ? getString(R.string.kong_shu_ju) : time, R.color.idc_000000);
        time = Const.getDataString(mData.getPlanedStartTime());
        initData(mPlanStartTimeView, R.string.ji_hua_kai_shi_shi_jian_s, Const.isNullForDBData(time) ? getString(R.string.kong_shu_ju) : time, R.color.idc_000000);
        time = Const.getDataString(mData.getRealStartTime());
        initData(mRealStartTimeView, R.string.shi_ji_kai_shi_shi_jian_s, Const.isNullForDBData(time) ? getString(R.string.kong_shu_ju) : time, R.color.idc_000000);
        time = Const.getDataString(mData.getRealEndTime());
        initData(mRealEndTimeView, R.string.shi_ji_jie_su_shi_jian_s, Const.isNullForDBData(time) ? getString(R.string.kong_shu_ju) : time, R.color.idc_000000);
        initData(mResponseGroupView, R.string.fu_ze_zu_bie_s, Const.isNullForDBData(mData.getResponseGroup()) ? getString(R.string.kong_shu_ju) : mData.getResponseGroup(), R.color.idc_000000);
        initData(mResponsePeopleView, R.string.fu_ze_ren_yuan_s, Const.isNullForDBData(mData.getResponsePeople()) ? getString(R.string.kong_shu_ju) : mData.getResponsePeople(), R.color.idc_000000);
        initPatrolInfos();
    }
    private void initClickableData(TextView view, int formatStrId, String value, int colorId) {
        String content = String.format(getResources().getString(formatStrId), value);
        SpannableString contentSpannable = Const.changeClickColor(content, value, getResources().getColor(colorId), new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                mDialog.setContent(Const.isNullForDBData(mData.getDetails()) ? getString(R.string.kong_shu_ju) : mData.getDetails()).showCancelButton(View.GONE, null).showSureButton(new View.OnClickListener() {
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
            String value = patrolItemData.getNormal() == 1 ? "正常" : "异常";
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
