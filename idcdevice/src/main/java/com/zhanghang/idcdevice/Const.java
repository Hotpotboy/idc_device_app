package com.zhanghang.idcdevice;

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

/**
 * Created by Administrator on 2016-04-03.
 */
public class Const {
    public static enum DataOperation{
        ADD,
        UPDATE,
        DELETE
    }
    public static final String CURRENT_USER_NAME = "书生";
    /**数据库名字*/
    public static final String DB_FILE_NAME = "idc_device.db";
    /**任务状态——已处理*/
    public static final String TASK_STATE_DEALED = "已完成";
    /**任务状态——未处理*/
    public static final String TASK_STATE_UNDEAL = "未开始";
    /*************Intent之中的key***********************************************/
    /**设备ID的key*/
//    public static final String INTENT_KEY_DEVICE_ID = "intent_key_device_id";
    /**任务数据key*/
    public static final String INTENT_KEY_TASK_DATA = "intent_key_task_data";
    /**加载Fragmentd的key*/
    public static final String INTENT_KEY_LOAD_FRAGMENT = "intent_key_load_fragment";
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
        String startTime;
        if(time<=0){
           return null;
        }else{
           return SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss,new Date(time));
        }
    }
}
