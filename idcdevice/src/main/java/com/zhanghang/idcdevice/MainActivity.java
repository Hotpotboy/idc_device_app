package com.zhanghang.idcdevice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.zxing.Intents;
import com.zhanghang.idcdevice.fragment.PatrolItemsFragment;
import com.zhanghang.idcdevice.fragment.TaskFragment;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.utils.camera.CameraUtils;
import com.zxing.util.GenerateQRCode;

import java.util.List;

public class MainActivity extends AppCompatActivity{
    /**抽屉视图*/
    private DrawerLayout mDrawerLayout;
    /**从扫描二维码页面返回回来时，缓存的任务数据*/
    private TaskData mCacheTaskData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
    }
    public void openDrawer(){
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public void  setDrawerListener(final DrawerLayout.DrawerListener listener){
        if(mDrawerLayout!=null) {
            if (listener != null) {
                mDrawerLayout.setDrawerListener(listener);
            }
        }else{
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    setDrawerListener(listener);
                }
            });
        }
    }

    public void gotoScannQRCode(TaskData data){
        CameraUtils.scannerQRCode(this);
        mCacheTaskData = data;
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(intent!=null){
            TaskData data = (TaskData) intent.getSerializableExtra(Const.INTENT_KEY_TASK_DATA);
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for(Fragment item:fragments){
                if(item instanceof TaskFragment){
                    ((TaskFragment)item).updateList(data);
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CameraUtils.SCANNER_QR_CODE_REQUEST_CODE://二维码扫描页面
                    String result = data.getStringExtra(Intents.Scan.RESULT);
                    if(!TextUtils.isEmpty(result)&&result.indexOf("&")>=0){
                        String[] resultArray = result.split("&");
                        String md5Result = GenerateQRCode.getMD5(resultArray[1]);
                        if(resultArray[0].equals(md5Result)&&mCacheTaskData!=null){//签名正确，刷新巡检页面的设备Id
                            Intent intent = new Intent(this, FragmentActivity.class);
                            intent.putExtra(Const.INTENT_KEY_LOAD_FRAGMENT, FragmentActivity.LOAD_PATROL_ITEM_FRAGMENT);
                            intent.putExtra(Const.INTENT_KEY_TASK_DATA, mCacheTaskData);
                            intent.putExtra(Const.INTENT_KEY_DEVICE_ID,resultArray[1]);
                            startActivity(intent);
                        }
                    }
                    break;
            }
        }
    }
}
