package com.zhanghang.idcdevice.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.db.PandianResultTable;
import com.zhanghang.idcdevice.mode.pandian.PandianResultData;
import com.zhanghang.self.adpter.BaseViewHolderExpandableAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/6/2.
 * 资产盘点适配器
 */
public class PandianAdapter extends BaseViewHolderExpandableAdapter {
    /**
     * 添加设备操作码
     */
    public static final int OPERATION_CODE_ADD_DEVICE = 1;/**
     * 删除设备操作码
     */
    public static final int OPERATION_CODE_DELETE_DEVICE = 2;
    private static final String KEY_CUPBOARD_NUM = "key_cupboard_num";
    private static final String KEY_CUPBOARD_EXPAND = "key_cupboard_expand";
    private static final String KEY_CUPBOARD_ADDDEVICE = "key_cupboard_addDevice";
    private static final String KEY_DEVICE_NUM = "key_device_num";
    private static final String KEY_DEVICE_DELETE = "key_device_delete";
    /**
     * 操作接口
     */
    private OperationListener mOperationListener;
    /**
     * 房间编号
     */
    private String mHouseCode;

    public PandianAdapter(Context context, ArrayList list, SparseArray<ArrayList> childList, String houseCode) {
        super(context, list, childList);
        mHouseCode = houseCode;
    }

    @Override
    protected View inflaterGroupView(int position) {
        return mLayoutInflater.inflate(R.layout.item_cupboard, null);
    }

    @Override
    protected View inflaterChildView(int groupPosition, int childPosition) {
        return mLayoutInflater.inflate(R.layout.item_device_num, null);
    }

    @Override
    protected void reBindDataAndGroupView(int groupPosition, boolean isExpanded, HashMap<String, View> baseViewHolder, View convertView) {
        TextView cupboardNum = (TextView) getViewByTag(R.id.item_cupboard_num, KEY_CUPBOARD_NUM, baseViewHolder, convertView);
        ImageView cupboardExpand = (ImageView) getViewByTag(R.id.item_cupboard_expand, KEY_CUPBOARD_EXPAND, baseViewHolder, convertView);
        TextView addDevice = (TextView) getViewByTag(R.id.item_cupboard_addDevice, KEY_CUPBOARD_ADDDEVICE, baseViewHolder, convertView);

        String num = (String) getGroup(groupPosition);
        cupboardNum.setText(String.format(mContext.getResources().getString(R.string.ji_gui_bian_hao_s), num));
        //判断是否展开
        if (isExpanded) {//展开
            cupboardExpand.setImageResource(R.drawable.white_up_arrow);
            addDevice.setVisibility(View.VISIBLE);//能添加设备
            addDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOperationListener != null) {
                        mOperationListener.operation(OPERATION_CODE_ADD_DEVICE,null);
                    }
                }
            });
        } else {
            cupboardExpand.setImageResource(R.drawable.white_down_arrow);
            addDevice.setVisibility(View.GONE);//不能添加设备
        }
    }

    @Override
    protected void reBindDataAndChildView(final int groupPosition, final int childPosition, boolean isLastChild, HashMap<String, View> baseViewHolder, View convertView) {
        TextView deviceNum = (TextView) getViewByTag(R.id.item_device_num, KEY_DEVICE_NUM, baseViewHolder, convertView);
        final Button deviceDelete = (Button) getViewByTag(R.id.item_device_num_delete, KEY_DEVICE_DELETE, baseViewHolder, convertView);
        deviceDelete.setText("删除编号");
        String num = (String) getChild(groupPosition, childPosition);
        deviceNum.setText(String.format(mContext.getResources().getString(R.string.she_bei_bian_hao_s), num));
        deviceDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] params = new String[2];
                params[0] = (String) getGroup(groupPosition);
                params[1] = (String) getChild(groupPosition, childPosition);
                deviceDelete.setText("正在删除...");
                new DeleteDeviceTask(groupPosition, childPosition).execute(params);
            }
        });
    }

    public void setOperationListener(OperationListener mOperationListener) {
        this.mOperationListener = mOperationListener;
    }

    /**
     * 删除设备任务
     */
    private class DeleteDeviceTask extends AsyncTask<String, Void, PandianResultData> {
        private int mGroupIndex;
        private int mChildIndex;

        private DeleteDeviceTask(int groupIndex, int childIndex) {
            mGroupIndex = groupIndex;
            mChildIndex = childIndex;
        }

        @Override
        protected PandianResultData doInBackground(String... params) {
            try {
                String buildNumCol = PandianResultTable.getComlueInfos(PandianResultData.class).get(0).getName();
                String cupboardNumCol = PandianResultTable.getComlueInfos(PandianResultData.class).get(1).getName();
                String deviceNumCol = PandianResultTable.getComlueInfos(PandianResultData.class).get(2).getName();
                String selecte = buildNumCol + "=? and " + cupboardNumCol + "=?";
                String[] selecteArgs = new String[2];
                selecteArgs[0] = mHouseCode;
                selecteArgs[1] = params[0];
                ArrayList<PandianResultData> pandianResultDatas = PandianResultTable.getPandianTableInstance().selectDatas(selecte, selecteArgs, null, null, null, PandianResultData.class);
                PandianResultData pandianResultData = null;
                if (pandianResultDatas != null && pandianResultDatas.size() > 1) {//如果不是最后一个直接删除
                    for(PandianResultData item:pandianResultDatas){
                        if(TextUtils.equals(item.getBuildNum(),mHouseCode)
                                &&TextUtils.equals(item.getCupboardNum(),params[0])
                                &&TextUtils.equals(item.getDeviceNum(),params[1])){
                            pandianResultData = item;
                            break;
                        }
                    }
                    if(pandianResultData==null) throw  new RuntimeException("数据库中没有此记录!");
                    selecte += " and " + deviceNumCol + "=?";
                    String[] selecteArgs1 = new String[3];
                    selecteArgs1[0] = mHouseCode;
                    selecteArgs1[1] = params[0];
                    selecteArgs1[2] = params[1];
                    PandianResultTable.getPandianTableInstance().deleteData(selecte, selecteArgs1);
                } else if (pandianResultDatas != null && pandianResultDatas.size() == 1) {//如果是最后一个
                    pandianResultData = pandianResultDatas.get(0);
                    pandianResultData.setDeviceNum("");
                    PandianResultTable.getPandianTableInstance().updateData(pandianResultData, null, null);
                }
                return pandianResultData;
            } catch (Exception e) {
                Toast.makeText(mContext, "删除失败!"+e.toString(), Toast.LENGTH_LONG).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(PandianResultData result) {
            if (result!=null) {
                ArrayList mChildDats = mChildDatas.get(mGroupIndex);
                mChildDats.remove(mChildIndex);
                notifyDataSetChanged();
                if (mOperationListener != null) {
                    mOperationListener.operation(OPERATION_CODE_DELETE_DEVICE,result);
                }
            }
        }
    }

    /**
     * 操作接口
     */
    public interface OperationListener {
        /**
         * @param operationCode 操作码
         * @param ext           额外数据
         */
        public void operation(int operationCode, Object ext);
    }
}
