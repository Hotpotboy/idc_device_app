package com.zhanghang.idcdevice;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.zhanghang.idcdevice.adbsocket.AdbSocketService;
import com.zhanghang.idcdevice.db.PatrolItemTable;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.utils.PreferenceUtil;
import com.zhanghang.self.utils.SystemUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.RecursiveTask;

/**
 * Created by Administrator on 2016-04-03.
 */
public class Const {
    public static enum DataOperation{
        ADD,
        UPDATE,
        DELETE
    }
    private static String sCurrentUserName = "书生";
    /**数据库名字*/
    public static final String DB_FILE_NAME = "idc_device.db";
    /**preference名字*/
    public static final String PREFERENCE_FILE_NAME = "idc_preference";
    /**任务状态——已处理*/
    public static final String TASK_STATE_DEALED = "已完成";
    /**任务状态——未处理*/
    public static final String TASK_STATE_UNDEAL = "未开始";
    /**任务状态——进行中*/
    public static final String TASK_STATE_DEALING = "进行中";
    /**任务类型——巡检*/
    public static final String TASK_TYPE_XUNJIAN = "1";
    /**任务类型——盘点*/
    public static final String TASK_TYPE_PANDIAN = "2";
    /*************Intent之中的key***********************************************/
    /**机柜扫描码的key*/
    public static final String INTENT_KEY_CABINET_NUM = "intent_key_cabinet_num";
    /**机房二维码称在Intent中的key*/
    public static final String INTENT_KEY_HOUSE_CODE = "intent_key_house_code";
    /**机房名称在Intent中的key*/
    public static final String INTENT_KEY_HOUSE_NAME = "intent_key_house_name";
    /**巡检任务数据key*/
    public static final String INTENT_KEY_TASK_DATA = "intent_key_task_data";
    /**任务类型在Intent中的key*/
    public static final String INTENT_KEY_TASK_TYPE = "intent_key_task_type";
    /**盘点任务数据列表在intent中的key*/
    public static final String INTENT_KEY_PANDIAN_TASK_DATA_LIST = "intent_key_pandian_task_data_list";
    /**加载Fragmentd的key*/
    public static final String INTENT_KEY_LOAD_FRAGMENT = "intent_key_load_fragment";
    /**{@link DialogActivity}展示值的key*/
    public static final String INTENT_KEY_DIALOG_ACTIVITY_SHOW = "intent_key_dialog_activity_show";
    /*************Preference之中的key***********************************************/
    /**所有从电脑获取的数据信息在SharedPreference中的存在KEY*/
    public static final String PREFERENCE_KEY_ALL_DATA_INFOS = "preference_key_all_date_infos";
    /**当前用户名在SharedPreference中的存在KEY*/
    private static final String PREFERENCE_KEY_USER_NAME = "preference_key_user_name";
    /**已完成扫描的机柜扫描码在SharedPreference中的存在KEY*/
    public static final String PREFERENCE_KEY_COMPLETED_CABINET = "preference_key_completed_cabinet";
    /*************相关的工具方法***********************************************/
    /**
     * 改变一个指定字符串中指定子集的颜色
     * @param parent   指定的字符串
     * @param key      指定的子集
     * @param color    颜色
     * @return
     */
    public static SpannableString changeSubColor(String parent,String key,@ColorInt int color){
        if(TextUtils.isEmpty(parent)){
            return null;
        }else if(TextUtils.isEmpty(key)||parent.indexOf(key)<0){
            return new SpannableString(parent);
        }else{
            int start = parent.indexOf(key);
            SpannableString result = new SpannableString(parent);
            result.setSpan(new ForegroundColorSpan(color),start,start+key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return result;
        }
    }

    /**是否连接上PC端，默认为false*/
    public static boolean isConnetionToPc(){
//       return PreferenceUtil.getBooleanInPreferce(DeviceApplication.getInstance(), DeviceApplication.getInstance().getVersionName(), AdbSocketService.IS_CONNECITION_PC_KEY, false);
        return AdbSocketService.isConnectionPc();
    }

    /**
     * 根据任务ID获取此任务对应的巡检项列表
     * @param taskId
     * @return
     * @throws Exception
     */
    public static ArrayList<PatrolItemData> getPatrolItemDataByTaskId(String taskId) throws Exception {
        ArrayList<PatrolItemData> items = new ArrayList<>();
        String[] selectionArgs = new String[2];
        selectionArgs[0]=taskId;
        selectionArgs[1]=1+"";//已启用
        String selection = PatrolItemTable.getPatrolItemTableInstance().getComlueInfos()[11].getName()+"=? AND "+PatrolItemTable.getPatrolItemTableInstance().getComlueInfos()[1].getName()+"=?";
        items = PatrolItemTable.getPatrolItemTableInstance().selectDatas(selection,selectionArgs,"","","",PatrolItemData.class);
        return items;
    }

    public static String getTaskTypeNameByTaskTypNum(String typeNum){
        if(TextUtils.equals(typeNum,TASK_TYPE_XUNJIAN)){
            return "巡检任务";
        }else if(TextUtils.equals(typeNum,TASK_TYPE_PANDIAN)){
            return "盘点任务";
        }else{
            return "未识别类型任务";
        }
    }

    /**
     * 判断一个任务是否被处理了
     * @param data
     * @return  true表示指定的任务已经被处理了
     */
    public static boolean isDealed(TaskData data){
        boolean showDeal = data.getRealStartTime()>0&&!isNullForDBData(data.getTaskState())&&TASK_STATE_DEALED.equals(data.getTaskState());//是否显示编辑按钮
        return showDeal;
    }

    /**
     * 判断来自于数据库的字符串是否为空
     * @param dbData
     * @return
     */
    public static boolean isNullForDBData(String dbData){
        return TextUtils.isEmpty(dbData)||"null".equals(dbData);
    }

    public static String getDataString(long time){
        if(time<=0){
           return null;
        }else{
           return SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss,new Date(time));
        }
    }

    /**
     *获取当前登陆用户名
     * @param context
     * @return
     */
    public static String getUserName(Context context){
        return PreferenceUtil.getStringInPreferce(context,PREFERENCE_FILE_NAME,PREFERENCE_KEY_USER_NAME,sCurrentUserName);
    }

    /**
     * 保存当前登陆用户名
     * @param context
     * @param userName
     */
    public static void setUserName(Context context,String userName){
        if(!TextUtils.equals(userName,getUserName(context))){
            PreferenceUtil.updateStringInPreferce(context,PREFERENCE_FILE_NAME,PREFERENCE_KEY_USER_NAME,userName);
        }
    }
}
