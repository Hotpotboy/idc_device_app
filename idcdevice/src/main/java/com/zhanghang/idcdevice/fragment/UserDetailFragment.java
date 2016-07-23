package com.zhanghang.idcdevice.fragment;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.R;
import com.zhanghang.self.base.BaseFragment;

/**
 * Created by Administrator on 2016-04-24.
 */
public class UserDetailFragment extends BaseFragment{
    private TextView mTitileCenter;
    private TextView mTitileLeft;
    private TextView mNiceNameView;
    private TextView mLoginNameView;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_user_detail;
    }

    @Override
    protected void initView(){
        //标题
        mTitileCenter = (TextView) findViewById(R.id.fragment_title_center);
        mTitileLeft = (TextView) findViewById(R.id.fragment_title_left);

        mNiceNameView = (TextView)findViewById(R.id.my_info_niceName);
        mLoginNameView = (TextView)findViewById(R.id.my_info_loginName);
        mNiceNameView.setText(Const.getUserName(mActivity));
        mLoginNameView.setText(Const.getUserName(mActivity));
    }

    @Override
    protected void initData() {
        mTitileCenter.setText("个人详情");
        mTitileLeft.setText("返回");
        mTitileLeft.setBackgroundColor(Color.TRANSPARENT);
        mTitileLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
    }
}
