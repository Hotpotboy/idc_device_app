package com.zhanghang.idcdevice.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mining.app.zxing.decoding.Intents;
import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.PublicDialog;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.adapter.DeviceAdapter;
import com.zhanghang.idcdevice.db.PandianResultTable;
import com.zhanghang.idcdevice.interfaces.PandianOperationListener;
import com.zhanghang.idcdevice.mode.DBdata;
import com.zhanghang.idcdevice.mode.DeviceData;
import com.zhanghang.idcdevice.mode.pandian.PandianResultData;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.base.BaseFragmentActivity;
import com.zhanghang.self.utils.PopupWindowUtils;
import com.zhanghang.self.utils.PreferenceUtil;
import com.zhanghang.self.utils.camera.CameraUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2016-04-11.
 */
public class DeviceDetailFragment extends BaseFragment implements View.OnClickListener, PandianOperationListener, AdapterView.OnItemClickListener {
    /**
     * 设备位置
     */
    private TextView mDeviceLocationView;
    /**
     * 标题栏左侧
     */
    private TextView mTitileLeft;

    /***
     * 机柜扫描编码
     */
    private String mCabinetNum;

    /**
     * 完成扫描按钮
     */
    private TextView mFinishTextView;

    /**
     * 开始扫描按钮
     */
    private TextView mScannerTextView;

    /***
     * 已扫描的设备
     */
    private ListView mDeviceListView;

    /**
     * 已扫描的设备列表
     */
    private ArrayList<String> mDevices = new ArrayList<>();

    private DeviceAdapter mDeviceAdapter;
    private PopupWindowUtils mNetLoadingWindow;

