package com.zhanghang.idcdevice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adbsocket.AdbSocketUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhanghang.idcdevice.adbsocket.Request;
import com.zhanghang.idcdevice.mode.LoginResultData;
import com.zhanghang.self.utils.PopupWindowUtils;
import com.zhanghang.self.utils.PreferenceUtil;

import java.io.IOException;

/**
 * Created by Administrator on 2016-04-20.
 */
public class LoginActivity extends Activity implements View.OnClickListener,TextView.OnEditorActionListener {
    /**用户名视图*/
    private EditText mUserNameView;
    /**密码视图*/
    private EditText mPasswordView;
    /**登陆按钮*/
    private Button mLoginButton;
    /**网络loading*/
    private PopupWindowUtils mNetLoadingWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUserNameView = (EditText) findViewById(R.id.login_username);
        mPasswordView = (EditText) findViewById(R.id.login_password);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mNetLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading,this,getWindow().getDecorView(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLoginButton.setOnClickListener(this);
        mUserNameView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mPasswordView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mUserNameView.setOnEditorActionListener(this);
        mPasswordView.setOnEditorActionListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_button:
                Editable userName = mUserNameView.getText();
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(this,"用户名不能为空!",Toast.LENGTH_LONG).show();
                    return;
                }
                Editable password = mPasswordView.getText();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this,"密码不能为空!",Toast.LENGTH_LONG).show();
                    return;
                }
                login(userName.toString(),password.toString());
                break;
        }
    }
    private void login(final String userName,String password){
        if(Const.isConnetionToPc()){
            String params = "{\"userName\":\""+userName+"\",\"password\":\""+password+"\"}";
            mNetLoadingWindow.showAtLocation();
            ((TextView)mNetLoadingWindow.getViewById(R.id.net_loading_tip)).setText("正在登陆中......");
            Request.addRequestForCode(AdbSocketUtils.LOGIN_IN_COMMANDE, params, new Request.CallBack() {
                @Override
                public void onSuccess(String result) {
                    mNetLoadingWindow.getPopupWindow().dismiss();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        LoginResultData loginResultData = objectMapper.readValue(result,LoginResultData.class);
                        if("true".equals(loginResultData.getIsSucc())) {
                            Const.setUserName(LoginActivity.this,userName);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(LoginActivity.this,"用户名或者密码错误~!",Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this,"解析错误!",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFail(String erroInfo) {
                    mNetLoadingWindow.getPopupWindow().dismiss();
                    Toast.makeText(LoginActivity.this,"与PC通信失败,【"+erroInfo+"】……",Toast.LENGTH_LONG).show();
                }
            });
        }else{
            Toast.makeText(this,"未与PC连接，请用USB数据线连接后重试……",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            //这里写你要在用户按下返回键同时执行的动作
            ((DeviceApplication)DeviceApplication.getInstance()).stop(this);
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId== EditorInfo.IME_ACTION_DONE){
            if(v.getId()==R.id.login_username){
                mUserNameView.clearFocus();
                mPasswordView.requestFocus();
            }else if(v.getId()==R.id.login_password){
                mPasswordView.clearFocus();
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
            }
        }
        return false;
    }
}
