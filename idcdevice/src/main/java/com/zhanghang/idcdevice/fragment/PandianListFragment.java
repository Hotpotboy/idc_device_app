package com.zhanghang.idcdevice.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Intents;
import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.EditDialog;
import com.zhanghang.idcdevice.PublicDialog;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.adapter.PandianAdapter;
import com.zhanghang.idcdevice.db.PandianResultTable;
import com.zhanghang.idcdevice.mode.pandian.PandianResultData;
import com.zhanghang.self.base.BaseFragmentActivity;
import com.zhanghang.self.utils.PopupWindowUtils;
import com.zhanghang.self.utils.camera.CameraUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by hangzhang209526 on 2016/6/2.
 * 盘点任务列表
 */
public class PandianListFragment extends BaseListFragment<PandianResultData> implements View.OnClickListener, ExpandableListView.OnGroupClickListener,ExpandableListView.OnGroupExpandListener,PandianAdapter.OperationListener {
    /**是否添加设备*/
    private boolean isAddDevice = false;
    /**
     * 机房二维码
     */
    private String mHouseCode;
    /**
     * 机房名字
     */
    private String mHouseName;
    /**
     * 列表视图
     */
    private ExpandableListView mPanListView;
    /**
     * 即将展开的机柜的二维码
     */
    private String mExpandCupboardCode = null;
    /**
     * 盘点任务适配器
     */
    private PandianAdapter mPanListAdapter;
    private TextView mTitileLeft;
    private TextView mTitileCenter;
    private TextView mNoDataTip;
    private PopupWindowUtils mNetLoadingWindow;
    private PublicDialog mDialog;
    private TextView mTitileRight;
    /**当前展开的机柜*/
    private int mExpandItemIndex = -1;
    /**输入弹出框*/
    private EditDialog mEditDialog;