    private AddDeviceFragment mAddDeviceFragment;
    private View mEmptyListView;
    private Handler mHandler = new Handler();
    /**
     * 位置详情
     */
    private String mLocation = "";
    private PublicDialog mDialog;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_devices;
    }

    protected void initDataFromArguments(Bundle arguments) {
        mCabinetNum = arguments.getString(Const.INTENT_KEY_CABINET_NUM);
    }

    @Override
    protected void initView() {
        mTitileLeft = (TextView) findViewById(R.id.fragment_devices_title_left);
        mFinishTextView = (TextView) findViewById(R.id.fragment_devices_finish);
        mFinishTextView.setOnClickListener(this);
        mScannerTextView = (TextView) findViewById(R.id.fragment_devices_addDevice);
        mScannerTextView.setOnClickListener(this);
        mDeviceLocationView = (TextView) findViewById(R.id.fragment_device_deviceLocation);
        mDeviceListView = (ListView) findViewById(R.id.fragment_devices_list);
        mDeviceLocationView.setOnClickListener(this);
        mTitileLeft.setText("返回");
        mTitileLeft.setBackgroundColor(Color.TRANSPARENT);
        mTitileLeft.setOnClickListener(this);
        mDeviceListView.setOnItemClickListener(this);
        mDeviceListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        mDeviceListView.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }
        });
        //确认框
        mDialog = new PublicDialog(mActivity);
        //对话框
        mNetLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, mActivity);
        initEmptyListView();
    }

    private void initEmptyListView() {
        mEmptyListView = LayoutInflater.from(mActivity).inflate(R.layout.public_no_data, null);
        int index = ((ViewGroup) mDeviceListView.getParent()).indexOfChild(mDeviceListView);
        ((ViewGroup) mDeviceListView.getParent()).addView(mEmptyListView, index < 1 ? 0 : index - 1);
        mDeviceListView.setEmptyView(mEmptyListView);
        TextView downButton = (TextView) mEmptyListView.findViewById(R.id.public_noData_downLoad);
        downButton.setText("扫描设备");
        downButton.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        try {
            String select = PandianResultTable.getPandianTableInstance().getComlueInfos()[1].getName() + " = ?";
            String[] args = new String[1];
            args[0] = mCabinetNum;
            ArrayList<PandianResultData> pandianResultDatas = PandianResultTable.getPandianTableInstance().selectDatas(select, args, null, null, null, PandianResultData.class);
            if (pandianResultDatas != null && !pandianResultDatas.isEmpty()) {
                mDevices.clear();
                for (PandianResultData item : pandianResultDatas) {
                    if (!Const.isNullForDBData(item.getDeviceNum())) {
                        mDevices.add(item.getDeviceNum());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "获取当前机柜下已盘点的设备失败![" + e.toString() + "]", Toast.LENGTH_LONG).show();
        }
        if (mDeviceAdapter == null) {
            mDeviceAdapter = new DeviceAdapter(mActivity, mDevices);
            mDeviceAdapter.setPandianOperationListener(this);
        } else {
            mDeviceAdapter.setDatas(mDevices);
        }
        if (mDeviceListView.getAdapter() == null) {
            mDeviceListView.setAdapter(mDeviceAdapter);
        }
        int index = mDeviceAdapter.getSelected();
        if ((index < 0 || index >= mDevices.size()) && mDevices.size() > 0) {
            mDeviceAdapter.setSelected(0);
            String selectedNum = mDevices.get(0);
            fillDeviceData(this, selectedNum);
        } else if (mDevices.size() > 0) {
            String selectedNum = mDevices.get(index);
            fillDeviceData(this, selectedNum);
        }
    }

    public static void fillDeviceData(BaseFragment baseFragment, String num) {
        TextView mDeviceNumView = (TextView) baseFragment.findViewById(R.id.fragment_device_deviceNum);
        TextView mDeviceNameView = (TextView) baseFragment.findViewById(R.id.fragment_device_deviceName);
        TextView mDeviceTypeView = (TextView) baseFragment.findViewById(R.id.fragment_device_deviceType);
        TextView mAssetNumView = (TextView) baseFragment.findViewById(R.id.fragment_device_assetNum);
        TextView mAssetSerialNumView = (TextView) baseFragment.findViewById(R.id.fragment_device_assetSerialNum);
        TextView mEntityAssetNumView = (TextView) baseFragment.findViewById(R.id.fragment_device_entityAssetNum);
        TextView mAssetType1View = (TextView) baseFragment.findViewById(R.id.fragment_device_assetType1);
        TextView mAssetType2View = (TextView) baseFragment.findViewById(R.id.fragment_device_assetType2);
        TextView mAssetType3View = (TextView) baseFragment.findViewById(R.id.fragment_device_assetType3);
        TextView mLocationView = (TextView) baseFragment.findViewById(R.id.fragment_device_deviceLocation);
        DeviceData data = DBdata.getDeviceDataFromCached(num, 0);
        String nullData = baseFragment.getActivity().getString(R.string.kong_shu_ju);
        if (mDeviceNumView != null)
            mDeviceNumView.setText(data == null ? nullData : (TextUtils.isEmpty(data.getDeviceNum()) ? nullData : data.getDeviceNum()));
        if (mDeviceNameView != null)
            mDeviceNameView.setText(data == null ? nullData : (TextUtils.isEmpty(data.getDeviceName()) ? nullData : data.getDeviceName()));
        if (mDeviceTypeView != null)
            mDeviceTypeView.setText(data == null ? nullData : (TextUtils.isEmpty(data.getDeviceModel()) ? nullData : data.getDeviceModel()));
        if (mAssetNumView != null)
            mAssetNumView.setText(data == null ? nullData : (TextUtils.isEmpty(data.getAssetNum()) ? nullData : data.getAssetNum()));
        if (mAssetSerialNumView != null)
            mAssetSerialNumView.setText(data == null ? nullData : (TextUtils.isEmpty(data.getAssetSerialNum()) ? nullData : data.getAssetSerialNum()));
        if (mEntityAssetNumView != null)
            mEntityAssetNumView.setText(data == null ? nullData : (TextUtils.isEmpty(data.getEntityAssetNum()) ? nullData : data.getEntityAssetNum()));
        if (mAssetType1View != null)
            mAssetType1View.setText(data == null ? nullData : (TextUtils.isEmpty(data.getAssetType1()) ? nullData : data.getAssetType1()));
        if (mAssetType2View != null)
            mAssetType2View.setText(data == null ? nullData : (TextUtils.isEmpty(data.getAssetType2()) ? nullData : data.getAssetType2()));
        if (mAssetType3View != null)
            mAssetType3View.setText(data == null ? nullData : (TextUtils.isEmpty(data.getAssetType3()) ? nullData : data.getAssetType3()));
        String content = baseFragment.getActivity().getResources().getString(R.string.she_bei_wei_zhi_s);
        if (data != null) {
            content = String.format(content,
                    TextUtils.isEmpty(data.getCity()) ? nullData : data.getCity(),
                    TextUtils.isEmpty(data.getCabinet()) ? nullData : data.getCabinet(),
                    TextUtils.isEmpty(data.getPosition()) ? nullData : data.getPosition());
        }else{
            content = nullData;
        }
        if(baseFragment instanceof  DeviceDetailFragment){
            ((DeviceDetailFragment)baseFragment).mLocation = content;
            mLocationView.setOnClickListener((DeviceDetailFragment)baseFragment);
        }else if(baseFragment instanceof AddDeviceFragment){
            ((AddDeviceFragment)baseFragment).mLocation = content;
            mLocationView.setOnClickListener((AddDeviceFragment)baseFragment);
        }
    }



    private void showLocation(String location){
        mDialog.dismiss();
        mDialog.setContent(location).showCancelButton(View.GONE,null).showSureButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        }).show();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.fragment_devices_title_left:
                mActivity.finish();
                break;
            case R.id.fragment_device_deviceLocation:
                showLocation(mLocation);
                break;
            case R.id.public_noData_downLoad:
            case R.id.fragment_devices_addDevice://扫描设备
                CameraUtils.scannerQRCode((BaseFragmentActivity) mActivity, this);
                break;
            case R.id.fragment_devices_finish://完成扫描
                mDialog.setContent("一旦完成此机柜的扫描，则不能再次进入此机柜!\n确定？").showCancelButton().showSureButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        Const.saveFinishedDevices(mActivity,mCabinetNum);
                    }
                }).show();
                break;
        }
    }

    @Override
    public void operation(int operationCode, Object ext) {
        if (operationCode == PandianOperationListener.OPERATION_CODE_DELETE_DEVICE) {
            String[] params = new String[2];
            params[0] = mCabinetNum;
            params[1] = (String) ext;
            new DeleteDeviceTask().execute(params);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String num = (String) mDeviceAdapter.getItem(position);
        fillDeviceData(this, num);
        mDeviceAdapter.setSelected(position);
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
                    AsyncTask task = new AddDeviceTask(false);
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
        if (mDevices.contains(deviceCode)) {
            return;
        }
        int deviceNum = mDevices.size();
        ArrayList<PandianResultData> resultDatas;
        try {
            String selecte = PandianResultTable.getPandianTableInstance().getComlueInfos()[1].getName() + "=?";
            String[] selecteArgs = new String[1];
            selecteArgs[0] = mCabinetNum;
            resultDatas = (ArrayList<PandianResultData>) PandianResultTable.getPandianTableInstance().selectDatas(selecte, selecteArgs, null, null, null, PandianResultData.class);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "从数据库中获取当前机柜信息失败！", Toast.LENGTH_LONG).show();
            return;
        }

        if (resultDatas == null
                || (resultDatas.size() != deviceNum && deviceNum > 0)
                || (resultDatas.size() != 1 && deviceNum == 0)) {
            Toast.makeText(mActivity, "严重错误!请重新安装app!", Toast.LENGTH_LONG).show();
            return;
        }

        PandianResultData resultData = null;
        if (deviceNum > 0) {
            long taskId = resultDatas.get(0).getTkId();
            resultData = new PandianResultData();
            resultData.setId(PandianResultTable.getId());
            resultData.setCupboardNum(mCabinetNum);
            resultData.setTkId(taskId);
        } else {
            resultData = resultDatas.get(0);
        }
        resultData.setDeviceNum(deviceCode);
        resultData.setTime(System.currentTimeMillis());
        try {
            if (deviceNum != 0) {
                PandianResultTable.getPandianTableInstance().insertData(resultData);
            } else {
                PandianResultTable.getPandianTableInstance().updateData(resultData, null, null);
            }
        } catch (Exception e) {
            Toast.makeText(mActivity, "添加设备二维码信息不正确!", Toast.LENGTH_LONG).show();
            return;
        }
        //更新视图
        mDevices.add(deviceCode);
        mDeviceAdapter.notifyDataSetChanged();
    }

    /**
     * 删除设备任务
     */
    private class DeleteDeviceTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String cupboardNumCol = PandianResultTable.getComlueInfos(PandianResultData.class).get(1).getName();
                String deviceNumCol = PandianResultTable.getComlueInfos(PandianResultData.class).get(2).getName();
                String selecte = cupboardNumCol + "=?";
                String[] selecteArgs = new String[1];
                selecteArgs[0] = params[0];
                ArrayList<PandianResultData> pandianResultDatas = PandianResultTable.getPandianTableInstance().selectDatas(selecte, selecteArgs, null, null, null, PandianResultData.class);
                PandianResultData pandianResultData = null;
                if (pandianResultDatas != null && pandianResultDatas.size() > 1) {//如果不是最后一个直接删除
                    for (PandianResultData item : pandianResultDatas) {
                        if (TextUtils.equals(item.getCupboardNum(), params[0])
                                && TextUtils.equals(item.getDeviceNum(), params[1])) {
                            pandianResultData = item;
                            break;
                        }
                    }
                    if (pandianResultData == null) throw new RuntimeException("数据库中没有此记录!");
                    selecte += " and " + deviceNumCol + "=?";
                    PandianResultTable.getPandianTableInstance().deleteData(selecte, params);
                } else if (pandianResultDatas != null && pandianResultDatas.size() == 1) {//如果是最后一个
                    pandianResultData = pandianResultDatas.get(0);
                    pandianResultData.setDeviceNum("");
                    PandianResultTable.getPandianTableInstance().updateData(pandianResultData, null, null);
                }
                return params[1];
            } catch (final Exception e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "删除失败!" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!TextUtils.isEmpty(result)) {
                int index = mDevices.indexOf(result);
                if (index >= 0 && index < mDevices.size()) {
                    if (mDevices.size()>1&&index == mDeviceAdapter.getSelected()) {
                        mDeviceAdapter.setSelected(0);
                        fillDeviceData(DeviceDetailFragment.this, mDevices.get(0));
                    }else if(mDevices.size()==1){//之声最后一个了
                        fillDeviceData(DeviceDetailFragment.this, null);
                    }
                    mDevices.remove(index);
                    mDeviceAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * 添加设备任务
     */
    private class AddDeviceTask extends AsyncTask<String, Void, String> {
        /**
         * 是否由{@link com.zhanghang.idcdevice.fragment.DeviceDetailFragment.AddDeviceFragment}调用
         */
        private boolean mIsInAddDeviceFragment;

        private AddDeviceTask(boolean isInAddDeviceFragment) {
            mIsInAddDeviceFragment = isInAddDeviceFragment;
        }

        @Override
        protected String doInBackground(String... params) {
            return params[0];
        }

        @Override
        public void onPostExecute(String data) {
            mNetLoadingWindow.getPopupWindow().dismiss();
            if (mDevices.contains(data)) {
                Toast.makeText(mActivity, "当前机柜已添加此设备，不能重复添加!", Toast.LENGTH_LONG).show();
                return;
            }
            if (mIsInAddDeviceFragment) {
                mAddDeviceFragment.dealScannerResult(data);
            } else {
                if (mAddDeviceFragment == null) mAddDeviceFragment = new AddDeviceFragment();
                mAddDeviceFragment.setAddingDeviceNum(data);
                if (mActivity instanceof BaseFragmentActivity) {
                    BaseFragmentActivity fragmentActivity = (BaseFragmentActivity) mActivity;
                    FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
                    transaction.replace(fragmentActivity.mResourceId, mAddDeviceFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        }
    }

    private class AddDeviceFragment extends BaseFragment implements View.OnClickListener {
        private static final int SHOW_TIME = 3;
        /**
         * 距离消失还有多少秒
         */
        private int mCurrentTime;
        /**
         * 关闭提示视图
         */
        private TextView mTipView;
        /**
         * 直接返回按钮
         */
        private TextView mReturnTextView;

        /**
         * 保存并返回按钮
         */
        private TextView mSavedAndReturnTextView;

        /**
         * 重新扫描按钮
         */
        private TextView mReScannerTextView;

        /**
         * 保存并继续按钮
         */
        private TextView mSavedAndContinueTextView;
        /**
         * 即将添加的设备的扫描码
         */
        private String mAddDeviceNum;
        public String mLocation;

        private Runnable mCheckDismiss = new Runnable() {
            @Override
            public void run() {
                if(mCurrentTime<=0) {
                    addDeviceCode(mAddDeviceNum);
                    CameraUtils.scannerQRCode((BaseFragmentActivity) mActivity, DeviceDetailFragment.this);
                    mActivity.finish();
                }else{
                    mCurrentTime--;
                    mTipView.setText(String.format(getString(R.string.ye_mian_ti_shi_s),mCurrentTime+""));
                    mHandler.postDelayed(mCheckDismiss, 1000);
                }
            }
        };

        private boolean setAddingDeviceNum(String num) {
            if (!TextUtils.equals(num, mAddDeviceNum)) {
                mAddDeviceNum = num;
                return true;
            }
            return false;
        }

        @Override
        protected int specifyRootLayoutId() {
            return R.layout.fragment_add_devices;
        }

        @Override
        protected void initView() {
            mReturnTextView = (TextView) findViewById(R.id.fragment_add_device_return);
            mReScannerTextView = (TextView) findViewById(R.id.fragment_add_device_rescanner);
            mSavedAndReturnTextView = (TextView) findViewById(R.id.fragment_add_device_return_and_save);
            mSavedAndContinueTextView = (TextView) findViewById(R.id.fragment_add_device_save_and_continue);
            mTipView = (TextView) findViewById(R.id.fragment_add_device_tip);
            mReturnTextView.setOnClickListener(this);
            mReScannerTextView.setOnClickListener(this);
            mSavedAndReturnTextView.setOnClickListener(this);
            mSavedAndContinueTextView.setOnClickListener(this);
        }

        @Override
        protected void initData() {
            fillDeviceData(this, mAddDeviceNum);
            ((TextView) findViewById(R.id.fragment_add_device_num)).setText(mAddDeviceNum);
            mCurrentTime = SHOW_TIME;
            mTipView.setText(String.format(getString(R.string.ye_mian_ti_shi_s),mCurrentTime+""));
            mHandler.postDelayed(mCheckDismiss,1000);
        }

        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.fragment_add_device_return://直接返回
                    addDeviceCode(mAddDeviceNum);
                    mActivity.finish();
                    CameraUtils.scannerQRCode((BaseFragmentActivity) mActivity, DeviceDetailFragment.this);
                    break;
                case R.id.fragment_add_device_rescanner://重新扫描
                    CameraUtils.scannerQRCode((BaseFragmentActivity) mActivity, this);
                    break;
                case R.id.fragment_add_device_return_and_save://返回并保存
                    addDeviceCode(mAddDeviceNum);
                    mActivity.finish();
                    break;
                case R.id.fragment_add_device_save_and_continue://保存并继续
                    addDeviceCode(mAddDeviceNum);
                    CameraUtils.scannerQRCode((BaseFragmentActivity) mActivity, this);
                    break;
                case R.id.fragment_device_deviceLocation:
                    showLocation(mLocation);
                    break;
            }
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
                        AsyncTask task = new AddDeviceTask(true);
                        ((DeviceApplication) DeviceApplication.getInstance()).resolveScannerResult(result, task);//解析结果
                        break;
                }
            }
        }

        private void dealScannerResult(String num) {
            if (setAddingDeviceNum(num)) {
                initData();
            }
        }

        private void setLocation(String location){
            mLocation = location;
        }

        @Override
        public void onDestroyView(){
            super.onDestroyView();
            mHandler.removeCallbacks(mCheckDismiss);
        }
    }
}