package com.zhanghang.idcdevice.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.FragmentActivity;
import com.zhanghang.idcdevice.LoginActivity;
import com.zhanghang.idcdevice.R;
import com.zhanghang.self.base.BaseFragment;

/**
 * Created by Administrator on 2016-03-29.
 */
public class UserInfoFragment extends BaseFragment implements View.OnClickListener {
    /**退出按钮*/
    private Button mLoginOutButton;
    /**上传按钮*/
    private Button mUploadButton;
    /**用户页面*/
    private LinearLayout mMyView;
    /**用户名称*/
    private TextView mUserNameView;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_user;
    }

    @Override
    public void initView(){
        mUserNameView = (TextView) findViewById(R.id.user_name_show);
        mLoginOutButton = (Button) findViewById(R.id.user_login_out);
        mUploadButton = (Button) findViewById(R.id.user_upload_data);
        mMyView = (LinearLayout) findViewById(R.id.user_name);
        mLoginOutButton.setOnClickListener(this);
        mUploadButton.setOnClickListener(this);
        mMyView.setOnClickListener(this);
        mUserNameView.setText(Const.getUserName(mActivity));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_name:
                Intent intent = new Intent(mActivity, FragmentActivity.class);
                intent.putExtra(Const.INTENT_KEY_LOAD_FRAGMENT, FragmentActivity.USER_DETAIL_FRAGMENT);
                startActivity(intent);
                break;
            case R.id.user_upload_data:
                ((DeviceApplication)DeviceApplication.getInstance()).uploadDataToPc(mActivity);
                break;
            case R.id.user_login_out:
                if (((DeviceApplication) DeviceApplication.getInstance()).isUploadData()) {
                    Const.setUserName(mActivity,"");//清空用户
                    intent = new Intent(mActivity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    mActivity.finish();
                }else{
                    Toast.makeText(mActivity, "还未上传数据，退出之前，请点击【上传当前数据】按钮进行数据上传!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
