package com.zhanghang.idcdevice;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

public class EditDialog extends PublicDialog {

    public EditDialog(Context context) {
        super(context);
        mContent.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.view_edit_content,mContent);
    }
    @Override
    public EditDialog setContent(String content) {
        return this;
    }
    /**获取修改值*/
    public String getEditValue(){
        EditText newValueView = (EditText) mContent.findViewById(R.id.view_content_new);
        return String.valueOf(newValueView.getText());
    }
}
