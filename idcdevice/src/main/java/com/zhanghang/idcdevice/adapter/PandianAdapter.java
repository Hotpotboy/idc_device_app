package com.zhanghang.idcdevice.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhanghang.idcdevice.R;
import com.zhanghang.self.adpter.BaseViewHolderExpandableAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/6/2.
 * 资产盘点适配器
 */
public class PandianAdapter extends BaseViewHolderExpandableAdapter {
    /**添加设备操作码*/
    public static final int OPERATION_CODE_ADD_DEVICE = 1;
    private static final String KEY_CUPBOARD_NUM = "key_cupboard_num";
    private static final String KEY_CUPBOARD_EXPAND = "key_cupboard_expand";
    private static final String KEY_CUPBOARD_ADDDEVICE = "key_cupboard_addDevice";
    private static final String KEY_DEVICE_NUM = "key_device_num";
    private static final String KEY_DEVICE_DELETE = "key_device_delete";
    /**操作接口*/
    private OperationListener mOperationListener;

    public PandianAdapter(Context context, ArrayList list, SparseArray<ArrayList> childList) {
        super(context, list, childList);
    }

    @Override
    protected View inflaterGroupView(int position) {
        return mLayoutInflater.inflate(R.layout.item_cupboard,null);
    }

    @Override
    protected View inflaterChildView(int groupPosition, int childPosition) {
        return mLayoutInflater.inflate(R.layout.item_device_num,null);
    }

    @Override
    protected void reBindDataAndGroupView(int groupPosition, boolean isExpanded, HashMap<String, View> baseViewHolder, View convertView) {
        TextView cupboardNum = (TextView) getViewByTag(R.id.item_cupboard_num,KEY_CUPBOARD_NUM,baseViewHolder,convertView);
        ImageView cupboardExpand = (ImageView) getViewByTag(R.id.item_cupboard_expand,KEY_CUPBOARD_EXPAND,baseViewHolder,convertView);
        TextView addDevice = (TextView) getViewByTag(R.id.item_cupboard_addDevice, KEY_CUPBOARD_ADDDEVICE, baseViewHolder, convertView);

        String num = (String) getGroup(groupPosition);
        cupboardNum.setText(String.format(mContext.getResources().getString(R.string.ji_gui_bian_hao_s),num));
        //判断是否展开
        if(isExpanded){//展开
            cupboardExpand.setImageResource(R.drawable.white_up_arrow);
            addDevice.setVisibility(View.VISIBLE);//能添加设备
            addDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOperationListener!=null){
                        mOperationListener.operation(OPERATION_CODE_ADD_DEVICE);
                    }
                }
            });
        }else{
            cupboardExpand.setImageResource(R.drawable.white_down_arrow);
            addDevice.setVisibility(View.GONE);//不能添加设备
        }
    }

    @Override
    protected void reBindDataAndChildView(int groupPosition, int childPosition, boolean isLastChild, HashMap<String, View> baseViewHolder, View convertView) {
        TextView deviceNum = (TextView) getViewByTag(R.id.item_device_num,KEY_DEVICE_NUM,baseViewHolder,convertView);
        Button deviceDelete = (Button) getViewByTag(R.id.item_cupboard_expand,KEY_DEVICE_DELETE,baseViewHolder,convertView);

        String num = (String) getChild(groupPosition,childPosition);
        deviceNum.setText(String.format(mContext.getResources().getString(R.string.she_bei_bian_hao_s),num));
        deviceDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void setOperationListener(OperationListener mOperationListener) {
        this.mOperationListener = mOperationListener;
    }

    /**
     * 操作接口
     */
    public interface OperationListener{
        /**
         *
         * @param operationCode   操作码
         */
        public void operation(int operationCode);
    }
}
