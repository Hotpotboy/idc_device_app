package com.zhanghang.idcdevice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.zxing.Intents;
import com.zhanghang.idcdevice.fragment.PatrolItemsFragment;
import com.zhanghang.idcdevice.fragment.TaskDetailFragment;
import com.zhanghang.idcdevice.fragment.TaskFragment;
import com.zhanghang.idcdevice.fragment.UserDetailFragment;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.base.BaseFragmentActivity;
import com.zhanghang.self.utils.camera.CameraUtils;
import com.zxing.util.GenerateQRCode;

public class FragmentActivity extends BaseFragmentActivity {
    /**加载任务巡检页面*/
    public static final int LOAD_PATROL_ITEM_FRAGMENT = 1;
    /**用户详情页*/
    public static final int USER_DETAIL_FRAGMENT = 2;
    /**任务详情页*/
    public static final int TASK_DETAIL_FRAGMENT = 3;
    private BaseFragment[] fragments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments);
        Intent intent = getIntent();
        if(intent!=null) {
            int key = intent.getIntExtra(Const.INTENT_KEY_LOAD_FRAGMENT,-1);
            switch (key){
                case TASK_DETAIL_FRAGMENT://任务详情页
                case LOAD_PATROL_ITEM_FRAGMENT:
                    TaskData data = (TaskData) intent.getSerializableExtra(Const.INTENT_KEY_TASK_DATA);
                    Bundle argments = new Bundle();
                    argments.putSerializable(Const.INTENT_KEY_TASK_DATA, data);
                    BaseFragment baseFragment = null;
                    if(key==LOAD_PATROL_ITEM_FRAGMENT) baseFragment = new PatrolItemsFragment();
                    else if(key==TASK_DETAIL_FRAGMENT) baseFragment = new TaskDetailFragment();
                    if(baseFragment!=null) {
                        baseFragment.setArguments(argments);
                        fragments = new BaseFragment[1];
                        fragments[0] = baseFragment;
                        initFragments(fragments, R.id.fragments_container);
                    }
                    break;
                case USER_DETAIL_FRAGMENT:
                    UserDetailFragment userDetailFragment = new UserDetailFragment();
                    argments = new Bundle();
                    userDetailFragment.setArguments(argments);
                    fragments=new BaseFragment[1];
                    fragments[0] = userDetailFragment;
                    initFragments(fragments, R.id.fragments_container);
                    break;
            }
        }
    }
}
