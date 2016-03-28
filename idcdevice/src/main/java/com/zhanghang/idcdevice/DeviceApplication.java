package com.zhanghang.idcdevice;

import android.content.Intent;

import com.zhanghang.self.base.BaseApplication;

/**
 * Created by Administrator on 2016-03-27.
 */
public class DeviceApplication extends BaseApplication {
    @Override
    public void onCreate(){
        super.onCreate();
        Intent intent = new Intent(this,AdbSocketService.class);
        startService(intent);
    }
}
