package com.zhanghang.idcdevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.fragment.PatrolItemsFragment;
import com.zhanghang.idcdevice.fragment.TaskDetailFragment;
import com.zhanghang.idcdevice.fragment.UserDetailFragment;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.base.BaseFragmentActivity;

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
