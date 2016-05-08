package com.zhanghang.idcdevice.fragment;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.EditDeviceDialog;
import com.zhanghang.idcdevice.PublicDialog;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.mode.DeviceData;
import com.zhanghang.self.base.BaseFragment;

import java.util.List;

/**
 * Created by Administrator on 2016-04-11.
 */
public class DeviceDetailFragment extends BaseFragment implements View.OnClickListener {
    /**
     * 设备编号
     */
    private TextView mDeviceNumView;
    /**
     * 设备名称
     */
    private TextView mDeviceNameView;
    /**
     * 设备类型
     */
    private TextView mDeviceTypeView;
    /**
     * 资产编号
     */
    private TextView mAssetNumView;
    /**
     * 资产序列号
     */
    private TextView mAssetSerialNumView;
    /**
     * 资产分类1
     */
    private TextView mAssetType1View;
    /**
     * 资产分类2
     */
    private TextView mAssetType2View;
    /**
     * 资产分类3
     */
    private TextView mAssetType3View;
    /**
     * 实物资产编号
     */
    private TextView mEntityAssetNumView;
    /**
     * 设备位置
     */
    private TextView mDeviceLocationView;
    /**
     * 保存按钮
     */
    private Button mSaveButton;
    /**
     * 删除按钮
     */
    private Button mDeleteButton;
    /**
     * 设备数据
     */
    private DeviceData mData;
    /**
     * 对比值
     */
    private DeviceData mStudioData;
    /**
     * 编辑对话框
     */
    private EditDeviceDialog mEditDialog;
    /**提示对话框*/
    private PublicDialog mTipDialog;
    /**是否修改过*/
    private SparseBooleanArray  mChanged;
    /**是否新增*/
    private boolean isAdd;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_device_detail;
    }

    @Override
    protected void initView() {
        mDeviceNumView = (TextView) findViewById(R.id.fragment_device_deviceNum);
        mDeviceNameView = (TextView) findViewById(R.id.fragment_device_deviceName);
        mDeviceTypeView = (TextView) findViewById(R.id.fragment_device_deviceType);
        mAssetNumView = (TextView) findViewById(R.id.fragment_device_assetNum);
        mAssetSerialNumView = (TextView) findViewById(R.id.fragment_device_assetSerialNum);
        mEntityAssetNumView = (TextView) findViewById(R.id.fragment_device_entityAssetNum);
        mAssetType1View = (TextView) findViewById(R.id.fragment_device_assetType1);
        mAssetType2View = (TextView) findViewById(R.id.fragment_device_assetType2);
        mAssetType3View = (TextView) findViewById(R.id.fragment_device_assetType3);
        mDeviceLocationView = (TextView) findViewById(R.id.fragment_device_deviceLocation);
        mSaveButton = (Button) findViewById(R.id.fragment_device_saveButton);
        mDeleteButton = (Button) findViewById(R.id.fragment_device_deleteButton);
        //编辑对话框
        mEditDialog = new EditDeviceDialog(mActivity);
        //设置监听器
//        mDeviceNumView.setOnClickListener(this);
//        mDeviceNameView.setOnClickListener(this);
//        mDeviceTypeView.setOnClickListener(this);
//        mAssetNumView.setOnClickListener(this);
//        mAssetSerialNumView.setOnClickListener(this);
//        mEntityAssetNumView.setOnClickListener(this);
//        mAssetType1View.setOnClickListener(this);
//        mAssetType2View.setOnClickListener(this);
//        mAssetType3View.setOnClickListener(this);
        mDeviceLocationView.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
    }

    /**
     * 添加数据
     */
    public void addData(){
        setData(null);
    }

    public void setData(DeviceData data) {
        mData = data;
        if(mData!=null) {//查看数据
            isAdd = false;
            mDeleteButton.setVisibility(View.GONE);
        }else{//新增数据
            isAdd = true;
            mData = new DeviceData();
            mDeleteButton.setVisibility(View.GONE);
        }
        try {
            mStudioData = (DeviceData) mData.clone();
            init();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        mChanged = new SparseBooleanArray();
        mDeviceNumView.setText(mData.getDeviceNum());
        mDeviceNameView.setText(mData.getDeviceName());
        mDeviceTypeView.setText(mData.getDeviceModel());
        mAssetNumView.setText(mData.getAssetNum());
        mAssetSerialNumView.setText(mData.getAssetSerialNum());
        mEntityAssetNumView.setText(mData.getEntityAssetNum());
        mAssetType1View.setText(mData.getAssetType1());
        mAssetType2View.setText(mData.getAssetType2());
        mAssetType3View.setText(mData.getAssetType3());
    }

    @Override
    public void onClick(final View v) {
        if (mEditDialog.isShowing()) {//确保对话框消失
            mEditDialog.dismiss();
        }
        switch (v.getId()) {
//            case R.id.fragment_device_deviceNum:
//            case R.id.fragment_device_deviceName:
//            case R.id.fragment_device_deviceType:
//            case R.id.fragment_device_assetNum:
//            case R.id.fragment_device_assetSerialNum:
//            case R.id.fragment_device_entityAssetNum:
//            case R.id.fragment_device_assetType1:
//            case R.id.fragment_device_assetType2:
//            case R.id.fragment_device_assetType3:
            case R.id.fragment_device_deviceLocation:
                if (v instanceof TextView) {
                    String content = "";
                    if(v.getId()==R.id.fragment_device_deviceLocation){
                        content = getResources().getString(R.string.she_bei_wei_zhi_s);
                        content = String.format(content,mData.getCity(),mData.getIdcRoom(),mData.getCabinet(),mData.getPosition());
                    }else {
                        content = (String) ((TextView) v).getText();
                    }
                    mEditDialog.setContent(content).showCancelButton(View.GONE,null).showSureButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            String newValue = mEditDialog.getEditValue();
//                            dealDialogCallBack((TextView) v, newValue);
                            mEditDialog.dismiss();
                        }
                    }).show();
                }
                break;
            case R.id.fragment_device_saveButton://保存
                if(!isChange()&&!isAdd){
                    if(mTipDialog==null){
                        mTipDialog = new PublicDialog(mActivity);
                    }
                    mTipDialog.dismiss();
                    mTipDialog.setContent("没有任何修改过……").showCancelButton(View.GONE,null).showSureButton(null).show();
                    break;
                }
                if(!isAdd) {
                    deleteOrEditOrAddOperation(Const.DataOperation.UPDATE);
                }else{
                    deleteOrEditOrAddOperation(Const.DataOperation.ADD);
                }
                break;
            case R.id.fragment_device_deleteButton://删除
                deleteOrEditOrAddOperation(Const.DataOperation.DELETE);
                break;
        }
    }

    /**
     * 处理编辑对话框的确定按钮点击事件
     *
     * @param view      原始值来源的视图
     * @param editValue 修改的值
     */
    private void dealDialogCallBack(TextView view, String editValue) {
        CharSequence oldValue = view.getText();
        boolean isSame = TextUtils.equals(oldValue, editValue);
        view.setText(editValue);
        switch (view.getId()) {
            case R.id.fragment_device_deviceNum:
                if(!isSame){//如果不相同
                    mStudioData.setDeviceNum(editValue);
                }else{
                    String value = new String(mData.getDeviceNum());
                    mStudioData.setDeviceNum(value);
                }
                mChanged.put(0,!isSame);
                break;
            case R.id.fragment_device_deviceName:
                if(!isSame){//如果不相同
                    mStudioData.setDeviceName(editValue);
                }else{
                    String value = new String(mData.getDeviceName());
                    mStudioData.setDeviceName(value);
                }
                mChanged.put(1,!isSame);
                break;
            case R.id.fragment_device_deviceType:
                if(!isSame){//如果不相同
                    mStudioData.setDeviceModel(editValue);
                }else{
                    String value = new String(mData.getDeviceModel());
                    mStudioData.setDeviceModel(value);
                }
                mChanged.put(2,!isSame);
                break;
            case R.id.fragment_device_assetNum:
                if(!isSame){//如果不相同
                    mStudioData.setAssetNum(editValue);
                }else{
                    String value = new String(mData.getAssetNum());
                    mStudioData.setAssetNum(value);
                }
                mChanged.put(3,!isSame);
                break;
            case R.id.fragment_device_assetSerialNum:
                if(!isSame){//如果不相同
                    mStudioData.setAssetSerialNum(editValue);
                }else{
                    String value = new String(mData.getAssetSerialNum());
                    mStudioData.setAssetSerialNum(value);
                }
                mChanged.put(4,!isSame);
                break;
            case R.id.fragment_device_entityAssetNum:
                if(!isSame){//如果不相同
                    mStudioData.setEntityAssetNum(editValue);
                }else{
                    String value = new String(mData.getEntityAssetNum());
                    mStudioData.setEntityAssetNum(value);
                }
                mChanged.put(5,!isSame);
                break;
            case R.id.fragment_device_assetType1:
                if(!isSame){//如果不相同
                    mStudioData.setAssetType1(editValue);
                }else{
                    String value = new String(mData.getAssetType1());
                    mStudioData.setAssetType1(value);
                }
                mChanged.put(6,!isSame);
                break;
            case R.id.fragment_device_assetType2:
                if(!isSame){//如果不相同
                    mStudioData.setAssetType2(editValue);
                }else{
                    String value = new String(mData.getAssetType2());
                    mStudioData.setAssetType2(value);
                }
                mChanged.put(7,!isSame);
                break;
            case R.id.fragment_device_assetType3:
                if(!isSame){//如果不相同
                    mStudioData.setAssetType3(editValue);
                }else{
                    String value = new String(mData.getAssetType3());
                    mStudioData.setAssetType3(value);
                }
                mChanged.put(8,!isSame);
                break;
            case R.id.fragment_device_deviceLocation:
                if(!isSame){//如果不相同
                    String[] valuse = {"","","",""};
                    if(!TextUtils.isEmpty(editValue)) {
                        String[] tmp = editValue.split("\\/");
                        for(int i=0;i<tmp.length;i++){
                            valuse[i] = tmp[i];
                        }
                    }
                    mStudioData.setCity(valuse[0]);
                    mStudioData.setIdcRoom(valuse[1]);
                    mStudioData.setCabinet(valuse[2]);
                    mStudioData.setPosition(valuse[3]);
                }else{
                    String city = new String(mData.getCity());
                    mStudioData.setCity(city);
                    String room = new String(mData.getIdcRoom());
                    mStudioData.setIdcRoom(room);
                    String cabinet = new String(mData.getCabinet());
                    mStudioData.setCity(cabinet);
                    String position = new String(mData.getPosition());
                    mStudioData.setCity(position);
                }
                mChanged.put(9,!isSame);
                break;
        }
    }

    /**
     * 判断是否用户进行过修改
     * @return false表示从未修改过
     */
    private boolean isChange(){
        for(int i=0;i<mChanged.size();i++){
            boolean item = mChanged.valueAt(i);
            if(item){
                return true;
            }
        }
        return false;
    }

    /**
     * 删除或者编辑或者新增数据的操作方法
     * @param type
     */
    private void deleteOrEditOrAddOperation(final Const.DataOperation type){
        if(mTipDialog==null){
            mTipDialog = new PublicDialog(mActivity);
        }
        mTipDialog.dismiss();
        String tip = type==Const.DataOperation.UPDATE?"保存":(type==Const.DataOperation.DELETE?"删除":"新增");
        mTipDialog.setContent("确定"+tip+"此设备?").showCancelButton()
                .showSureButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Fragment> fragmentList = getFragmentManager().getFragments();
                        for(Fragment item:fragmentList){
                            if(item instanceof DeviceListFragment){
                                ((DeviceListFragment)item).updateData(mStudioData,type);
                                break;
                            }
                        }
                        mTipDialog.dismiss();
                    }
                }).show();
    }
}