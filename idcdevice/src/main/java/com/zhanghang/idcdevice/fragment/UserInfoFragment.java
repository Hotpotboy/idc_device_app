package com.zhanghang.idcdevice.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adbsocket.AdbSocketUtils;
import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.FragmentActivity;
import com.zhanghang.idcdevice.LoginActivity;
import com.zhanghang.idcdevice.PublicDialog;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.adbsocket.Request;
import com.zhanghang.idcdevice.db.DeviceTable;
import com.zhanghang.idcdevice.db.PatrolItemTable;
import com.zhanghang.idcdevice.db.TaskTable;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.utils.PopupWindowUtils;

/**
 * Created by Administrator on 2016-03-29.
 */
public class UserInfoFragment extends BaseFragment implements View.OnClickListener {
    /**退出按钮*/
    private Button mLoginOutButton;
    /**上传按钮*/
    private Button mUploadButton;
    private PopupWindowUtils mNetLoadingWindow;
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
        mNetLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, mActivity, mActivity.getWindow().getDecorView(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
                if(Const.isConnetionToPc()) {
                    final PublicDialog dialog = new PublicDialog(mActivity);
                    dialog.setContent("即将上传本地数据库!").showCancelButton(View.GONE, null).showSureButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            ((TextView) mNetLoadingWindow.getViewById(R.id.net_loading_tip)).setText("正在上传数据库......");
                            mNetLoadingWindow.showAtLocation();
                            final String datas = ((DeviceApplication) DeviceApplication.getInstance()).sendDataToPc();
                            if (TextUtils.isEmpty(datas)) {
                                mNetLoadingWindow.getPopupWindow().dismiss();
                                Toast.makeText(mActivity, "从数据库中获取的数据为空!", Toast.LENGTH_LONG).show();
                            } else {
                                Request.addRequestForCode(AdbSocketUtils.UPLOAD_DB_COMMAND, datas, new Request.CallBack() {
                                    @Override
                                    public void onSuccess(String result) {
                                        mNetLoadingWindow.getPopupWindow().dismiss();
                                        /**删除相关表*/
                                        DeviceTable.getDeviceTableInstance().deleteTable();
                                        TaskTable.getTaskTableInstance().deleteTable();
                                        PatrolItemTable.getPatrolItemTableInstance().deleteTable();
                                        Toast.makeText(mActivity, "成功上传数据库!", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onFail(String erroInfo) {
                                        mNetLoadingWindow.getPopupWindow().dismiss();
                                        Toast.makeText(mActivity, "上传数据库失败!原因【"+erroInfo+"】"+erroInfo, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }).show();
                }else{
                    Toast.makeText(mActivity, "上传数据之前，请用USB连接线与PC端相连接……", Toast.LENGTH_LONG).show();
                }
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
