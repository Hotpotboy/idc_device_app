package com.zhanghang.idcdevice.fragment;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.zhanghang.idcdevice.R;
import com.zhanghang.self.base.BaseFragment;

/**
 * Created by Administrator on 2016-04-24.
 */
public class UserDetailFragment extends BaseFragment{
    private TextView mTitileCenter;
    private TextView mTitileLeft;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_user_detail;
    }

    @Override
    protected void initView(){
        //标题
        mTitileCenter = (TextView) findViewById(R.id.fragment_title_center);
        mTitileLeft = (TextView) findViewById(R.id.fragment_title_left);
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
