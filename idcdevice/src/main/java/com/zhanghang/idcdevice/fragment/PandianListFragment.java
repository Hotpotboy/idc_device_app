package com.zhanghang.idcdevice.fragment;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ExpandableListView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.adapter.PandianAdapter;
import com.zhanghang.idcdevice.mode.pandian.PandianResultData;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/6/2.
 * 盘点任务列表
 */
public class PandianListFragment extends BaseListFragment<PandianResultData> implements View.OnClickListener {
    /**列表视图*/
    private ExpandableListView mPanListView;
    /**盘点任务适配器*/
    private PandianAdapter mPanListAdapter;
    @Override
    protected void initDataFromArguments(Bundle arguments){
         mDatas = (ArrayList<PandianResultData>) arguments.get(Const.INTENT_KEY_PANDIAN_TASK_DATA_LIST);
    }
    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_expend_list;
    }

    @Override
    protected void initView(){
        super.initView();
        mPanListView = (ExpandableListView) mListView;
    }

    @Override
    void loadData() {
        if(mDatas!=null&&mDatas.size()>0){
            showList(true);
            ArrayList<String> cupboardNums = new ArrayList<>();//已盘点的机柜二维码信息列表
            SparseArray<ArrayList> deviceNums = new SparseArray<>();//已盘点的设备二维码信息列表
            for(PandianResultData item:mDatas){
                int index = -1;
                ArrayList deviceNumsInSpecailCupboard;//指定机柜下的资产信息列表
                if((index=cupboardNums.indexOf(item.getCupboardNum()))<0){
                    index=cupboardNums.size();
                    cupboardNums.add(item.getBuildNum());
                    deviceNumsInSpecailCupboard = new ArrayList();
                }else{
                    deviceNumsInSpecailCupboard = deviceNums.get(index);
                }
                deviceNumsInSpecailCupboard.add(item.getDeviceNum());
                deviceNums.put(index,deviceNumsInSpecailCupboard);
                if(mPanListAdapter==null){
                    mPanListAdapter = new PandianAdapter(mActivity,cupboardNums,deviceNums);
                    mPanListView.setAdapter(mPanListAdapter);
                }else{
                    mPanListAdapter.setDatas(cupboardNums,deviceNums);
                }
            }
        }else{
            showList(false);
        }
    }

    @Override
    protected void showList(boolean isShowList){
        super.showList(isShowList);
        if(isShowList){//如果没有数据，就表示扫描
            mNoDataOperationButton.setText("扫描机柜");
            mNoDataOperationButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.public_noData_downLoad://扫描机柜
                break;
        }
    }
}
