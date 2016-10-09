package com.zhanghang.idcdevice.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.interfaces.PandianOperationListener;
import com.zhanghang.idcdevice.mode.DBdata;
import com.zhanghang.idcdevice.mode.DeviceData;
import com.zhanghang.self.adpter.BaseViewHolderAdapter;
import com.zhanghang.self.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Administrator on 2016-04-03.
 */
public class CabinetAdapter extends BaseViewHolderAdapter {
    private static final String KEY_CUPBOARD_NUM = "key_cupboard_num";
    private static final String KEY_CUPBOARD_EXPAND = "key_cupboard_expand";
    private static final String KEY_CUPBOARD_DELETE = "key_cupboard_delete";
    private static final String KEY_CUPBOARD_NAME = "key_cupboard_name";
    private static final String KEY_CUPBOARD_TYPE = "key_cupboard_type";
    private static final String KEY_CUPBOARD_DEALED = "key_cupboard_dealed";
    private PandianOperationListener mPandianOperationListener;

    public CabinetAdapter(Context context, ArrayList<String> list) {
        super(context, list);
    }

    public void setPandianOperationListener(PandianOperationListener pandianOperationListener) {
        this.mPandianOperationListener = pandianOperationListener;
    }

    @Override
    protected View inflaterView(int position) {
        return mLayoutInflater.inflate(R.layout.item_cupboard,null);
    }

    @Override
    protected void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView) {
        TextView cabinetNumView = (TextView) getViewByTag(R.id.item_cupboard_num, KEY_CUPBOARD_NUM, baseViewHolder, convertView);
        TextView cabinetExpandView = (TextView) getViewByTag(R.id.item_cupboard_expand, KEY_CUPBOARD_EXPAND, baseViewHolder, convertView);
        TextView deleteCabinetView = (TextView) getViewByTag(R.id.item_cupboard_delete, KEY_CUPBOARD_DELETE, baseViewHolder, convertView);
        TextView cabinetNameView = (TextView) getViewByTag(R.id.item_cupboard_name, KEY_CUPBOARD_NAME, baseViewHolder, convertView);
        TextView cabinetTypeView = (TextView) getViewByTag(R.id.item_cupboard_type, KEY_CUPBOARD_TYPE, baseViewHolder, convertView);
        View cabinetDealedView = getViewByTag(R.id.item_cuboard_dealed,KEY_CUPBOARD_DEALED,baseViewHolder,convertView);

        final String cabinetNum = (String) mDatas.get(position);//机柜扫描编号
        DeviceData data = DBdata.getDeviceDataFromCached(cabinetNum, 1);
        if(data==null) data = new DeviceData();
        String nullData = mContext.getString(R.string.kong_shu_ju);
        cabinetNumView.setText(String.format(mContext.getResources().getString(R.string.ji_gui_ming_cheng_s), TextUtils.isEmpty(data.getDeviceName())?nullData:data.getDeviceName()));
        cabinetNameView.setText(String.format(mContext.getResources().getString(R.string.she_bei_ming_cheng_s), TextUtils.isEmpty(data.getDeviceName())?nullData:data.getDeviceName()));
        String content = nullData;
        if(!TextUtils.isEmpty(data.getAssetSerialNum())){
            content = data.getAssetSerialNum()+"/";
        }else{
            content += "/";
        }
        if(!TextUtils.isEmpty(data.getCity())){
            content += data.getCity()+"/";
        }else{
            content += nullData+"/";
        }
        if(!TextUtils.isEmpty(data.getIdcRoom())){
            content += data.getIdcRoom()+"/";
        }else{
            content += nullData+"/";
        }
        if(!TextUtils.isEmpty(data.getCabinet())){
            content += data.getCabinet();
        }else{
            content += nullData;
        }
        cabinetTypeView.setText(content);

        //获取已完成的机柜扫描码列表
        Set<String> completedCabinets = PreferenceUtil.getStringSetInPreferce(mContext, Const.PREFERENCE_FILE_NAME,Const.PREFERENCE_KEY_COMPLETED_CABINET);
        if(completedCabinets==null||!completedCabinets.contains(cabinetNum)) {//此机柜还未完成扫描
            cabinetExpandView.setVisibility(View.VISIBLE);
            cabinetDealedView.setVisibility(View.GONE);
            //扫描按钮点击
            cabinetExpandView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPandianOperationListener != null) {
                        mPandianOperationListener.operation(PandianOperationListener.OPERATION_CODE_OPEN_CABINET, cabinetNum);
                    }
                }
            });
        }else{//此机柜已经完成扫描
            cabinetDealedView.setVisibility(View.VISIBLE);
            cabinetExpandView.setVisibility(View.GONE);
        }
        //删除按钮点击
        deleteCabinetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPandianOperationListener != null) {
                    mPandianOperationListener.operation(PandianOperationListener.OPERATION_CODE_DELETE_CABINET,cabinetNum);
                }
            }
        });
    }
}
