package com.zhanghang.idcdevice.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.mode.pandian.HouseData;
import com.zhanghang.self.adpter.BaseViewHolderAdapter;
import com.zhanghang.self.utils.camera.CameraUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2016-04-03.
 * 机房适配器
 */
public class HouseAdapter extends BaseViewHolderAdapter {
    private static final String ROOM_NAME_KEY = "room_name_key";
    private static final String ROOM_LOCATION_KEY = "room_location_key";
    private static final String ROOM_OPERATION_KEY = "room_operation_key";

    public HouseAdapter(Context context, ArrayList list) {
        super(context, list);
    }

    @Override
    protected View inflaterView(int position) {
        return mLayoutInflater.inflate(R.layout.item_room,null);
    }

    @Override
    protected void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView) {
        TextView roomNameView = (TextView) getViewByTag(R.id.item_room_name,ROOM_NAME_KEY,baseViewHolder,convertView);
        TextView roomLocation = (TextView) getViewByTag(R.id.item_room_location,ROOM_LOCATION_KEY,baseViewHolder,convertView);
        TextView roomOperation = (TextView) getViewByTag(R.id.item_room_dealing,ROOM_OPERATION_KEY,baseViewHolder,convertView);

        final HouseData data = (HouseData) getItem(position);
        roomNameView.setText(data.getName());
        String location = data.getBuildName()+"-"+data.getFloorName();
        roomLocation.setText(String.format(mContext.getResources().getString(R.string.fang_jian_wei_zhi_s),location));
        roomOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraUtils.scannerQRCode((Activity)mContext);//进入二维码扫描页面
            }
        });
    }
}
