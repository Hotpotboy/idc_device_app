package com.zhanghang.idcdevice;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Intents;
import com.zhanghang.idcdevice.fragment.HouseListFragment;
import com.zhanghang.idcdevice.fragment.PatrolItemsFragment;
import com.zhanghang.idcdevice.fragment.TaskDetailFragment;
import com.zhanghang.idcdevice.fragment.UserDetailFragment;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.base.BaseFragmentActivity;
import com.zhanghang.self.utils.PopupWindowUtils;
import com.zhanghang.self.utils.camera.CameraUtils;
import com.zxing.util.GenerateQRCode;

import java.util.ArrayList;

public class FragmentActivity extends BaseFragmentActivity {
    /**加载任务巡检页面*/
    public static final int LOAD_PATROL_ITEM_FRAGMENT = 1;
    /**用户详情页*/
    public static final int USER_DETAIL_FRAGMENT = 2;
    /**巡检任务详情页*/
    public static final int XUNJIAN_TASK_DETAIL_FRAGMENT = 3;
    /**盘点任务详情页*/
    public static final int PANDIAN_TASK_DETAIL_FRAGMENT = 4;
    private BaseFragment[] fragments;
    private PopupWindowUtils mNetLoadingWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments);
        Intent intent = getIntent();
        if(intent!=null) {
            int key = intent.getIntExtra(Const.INTENT_KEY_LOAD_FRAGMENT,-1);
            switch (key){
                case XUNJIAN_TASK_DETAIL_FRAGMENT://巡检任务详情页
                case LOAD_PATROL_ITEM_FRAGMENT:
                    TaskData data = (TaskData) intent.getSerializableExtra(Const.INTENT_KEY_TASK_DATA);
                    Bundle argments = new Bundle();
                    argments.putSerializable(Const.INTENT_KEY_TASK_DATA, data);
                    BaseFragment baseFragment = null;
                    if(key==LOAD_PATROL_ITEM_FRAGMENT) baseFragment = new PatrolItemsFragment();
                    else if(key== XUNJIAN_TASK_DETAIL_FRAGMENT) baseFragment = new TaskDetailFragment();
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
                case PANDIAN_TASK_DETAIL_FRAGMENT://盘点任务详情页
                    baseFragment = new HouseListFragment();
                    fragments = new BaseFragment[1];
                    fragments[0] = baseFragment;
                    initFragments(fragments, R.id.fragments_container);
                    break;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CameraUtils.SCANNER_QR_CODE_REQUEST_CODE://二维码扫描页面
                    String result = data.getStringExtra(Intents.Scan.RESULT);
                    if(mNetLoadingWindow==null){
                        mNetLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, this, getWindow().getDecorView(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    }
                    mNetLoadingWindow.showAtLocation();
                    ((TextView) mNetLoadingWindow.getViewById(R.id.net_loading_tip)).setText("解析二维码成功，正在查询符合条件的数据......");
                    final String[] resultArray = result.split("&");
                    String md5Result = GenerateQRCode.getMD5(resultArray[1]);
                    AsyncTask task = new AsyncTask<String, Void, ArrayList>() {

                        @Override
                        protected ArrayList<TaskData> doInBackground(String... params) {
                            return selectDataFromDb(params[1]);
                        }

                        @Override
                        public void onPostExecute(ArrayList datas) {
                            mNetLoadingWindow.getPopupWindow().dismiss();
                            try {
//                                selecteFitData(datas);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(FragmentActivity.this, "此设备无相应的任务，请扫描下一个设备!", Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    ((DeviceApplication)DeviceApplication.getInstance()).resolveScannerResult(md5Result,task);//解析结果
                    break;
            }
        }
    }

    private ArrayList selectDataFromDb(String param) {
        return null;
    }


}
