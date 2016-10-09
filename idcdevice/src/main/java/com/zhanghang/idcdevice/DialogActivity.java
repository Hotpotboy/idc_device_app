package com.zhanghang.idcdevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public class DialogActivity extends Activity {
    @Override
    public void onNewIntent(Intent intent){
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.net_loading);
        String tip = getIntent().getStringExtra(Const.INTENT_KEY_DIALOG_ACTIVITY_SHOW);
        if(!TextUtils.isEmpty(tip)){
            ((TextView)findViewById(R.id.net_loading_tip)).setText(tip);
        }
    }
}
