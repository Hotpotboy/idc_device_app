package com.zhanghang.idcdevice;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

public class EditDeviceDialog extends PublicDialog {

    public EditDeviceDialog(Context context) {
        super(context);
        mContent.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.view_edit_device_content,mContent);
    }
    @Override
    public EditDeviceDialog setContent(String content) {
        TextView oldView = (TextView) mContent.findViewById(R.id.view_device_old);
        SpannableString contentSpan = Const.changeSubColor(String.format(mContent.getResources().getString(R.string.yuan_shi_zhi_s), content), content, Color.BLACK);
        oldView.setText(contentSpan);
        mContent.measure(0,0);
        int width = (int) (mContent.getMeasuredWidth()+getContext().getResources().getDimension(R.dimen.thirty_two_dp)*2);
        setWindowWidth(width);
        EditText newValueView = (EditText) mContent.findViewById(R.id.view_device_new);
        newValueView.setText("");
        return this;
    }
    /**获取修改值*/
    public String getEditValue(){
        EditText newValueView = (EditText) mContent.findViewById(R.id.view_device_new);
        return String.valueOf(newValueView.getText());
    }
}
