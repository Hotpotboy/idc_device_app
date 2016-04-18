package com.zhanghang.idcdevice.fragment;

import android.widget.ListView;

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
    /**任务适配器*/
    BaseViewHolderAdapter mListAdapter;
    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_list;
    }

    @Override
    protected void initView(){
        mListView = (ListView)findViewById(R.id.all_list);
    }

    @Override
    protected void initData(){
        loadData();
    }

    abstract void loadData();
}
