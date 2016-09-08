package com.zhanghang.idcdevice.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adbsocket.AdbSocketUtils;
import com.google.zxing.Intents;
import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.EditDialog;
import com.zhanghang.idcdevice.FragmentActivity;
import com.zhanghang.idcdevice.PublicDialog;
import com.zhanghang.idcdevice.R;
import com.zhanghang.idcdevice.adapter.CabinetAdapter;
import com.zhanghang.idcdevice.adbsocket.Request;
import com.zhanghang.idcdevice.db.DeviceTable;
import com.zhanghang.idcdevice.db.PandianResultTable;
import com.zhanghang.idcdevice.db.PatrolItemTable;
import com.zhanghang.idcdevice.db.TaskTable;
import com.zhanghang.idcdevice.interfaces.PandianOperationListener;
import com.zhanghang.idcdevice.mode.DBdata;
import com.zhanghang.idcdevice.mode.pandian.PandianResultData;
import com.zhanghang.self.base.BaseFragmentActivity;
import com.zhanghang.self.utils.PopupWindowUtils;
import com.zhanghang.self.utils.camera.CameraUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016-03-29.
 */
public class PandianTaskDetailFragment extends TaskDetailFragment implements PandianOperationListener {
    /**
     * 标题左边的返回按钮
     */
    private TextView mTitleLeft;

    /**
     * 添加机柜按钮
     */
    private TextView mAddCabinet;

    /**
     * 完成任务按钮
     */
    private TextView mFinishTask;

    /**
     * 机柜适配器
     */
    private CabinetAdapter mCabinetAdapter;

    /**
     * 当前任务下的机柜列表
     */
    private ArrayList<String> mCabinetNumList = new ArrayList<>();

    /***/
    private ListView mCabinetListView;
    private PublicDialog mDialog;
    /**
     * 打开机柜的扫描码
     */
    private String mOpenCabinetNum = "";
    private PopupWindowUtils mNetLoadingWindow;

    private View mEmptyListView;

