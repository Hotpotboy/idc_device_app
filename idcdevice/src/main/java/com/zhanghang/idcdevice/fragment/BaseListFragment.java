package com.zhanghang.idcdevice.fragment;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.adapter.TaskAdapter;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.adpter.BaseViewHolderAdapter;
import com.zhanghang.self.base.BaseFragment;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-29.
 */
public abstract class BaseListFragment<T> extends BaseFragment {
    /**任务列表视图*/
    ListView mListView;
    /**任务列表*/
    ArrayList<T> mDatas;
    /**无数据布局*/
    private LinearLayout mNoDataLayout;
    /**下载数据按钮*/
    private Button mDownloadButton;
    /**任务适配器*/
    BaseViewHolderAdapter mListAdapter;
    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_list;
    }

    @Override
    protected void initView(){
        mListView = (ListView)findViewById(R.id.all_list);
        mNoDataLayout = (LinearLayout) findViewById(R.id.public_no_data);
        mDownloadButton = (Button) findViewById(R.id.public_noData_downLoad);
    }

    @Override
    protected void initData(){
        loadData();
    }

    abstract void loadData();

    protected void showList(boolean isShowList){
        if(isShowList){
            mListView.setVisibility(View.VISIBLE);
            mNoDataLayout.setVisibility(View.GONE);
            mDownloadButton.setOnClickListener(null);
        }else{
            mListView.setVisibility(View.GONE);
            mNoDataLayout.setVisibility(View.VISIBLE);
            mDownloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((DeviceApplication) DeviceApplication.getInstance()).getDataFromPC(mActivity);
                }
            });
        }
    }
}
