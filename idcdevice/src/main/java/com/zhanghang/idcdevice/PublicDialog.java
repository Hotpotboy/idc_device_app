package com.zhanghang.idcdevice;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhanghang.idcdevice.fragment.PatrolItemsFragment;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.base.BaseFragmentActivity;

public class PublicDialog extends Dialog {
    /**最小宽度*/
    private int mMinWidth;
    /**
     * 内容视图
     */
    FrameLayout mContent;
    /**
     * 确定按钮
     */
    private Button mSureButton;
    /**
     * 取消按钮
     */
    private Button mCancelButton;
    /**
     * 按钮之间的分隔符
     */
    public View mSplite;

    public PublicDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.public_dialog);
        mContent = (FrameLayout) findViewById(R.id.public_dialog_context_layout);
        mSureButton = (Button) findViewById(R.id.public_dialog_sure);
        mCancelButton = (Button) findViewById(R.id.public_dialog_cancel);
        mSplite = findViewById(R.id.public_dialog_button_splite);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCanceledOnTouchOutside(true);
        mMinWidth = (int) context.getResources().getDimension(R.dimen.five_hundred_dp);
    }

    public void setWindowWidth(int width){
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if(layoutParams!=null){
            layoutParams.width = width>mMinWidth?width:mMinWidth;
            getWindow().setAttributes(layoutParams);
        }
    }

    /**
     * 设置对话框的显示内容
     *
     * @param content
     * @return
     */
    public PublicDialog setContent(String content) {
        TextView textView = (TextView) mContent.getChildAt(0);
        textView.setText(content);
        return this;
    }
    public PublicDialog showCancelButton() {
        return showCancelButton(View.VISIBLE,null);
    }

    /**
     * 设置取消按钮
     *
     * @param visible         取消按钮的可视状态
     * @param onClickListener 取消按钮的点击效果，可为空，为空默认点击取消对话框
     * @return
     */
    public PublicDialog showCancelButton(int visible, View.OnClickListener onClickListener) {
        if (visible == View.VISIBLE) {
            mCancelButton.setVisibility(visible);
            mSplite.setVisibility(visible);
            if (onClickListener != null) {
                mCancelButton.setOnClickListener(onClickListener);
            } else {
                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }
        } else {
            mCancelButton.setVisibility(View.GONE);
            mSplite.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * 设置确定按钮
     *
     * @param onClickListener 确定按钮的点击效果，可为空，为空默认点击取消对话框
     * @return
     */
    public PublicDialog showSureButton(View.OnClickListener onClickListener) {
        if (onClickListener != null) {
            mSureButton.setOnClickListener(onClickListener);
        } else {
            mSureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        return this;
    }
}