    /**
     * 上传当前按钮
     */
    private TextView mUploadTaskView;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_pandian_task_detail;
    }

    @Override
    void initTitle() {
        mTitleLeft = (TextView) findViewById(R.id.fragment_pandian_task_detail_title_left);
        mAddCabinet = (TextView) findViewById(R.id.fragment_pandian_task_detail_addCabinet);
        mFinishTask = (TextView) findViewById(R.id.fragment_pandian_task_detail_finish);
        mUploadTaskView = (TextView) findViewById(R.id.fragment_pandian_task_detail_upload);
        mTitleLeft.setOnClickListener(this);
        mAddCabinet.setOnClickListener(this);
        mFinishTask.setOnClickListener(this);
        mUploadTaskView.setOnClickListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mCabinetListView = (ListView) findViewById(R.id.fragment_pandian_task_detail_cabinets);
        initEmptyListView();
        mCabinetListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        mCabinetListView.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }
        });
        //对话框
        mNetLoadingWindow = PopupWindowUtils.getInstance(R.layout.net_loading, mActivity);
        //确认框
        mDialog = new PublicDialog(mActivity);
    }

    private void initEmptyListView() {
        mEmptyListView = LayoutInflater.from(mActivity).inflate(R.layout.public_no_data, null);
        ((ViewGroup) mCabinetListView.getParent()).addView(mEmptyListView);
        mCabinetListView.setEmptyView(mEmptyListView);
        TextView downButton = (TextView) mEmptyListView.findViewById(R.id.public_noData_downLoad);
        downButton.setText("扫描机柜");
        downButton.setOnClickListener(this);
    }

    @Override
    void initTitleData() {

    }

    @Override
    void initPatrolInfos() {
        //盘点任务没有巡检项所以隐藏
        mPatrolInfosView.setVisibility(View.GONE);
        findViewById(R.id.fragment_task_detail_patrolsInfos_tip).setVisibility(View.GONE);
        findViewById(R.id.fragment_task_detail_split0).setVisibility(View.GONE);
        findViewById(R.id.fragment_task_detail_split1).setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        super.initData();
        String selection = PandianResultTable.getPandianTableInstance().getComlueInfos()[5].getName() + " = ?";
        String[] args = new String[1];
        args[0] = mData.getTaskId() + "";
        try {
            ArrayList<PandianResultData> resultDatas = PandianResultTable.getPandianTableInstance().selectDatas(selection, args, null, null, null, PandianResultData.class);
            if (resultDatas != null && resultDatas.size() > 0) {
                for (PandianResultData item : resultDatas) {
                    if (!mCabinetNumList.contains(item.getCupboardNum())) {
                        mCabinetNumList.add(item.getCupboardNum());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "查询当前任务下已盘点的机柜失败!", Toast.LENGTH_LONG).show();
        }
        if (mCabinetAdapter == null) {
            mCabinetAdapter = new CabinetAdapter(mActivity, mCabinetNumList);
            mCabinetAdapter.setPandianOperationListener(this);
            mCabinetListView.setAdapter(mCabinetAdapter);
        } else {
            mCabinetAdapter.setDatas(mCabinetNumList);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_pandian_task_detail_title_left://返回
                mActivity.finish();
                break;
            case R.id.fragment_pandian_task_detail_upload://上传当前盘点任务
                mNetLoadingWindow.showAtLocation();
                ((TextView) mNetLoadingWindow.getViewById(R.id.net_loading_tip)).setText("正在上传当前任务......");
                mDialog.setContent("一旦上传任务，则会删除当前盘点任务\n确定？").showCancelButton().showSureButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        new UploadTask().execute();
                    }
                }).show();
                break;
            case R.id.public_noData_downLoad:
            case R.id.fragment_pandian_task_detail_addCabinet://添加机柜
                CameraUtils.scannerQRCode((BaseFragmentActivity) mActivity, this);
                break;
            case R.id.fragment_pandian_task_detail_finish://完成任务
                mDialog.setContent("一旦完成此任务，则不能再打开！确定？").showCancelButton().showSureButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        new AsyncTask<Void, Void, Boolean>() {
                            @Override
                            protected Boolean doInBackground(Void... params) {
                                mData.setTaskState(Const.TASK_STATE_DEALED);
                                try {
                                    TaskTable.getTaskTableInstance().updateData(mData, null, null);
                                } catch (Exception e) {
                                    return false;
                                }
                                return true;
                            }

                            @Override
                            protected void onPostExecute(Boolean result) {
                                if (result) {
                                    DeviceApplication.invokeDataDownFinishedListener();
                                    mActivity.finish();
                                } else {
                                    Toast.makeText(mActivity, "结束任务失败!", Toast.LENGTH_LONG).show();
                                }
                            }
                        }.execute();
                    }
                }).show();
                break;
        }
    }

    @Override
    public void operation(int operationCode, final Object ext) {
        if (operationCode == PandianOperationListener.OPERATION_CODE_OPEN_CABINET) {//打开机柜
            mDialog.setContent("要进入此机柜，需扫描机柜二维码，\n确认扫描?").showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOpenCabinetNum = (String) ext;
                    CameraUtils.scannerQRCode((BaseFragmentActivity) mActivity, PandianTaskDetailFragment.this);
                    mDialog.dismiss();
                }
            }).show();
        } else if (operationCode == PandianOperationListener.OPERATION_CODE_DELETE_CABINET) {//删除机柜
            mDialog.setContent("此操作会清空此机柜下所有的设备,\n是否删除此机柜?").showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteCupBoard((String) ext);
                    mDialog.dismiss();
                }
            }).show();
        }
    }

    /**
     * 添加机柜二维码，保存到数据库并更新视图
     *
     * @param cupBoardCode 机柜二维码
     */
    private void addCupBoardCode(String cupBoardCode) {
        if (!TextUtils.isEmpty(cupBoardCode)) {
            try {
                //判断添加的机柜是否被其他盘点任务所盘点
                String select = PandianResultTable.getPandianTableInstance().getComlueInfos()[1].getName() + "=?";
                String[] args = {cupBoardCode};
                List<PandianResultData> pandianResultDatas = PandianResultTable.getPandianTableInstance().selectDatas(select, args, null, null, null, PandianResultData.class);
                if (pandianResultDatas != null && !pandianResultDatas.isEmpty()) {
                    long taskId = mData.getTaskId();
                    for (PandianResultData item : pandianResultDatas) {
                        if (taskId != item.getTkId()) {
                            mDialog.setContent("此机柜已被其他盘点任务所盘点，流程不能继续!").showCancelButton(View.GONE,null).showSureButton(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDialog.dismiss();
                                }
                            }).show();
                            return;
                        }
                    }
                }
                PandianResultData resultData = new PandianResultData();
                resultData.setCupboardNum(cupBoardCode);
                resultData.setId(PandianResultTable.getId());
                resultData.setTime(System.currentTimeMillis());
                resultData.setTkId(mData.getTaskId());
                PandianResultTable.getPandianTableInstance().insertData(resultData);
                //更新视图
                mCabinetNumList.add(cupBoardCode);
                mCabinetAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Toast.makeText(mActivity, "添加机柜至数据库失败!", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    /***
     * 删除机柜
     *
     * @param cabinetNum
     */
    private void deleteCupBoard(final String cabinetNum) {
        mNetLoadingWindow.showAtLocation();
        ((TextView) mNetLoadingWindow.getViewById(R.id.net_loading_tip)).setText("正在删除机柜，请稍后......");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String selection = PandianResultTable.getComlueInfos(PandianResultData.class).get(1).getName();
                    selection += "=?";
                    String[] args = new String[1];
                    args[0] = cabinetNum + "";
                    PandianResultTable.getPandianTableInstance().deleteData(selection, args);
                    return null;
                } catch (Exception e) {
                    return e.toString();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                mNetLoadingWindow.getPopupWindow().dismiss();
                if (result != null) {
                    Toast.makeText(mActivity, "删除失败【" + result + "】", Toast.LENGTH_LONG).show();
                } else {
                    mCabinetNumList.remove(cabinetNum);
                    mCabinetAdapter.notifyDataSetChanged();
                }
            }
        }.execute();

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
                    if (!TextUtils.isEmpty(mOpenCabinetNum)) {
                        //打开机柜
                        task = new OpenCupboardTask();//打开机柜
                    } else {
                        task = new AddCupboardTask();//新增机柜
                    }
                    ((DeviceApplication) DeviceApplication.getInstance()).resolveScannerResult(result, task);//解析结果
                    break;
            }
        }
    }

    /**
     * 打开机柜任务
     */
    private class OpenCupboardTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return TextUtils.equals(params[0], mOpenCabinetNum);
        }

        @Override
        public void onPostExecute(Boolean isScanner) {
            mNetLoadingWindow.getPopupWindow().dismiss();
            if (isScanner) {//扫描的结果匹配机柜二维码
                Intent intent = new Intent(mActivity, FragmentActivity.class);
                intent.putExtra(Const.INTENT_KEY_CABINET_NUM, mOpenCabinetNum);
                intent.putExtra(Const.INTENT_KEY_LOAD_FRAGMENT, FragmentActivity.DEVICE_DETAIL_FRAGMENT);
                mActivity.startActivity(intent);
            } else {
                Toast.makeText(mActivity, "机柜的二维码与扫描结果不匹配，请重新扫描!", Toast.LENGTH_LONG).show();
            }
            mOpenCabinetNum = "";
        }
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
        public void onPostExecute(final String data) {
            mNetLoadingWindow.getPopupWindow().dismiss();
            mDialog.dismiss();
            if (mCabinetNumList.contains(data)) {
                Toast.makeText(mActivity, "该机柜已添加，不能重复添加!", Toast.LENGTH_LONG).show();
                return;
            }
            mDialog.setContent("机柜二维码扫描成功,是否添加此机柜?").showCancelButton().showSureButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCupBoardCode(data);
                    mDialog.dismiss();
                }
            }).show();
        }
    }

    private class UploadTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            if (Const.isConnetionToPc()) {
                mData.setTaskState(Const.TASK_STATE_DEALED);
                mData.setDealPeople(Const.getUserName(mActivity));
                final long taskId = mData.getTaskId();
                String needUploadData = ((DeviceApplication) DeviceApplication.getInstance()).sendDataToPc(taskId + "");
                if (TextUtils.isEmpty(needUploadData)) {
                    return "从数据库中获取的数据为空!";
                } else {
                    Request.addRequestForCode(AdbSocketUtils.UPLOAD_DB_COMMAND, needUploadData, new Request.CallBack() {
                        @Override
                        public void onSuccess(String result) {
                            ((TextView) mNetLoadingWindow.getViewById(R.id.net_loading_tip)).setText("成功上传当前任务，正在删除......");
                            /**删除当前任务*/
                            String selection = TaskTable.getTaskTableInstance().getComlueInfos()[16].getName() + "=?";
                            String[] args = {taskId + ""};
                            TaskTable.getTaskTableInstance().deleteData(selection, args);
                            mNetLoadingWindow.getPopupWindow().dismiss();
                            Toast.makeText(mActivity, "成功上传任务!", Toast.LENGTH_LONG).show();
                            DeviceApplication.invokeDataDownFinishedListener();//刷新
                            mActivity.finish();//关闭当前页面
                        }

                        @Override
                        public void onFail(String erroInfo) {
                            mNetLoadingWindow.getPopupWindow().dismiss();
                            Toast.makeText(mActivity, "上传数据库失败!原因【" + erroInfo + "】" + erroInfo, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                return "还未与电脑连接，不能上传！请连接USB后重试!";
            }
            return null;
        }

        @Override
        public void onPostExecute(final String data) {
            if (!TextUtils.isEmpty(data)) {
                Toast.makeText(mActivity, data, Toast.LENGTH_LONG).show();
            }
        }
    }
}
