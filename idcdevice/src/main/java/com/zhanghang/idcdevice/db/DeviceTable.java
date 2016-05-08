package com.zhanghang.idcdevice.db;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.mode.DeviceData;
import com.zhanghang.self.db.BaseSQLiteHelper;
import com.zhanghang.self.db.ComlueInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-04-18.
 */
public class DeviceTable extends BaseSQLiteHelper<DeviceData> {
    private static final String TABLE_NAME = "device";
    private static DeviceTable sDeviceTableInstance;

    public static DeviceTable getDeviceTableInstance(){
        synchronized (DeviceTable.class){
            if(sDeviceTableInstance ==null){
                ArrayList<ComlueInfo> comlues = getComlueInfos(DeviceData.class);
                sDeviceTableInstance = new DeviceTable(comlues.toArray(new ComlueInfo[comlues.size()]));
            }
        }
        return sDeviceTableInstance;
    }
    private DeviceTable(ComlueInfo[] comlueNames) {
        super(DeviceApplication.getInstance(), Const.DB_FILE_NAME, TABLE_NAME, comlueNames, DeviceApplication.getInstance().getVersionCode());
    }
}
