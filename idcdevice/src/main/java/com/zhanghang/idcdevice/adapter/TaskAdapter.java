package com.zhanghang.idcdevice.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.FragmentActivity;
import com.zhanghang.idcdevice.MainActivity;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.db.TaskTable;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.adpter.BaseViewHolderAdapter;

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
        return mLayoutInflater.inflate(R.layout.item_task, null);
    }

    @Override
    protected void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView) {
        TextView taskNameView = (TextView) getViewByTag(R.id.item_task_name, TASK_NAME_KEY, baseViewHolder, convertView);
        TextView taskDetail = (TextView) getViewByTag(R.id.item_task_details, TASK_DETAIL_KEY, baseViewHolder, convertView);
        TextView taskOperation = (TextView) getViewByTag(R.id.item_task_dealing, TASK_OPERATION_KEY, baseViewHolder, convertView);
        TextView taskDealed = (TextView) getViewByTag(R.id.item_task_dealed, TASK_DEALED_KEY, baseViewHolder, convertView);

        final TaskData data = (TaskData) getItem(position);
        taskNameView.setText(data.getTaskName());
        taskDetail.setText(Const.isNullForDBData(data.getDetails())?mContext.getResources().getString(R.string.kong_shu_ju):data.getDetails());
        taskDealed.setText(data.getTaskState());
        taskOperation.setVisibility(View.VISIBLE);
        final String taskType = data.getTaskType();
        if(TextUtils.equals(taskType,Const.TASK_TYPE_XUNJIAN)) {//巡检任务
            taskOperation.setText("打开任务");
        }else if(TextUtils.equals(taskType,Const.TASK_TYPE_PANDIAN)) {//盘点任务
            taskOperation.setText("开始盘点");
        }
        if(TextUtils.equals(Const.TASK_STATE_DEALED,data.getTaskState())
                &&TextUtils.equals(Const.TASK_TYPE_PANDIAN,taskType)){
            taskOperation.setVisibility(View.INVISIBLE);
        }else {
            taskOperation.setVisibility(View.VISIBLE);
            taskOperation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = new Intent(mContext, FragmentActivity.class);
                    intent.putExtra(Const.INTENT_KEY_TASK_DATA, data);
                    if (TextUtils.equals(taskType, Const.TASK_TYPE_XUNJIAN)) {//巡检任务
                        intent.putExtra(Const.INTENT_KEY_LOAD_FRAGMENT, FragmentActivity.XUNJIAN_TASK_DETAIL_FRAGMENT);
                        mContext.startActivity(intent);
                    } else if (TextUtils.equals(taskType, Const.TASK_TYPE_PANDIAN)) {//盘点任务
                        intent.putExtra(Const.INTENT_KEY_LOAD_FRAGMENT, FragmentActivity.PANDIAN_TASK_DETAIL_FRAGMENT);
                        new AsyncTask<Void, Void, Boolean>() {
                            @Override
                            protected Boolean doInBackground(Void... params) {
                                data.setTaskState(Const.TASK_STATE_DEALING);
                                data.setDealPeople(Const.getUserName(mContext));
                                try {
                                    TaskTable.getTaskTableInstance().updateData(data, null, null);
                                } catch (Exception e) {
                                    return false;
                                }
                                return true;
                            }

                            @Override
                            protected void onPostExecute(Boolean result) {
                                if (result) {
                                    notifyDataSetChanged();
                                    mContext.startActivity(intent);
                                } else {
                                    Toast.makeText(mContext, "打开任务失败!", Toast.LENGTH_LONG).show();
                                }
                            }
                        }.execute();
                    }
                }
            });
        }
    }
}
