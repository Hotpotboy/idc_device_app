package com.zhanghang.idcdevice.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.FragmentActivity;
import com.zhanghang.idcdevice.MainActivity;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.adpter.BaseViewHolderAdapter;
import com.zhanghang.self.utils.camera.CameraUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2016-04-03.
 */
public class TaskAdapter extends BaseViewHolderAdapter {
    private static final String TASK_NAME_KEY = "task_name_key";
    private static final String TASK_DETAIL_KEY = "task_detail_key";
    private static final String TASK_OPERATION_KEY = "task_operation_key";
    private static final String TASK_DEALED_KEY = "task_dealed_key";

    public TaskAdapter(Context context, ArrayList list) {
        super(context, list);
    }

    @Override
    protected View inflaterView(int position) {
        return mLayoutInflater.inflate(R.layout.item_task,null);
    }

    @Override
    protected void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView) {
        TextView taskNameView = (TextView) getViewByTag(R.id.item_task_name,TASK_NAME_KEY,baseViewHolder,convertView);
        TextView taskDetail = (TextView) getViewByTag(R.id.item_task_details,TASK_DETAIL_KEY,baseViewHolder,convertView);
        TextView taskOperation = (TextView) getViewByTag(R.id.item_task_dealing,TASK_OPERATION_KEY,baseViewHolder,convertView);
        ImageView taskDealed = (ImageView) getViewByTag(R.id.item_task_dealed,TASK_DEALED_KEY,baseViewHolder,convertView);

        final TaskData data = (TaskData) getItem(position);
        taskNameView.setText(data.getTaskName());
        taskDetail.setText(data.getDetails());
        boolean showDeal = data.getRealStartTime()<=0;//是否显示处理按钮
        boolean showEdit = TextUtils.isEmpty(data.getDealResult());//是否显示编辑按钮
        if(showDeal){//能接受
            taskOperation.setVisibility(View.VISIBLE);
            taskDealed.setVisibility(View.GONE);
            taskOperation.setText("接受");
            taskOperation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    data.setRealStartTime(System.currentTimeMillis());
                    data.setDealPeople(Const.CURRENT_USER_NAME);
                    ((MainActivity)mContext).gotoScannQRCode(data);
                }
            });
        }else if(showEdit){//能编辑
            taskOperation.setVisibility(View.VISIBLE);
            taskDealed.setVisibility(View.GONE);
            taskOperation.setText("编辑");
            taskOperation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }else{//已处理
            taskOperation.setVisibility(View.GONE);
            taskDealed.setVisibility(View.VISIBLE);
        }
    }
}
