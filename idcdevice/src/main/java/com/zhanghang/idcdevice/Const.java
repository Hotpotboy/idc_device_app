package com.zhanghang.idcdevice;

import android.support.annotation.ColorInt;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

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
    /*************Intent之中的key***********************************************/
    /**设备ID的key*/
    public static final String INTENT_KEY_DEVICE_ID = "intent_key_device_id";
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
}
