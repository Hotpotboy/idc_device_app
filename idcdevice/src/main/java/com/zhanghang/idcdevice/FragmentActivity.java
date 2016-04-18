package com.zhanghang.idcdevice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.zxing.Intents;
import com.zhanghang.idcdevice.fragment.PatrolItemsFragment;
import com.zhanghang.idcdevice.fragment.TaskFragment;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.base.BaseFragmentActivity;
import com.zhanghang.self.utils.camera.CameraUtils;
import com.zxing.util.GenerateQRCode;

public class FragmentActivity extends BaseFragmentActivity {
    /**加载任务巡检页面*/
    public static final int LOAD_PATROL_ITEM_FRAGMENT = 1;
    private BaseFragment[] fragments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments);
        Intent intent = getIntent();
        if(intent!=null) {
            int key = intent.getIntExtra(Const.INTENT_KEY_LOAD_FRAGMENT,-1);
            switch (key){
                case LOAD_PATROL_ITEM_FRAGMENT:
                    PatrolItemsFragment patrolItemsFragment = new PatrolItemsFragment();
                    TaskData data = (TaskData) intent.getSerializableExtra(Const.INTENT_KEY_TASK_DATA);
                    String deviceId = intent.getStringExtra(Const.INTENT_KEY_DEVICE_ID);
                    Bundle argments = new Bundle();
                    argments.putSerializable(Const.INTENT_KEY_TASK_DATA, data);
                    argments.putString(Const.INTENT_KEY_DEVICE_ID,deviceId);
                    patrolItemsFragment.setArguments(argments);
                    fragments=new BaseFragment[1];
                    fragments[0] = patrolItemsFragment;
                    initFragments(fragments, R.id.fragments_container);
                    break;
            }
        }
    }
}
