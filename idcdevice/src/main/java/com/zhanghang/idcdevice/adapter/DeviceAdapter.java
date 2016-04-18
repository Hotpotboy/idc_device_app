package com.zhanghang.idcdevice.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.adbsocket.Device;
import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.FragmentActivity;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.mode.DeviceData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.adpter.BaseViewHolderAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2016-04-03.
 */
public class DeviceAdapter extends BaseViewHolderAdapter {
    private static final String DEVICE_NAME_KEY = "device_name_key";
    private static final String DEVICE_TYPE_KEY = "device_type_key";

    public DeviceAdapter(Context context, ArrayList list) {
        super(context, list);
    }

    @Override
    protected View inflaterView(int position) {
        return mLayoutInflater.inflate(R.layout.item_device,null);
    }

    @Override
    protected void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView) {
        TextView deviceNameView = (TextView) getViewByTag(R.id.item_device_name,DEVICE_NAME_KEY,baseViewHolder,convertView);
        TextView deviceType = (TextView) getViewByTag(R.id.item_device_type,DEVICE_TYPE_KEY,baseViewHolder,convertView);

        final DeviceData data = (DeviceData) getItem(position);
        deviceNameView.setText(String.format(mContext.getResources().getString(R.string.she_bei_ming_cheng_s),data.getDeviceName()));
        deviceType.setText(String.format(mContext.getResources().getString(R.string.she_bei_lei_bie_s), data.getDeviceModel()));
    }
}