    @Override
    protected void initDataFromArguments(Bundle arguments) {
        mDatas = (ArrayList<PandianResultData>) arguments.get(Const.INTENT_KEY_PANDIAN_TASK_DATA_LIST);
        mHouseName = arguments.getString(Const.INTENT_KEY_HOUSE_NAME, "");
        mHouseCode = arguments.getString(Const.INTENT_KEY_HOUSE_CODE, "");
    }

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_expend_list;
    }

    @Override
    protected void initView() {
        super.initView();
        mPanListView = (ExpandableListView) mListView;
        mNoDataTip = (TextView) findViewById(R.id.public_noData_tip);
        //标题
        mTitileLeft = (TextView) findViewById(R.id.fragment_title_left);
        mTitileCenter = (TextView) findViewById(R.id.fragment_title_center);
        mTitileRight = (TextView) findViewById(R.id.fragment_title_right);
        //对话框
        mNetLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, mActivity);
       //确认框
        mDialog = new PublicDialog(mActivity);
    }

    @Override
    protected void initData() {
        super.initData();
        mTitileCenter.setText("机房资产盘点");
        mTitileLeft.setText("返回");
        mTitileLeft.setBackgroundColor(Color.TRANSPARENT);
        mTitileLeft.setOnClickListener(this);
        mTitileRight.setText("新增机柜");
        mTitileRight.setVisibility(View.VISIBLE);
        mTitileRight.setOnClickListener(this);
    }

    @Override
    void loadData() {
        ArrayList<String> cupboardNums = new ArrayList<>();//已盘点的机柜二维码信息列表
        SparseArray<ArrayList> deviceNums = new SparseArray<>();//已盘点的设备二维码信息列表
        if (mPanListAdapter == null) {
            mPanListAdapter = new PandianAdapter(mActivity, cupboardNums, deviceNums,mHouseCode);
            mPanListView.setAdapter(mPanListAdapter);
            mPanListView.setOnGroupClickListener(this);
            mPanListView.setOnGroupExpandListener(this);
            mPanListAdapter.setOperationListener(this);
        }
        if (mDatas != null && mDatas.size() > 0) {
            showList(true);
            for (PandianResultData item : mDatas) {
                int index = -1;
                ArrayList deviceNumsInSpecailCupboard;//指定机柜下的资产信息列表
                if ((index = cupboardNums.indexOf(item.getCupboardNum())) < 0) {
                    index = cupboardNums.size();
                    cupboardNums.add(item.getCupboardNum());
                    deviceNumsInSpecailCupboard = new ArrayList();
                } else {
                    deviceNumsInSpecailCupboard = deviceNums.get(index);
                }
                if (!Const.isNullForDBData(item.getDeviceNum())) {//如果设备二维码不为空
                    deviceNumsInSpecailCupboard.add(item.getDeviceNum());
                }
                deviceNums.put(index, deviceNumsInSpecailCupboard);
            }
            mPanListAdapter.setDatas(cupboardNums, deviceNums);
        } else {
            mDatas = new ArrayList<>();
            showList(false);
        }
    }

    @Override
    protected void showList(boolean isShowList) {
        super.showList(isShowList);
        if (!isShowList) {//如果没有数据，就表示扫描
            mNoDataOperationButton.setText("开始扫描机柜");
            mNoDataTip.setText("机房【" + mHouseName + "】,还没有盘点任何数据!");
            mNoDataOperationButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_title_left://返回
                mActivity.finish();
                break;
            case R.id.fragment_title_right://标题栏右内容，新增机柜
                if(mExpandItemIndex>=0) {
                    mPanListView.collapseGroup(mExpandItemIndex);//关闭展开项
                    collapseGroup();
                }
            case R.id.public_noData_downLoad://扫描机柜
                CameraUtils.scannerQRCode((BaseFragmentActivity) mActivity, this);
                break;
        }
    }

    /**
     * 关闭组后的调用方法
     */
    private void collapseGroup(){
        mPanListAdapter.notifyDataSetChanged();
        cleanExpandItem();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == mActivity.RESULT_OK) {
            switch (requestCode) {
                case CameraUtils.SCANNER_QR_CODE_REQUEST_CODE://二维码扫描页面
                    final String result = intent.getStringExtra(Intents.Scan.RESULT);
                    mNetLoadingWindow.showAtLocation();
                    ((TextView) mNetLoadingWindow.getViewById(R.id.net_loading_tip)).setText("解析二维码成功，正在查询符合条件的任务......");
                    AsyncTask task = null;
                    if(mExpandItemIndex==-1){
                        task = new AddCupboardTask();//新增机柜
                    }else if(isAddDevice){//新增设备
                        task = new AddDeviceTask();
                        isAddDevice = false;
                    }else{
                        task = new OpenCupboardTask();//打开机柜
                    }
                    ((DeviceApplication) DeviceApplication.getInstance()).resolveScannerResult(result, task);//解析结果
                    break;
            }
        }
    }

    /**
     * 添加设备二维码，保存到数据库并重新加载
     *
     * @param deviceCode 设备二维码
     */
    private void addDeviceCode(String deviceCode) {
        int deviceNum = mPanListAdapter.getChildrenCount(mExpandItemIndex);
        PandianResultData resultData = null;
        if(deviceNum==0){
             for(PandianResultData item:mDatas){
                 if(TextUtils.equals(item.getCupboardNum(),mExpandCupboardCode)){
                     resultData = item;
                     break;
                 }
             }
        }else{
            resultData = new PandianResultData();
            resultData.setId(PandianResultTable.getId());
            resultData.setCupboardNum(mExpandCupboardCode);
            resultData.setBuildNum(mHouseCode);
        }
        resultData.setDeviceNum(deviceCode);
        resultData.setTime(System.currentTimeMillis());
        try {
            if(deviceNum!=0) {
                PandianResultTable.getPandianTableInstance().insertData(resultData);
            }else{
                PandianResultTable.getPandianTableInstance().updateData(resultData,null,null);
            }
        } catch (Exception e) {
            Toast.makeText(mActivity, "添加机柜二维码信息不正确!", Toast.LENGTH_LONG).show();
            return;
        }
        //更新视图
        mPanListAdapter.addChildData(mExpandItemIndex,deviceCode);
        mPanListAdapter.notifyDataSetInvalidated();
        if(deviceNum!=0){
            mDatas.add(resultData);
        }
    }

    /**
     * 添加机柜二维码，保存到数据库并更新视图
     *
     * @param cupBoardCode 机柜二维码
     */
    private void addCupBoardCode(String cupBoardCode) {
        PandianResultData resultData = new PandianResultData();
        resultData.setCupboardNum(cupBoardCode);
        resultData.setBuildNum(mHouseCode);
        resultData.setId(PandianResultTable.getId());
        resultData.setTime(System.currentTimeMillis());
        try {
            PandianResultTable.getPandianTableInstance().insertData(resultData);
        } catch (Exception e) {
            Toast.makeText(mActivity, "添加机柜二维码信息不正确!", Toast.LENGTH_LONG).show();
            return;
        }
        //更新视图
        mPanListAdapter.addParentData(cupBoardCode);
        mPanListAdapter.notifyDataSetInvalidated();
        mDatas.add(resultData);//保存，方便下一步添加此机柜的设备
        showList(true);
    }

    /**
     * 机柜二维码是否正确
     *
     * @return null表示不正确
     */
    private String isCupBoardCode(String qrCode) {
        return qrCode;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, final int groupPosition, long id) {
        if (parent.isGroupExpanded(groupPosition)) {//如果是展开,则关闭（默认行为）
            collapseGroup();
            return false;
        } else {
            mDialog.setContent("要展开此机柜，需扫描机柜二维码，\n确认扫描?").showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mExpandItemIndex>=0&&mExpandItemIndex<mPanListAdapter.getGroupCount()) {
                        mPanListView.collapseGroup(mExpandItemIndex);
                    }
                    mExpandCupboardCode = mPanListAdapter.getGroup(groupPosition).toString();
                    mExpandItemIndex = groupPosition;
                    CameraUtils.scannerQRCode((BaseFragmentActivity)mActivity,PandianListFragment.this);
                    mDialog.dismiss();
                }
            }).show();
            return true;
        }
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        //判断当前机柜下是否已有设备
        int deviceNum = mPanListAdapter.getChildrenCount(groupPosition);
        if(deviceNum<=0){
            mDialog.setContent("此机柜还未添加，是否扫描添加一个设备?").showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    CameraUtils.scannerQRCode((BaseFragmentActivity)mActivity,PandianListFragment.this);
                    isAddDevice = true;
                }
            }).show();
        }
    }

    @Override
    public void operation(int operationCode,final Object ext) {
        switch (operationCode){
            case PandianAdapter.OPERATION_CODE_ADD_DEVICE://添加设备
                CameraUtils.scannerQRCode((BaseFragmentActivity) mActivity, PandianListFragment.this);
                isAddDevice = true;
                break;
            case PandianAdapter.OPERATION_CODE_DELETE_DEVICE:
                if(ext!=null&&ext instanceof PandianResultData) {//同步
                    PandianResultData needDeleted = null;
                    for (PandianResultData item : mDatas) {
                        if (item.getId() == ((PandianResultData) ext).getId()) {
                            needDeleted = item;
                            break;
                        }
                    }
                    if (needDeleted != null) mDatas.remove(needDeleted);
                }
                break;
            case PandianAdapter.OPERATION_CODE_DELETE_CUPBOARD://删除机柜
                mDialog.setContent("此操作会清空此机柜下所有的设备,\n是否删除此机柜?").showCancelButton().showSureButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteCupBoard((int)ext);
                        mDialog.dismiss();
                    }
                }).show();
                break;
        }
    }

    /***
     * 删除机柜
     * @param ext
     */
    private void deleteCupBoard(final int ext) {
        mNetLoadingWindow.showAtLocation();
        ((TextView) mNetLoadingWindow.getViewById(R.id.net_loading_tip)).setText("正在删除机柜，请稍后......");
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String selection = PandianResultTable.getComlueInfos(PandianResultData.class).get(0).getName();
                    selection += "=? and ";
                    selection += PandianResultTable.getComlueInfos(PandianResultData.class).get(1).getName();
                    selection += "=?";
                    String[] args = new String[2];
                    args[0] = mHouseCode;
                    args[1] = (String) mPanListAdapter.getGroup(ext);
                    PandianResultTable.getPandianTableInstance().deleteData(selection, args);
                    return null;
                }catch (Exception e){
                    return e.toString();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                mNetLoadingWindow.getPopupWindow().dismiss();
                if (result!=null) {
                   Toast.makeText(mActivity,"删除失败【"+result+"】",Toast.LENGTH_LONG).show();
                }else{
                    String cupBoard = (String) mPanListAdapter.getGroup(ext);
                    Iterator<PandianResultData> iterator =  mDatas.iterator();
                    while(iterator.hasNext()){
                        PandianResultData data = iterator.next();
                        if(TextUtils.equals(mHouseCode,data.getBuildNum())
                                &&TextUtils.equals(cupBoard,data.getCupboardNum())){
                            iterator.remove();
                        }
                    }
                    mPanListAdapter.removeParentData(ext);
                    Toast.makeText(mActivity,"删除成功!",Toast.LENGTH_LONG).show();
                    if(mExpandItemIndex==ext){//如果删除的就是当前展开的机柜，则无效化相关展开的属性
                        cleanExpandItem();
                    }
                    if(mDatas.size()==0){
                        showList(false);
                    }
                }
            }
        }.execute();

    }

    /**
     * 效化相关展开的属性值
     */
    private void cleanExpandItem(){
        mExpandItemIndex = -1;
        mExpandCupboardCode = null;
    }

    /**
     * 添加机柜任务
     */
    private class AddCupboardTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return params[0];
        }

        @Override
        public void onPostExecute(String data) {
            mNetLoadingWindow.getPopupWindow().dismiss();
            mDialog.dismiss();
            String tmp = "";
            if (TextUtils.isEmpty((tmp = isCupBoardCode(data)))) {
                Toast.makeText(mActivity, "机柜的二维码不正确!", Toast.LENGTH_LONG).show();
                if(mEditDialog==null){
                    mEditDialog = new EditDialog(mActivity);
                }
                mEditDialog.dismiss();
                mEditDialog.showCancelButton().showSureButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEditDialog.dismiss();
                        String code = mEditDialog.getEditValue();
                        if(TextUtils.isEmpty(code)){
                            Toast.makeText(mActivity, "填写的机柜编码为空!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //添加机柜
                        addCupBoardCode(code);
                    }
                }).show();
                return;
            }
            if (mPanListAdapter.isInParent(tmp)) {
                Toast.makeText(mActivity, "该机柜已添加，不能重复添加!", Toast.LENGTH_LONG).show();
                return;
            }
            final String cupboardCode = tmp;
            mDialog.setContent("机柜二维码扫描成功,是否添加此机柜?").showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCupBoardCode(cupboardCode);
                    mDialog.dismiss();
                }
            }).show();
        }
    }

    /**
     * 打开机柜任务
     */
    private class OpenCupboardTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return TextUtils.equals(params[0],mExpandCupboardCode);
        }

        @Override
        public void onPostExecute(Boolean isScanner) {
            mNetLoadingWindow.getPopupWindow().dismiss();
            if(isScanner) {//扫描的结果匹配机柜二维码
                mPanListView.expandGroup(mExpandItemIndex);
            }else{
                Toast.makeText(mActivity, "机柜的二维码与扫描结果不匹配，请重新扫描!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 添加设备任务
     */
    private class AddDeviceTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return params[0];
        }

        @Override
        public void onPostExecute(String data) {
            mNetLoadingWindow.getPopupWindow().dismiss();
            if (mPanListAdapter.isInSpecailParent(mExpandItemIndex, data)) {
                Toast.makeText(mActivity, "当前机柜已添加此设备，不能重复添加!", Toast.LENGTH_LONG).show();
                return;
            }
            mDialog.dismiss();
            final String deviceCode = data;
            mDialog.setContent("设备二维码扫描成功,是否添加此设备?").showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addDeviceCode(deviceCode);
                    mDialog.dismiss();
                }
            }).show();
        }
    }
}