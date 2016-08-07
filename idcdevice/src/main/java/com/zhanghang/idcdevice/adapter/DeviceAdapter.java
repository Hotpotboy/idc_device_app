package com.zhanghang.idcdevice.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.interfaces.PandianOperationListener;
import com.zhanghang.idcdevice.mode.DBdata;
import com.zhanghang.idcdevice.mode.DeviceData;
import com.zhanghang.self.adpter.BaseViewHolderAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2016-04-03.
 * 机房适配器
 */
public class DeviceAdapter extends BaseViewHolderAdapter {
    private static final String DEVICE_NAME_KEY = "device_name_key";
    private static final String DEVICE_NUM_KEY = "device_num_key";
    private static final String DEVICE_DELETE_KEY = "device_delete_key";
    private PandianOperationListener mPandianOperationListener;

    private int mSelected = -1;

    public DeviceAdapter(Context context, ArrayList<String> list) {
        super(context, list);
    }

    @Override
    protected View inflaterView(int position) {
        return mLayoutInflater.inflate(R.layout.item_device,null);
    }

    public void setPandianOperationListener(PandianOperationListener pandianOperationListener) {
        mPandianOperationListener = pandianOperationListener;
    }

    @Override
    protected void reBindDataAndView(final int position, HashMap<String, View> baseViewHolder, View convertView) {
        TextView deviceNameView = (TextView) getViewByTag(R.id.item_device_name,DEVICE_NAME_KEY,baseViewHolder,convertView);
        TextView deviceNumView = (TextView) getViewByTag(R.id.item_device_num,DEVICE_NUM_KEY,baseViewHolder,convertView);
        Button deviceDeleteView = (Button) getViewByTag(R.id.fragment_device_deleteNum, DEVICE_DELETE_KEY, baseViewHolder, convertView);

        final String data = (String) mDatas.get(position);
        deviceNumView.setText(String.format(mContext.getString(R.string.she_bei_sao_miao_ma_s), data));
        DeviceData deviceData = DBdata.getDeviceDataFromCached(data,0);
        if(deviceData==null|| TextUtils.isEmpty(deviceData.getDeviceName())){
            deviceNameView.setText(String.format(mContext.getString(R.string.she_bei_ming_cheng_s),mContext.getString(R.string.kong_shu_ju)));
        }else{
            deviceNameView.setText(String.format(mContext.getString(R.string.she_bei_ming_cheng_s),deviceData.getDeviceName()));
        }

        deviceDeleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPandianOperationListener!=null){
                    mPandianOperationListener.operation(PandianOperationListener.OPERATION_CODE_DELETE_DEVICE,data);
                }
            }
        });
        if(position!=mSelected) {
            convertView.setBackgroundColor(Color.WHITE);
        }else{
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.idc_f5dcdd));
        }
    }

    public void setSelected(int selected) {
        mSelected = selected;
        notifyDataSetChanged();
    }

    public int getSelected(){
        return mSelected;
    }
}
