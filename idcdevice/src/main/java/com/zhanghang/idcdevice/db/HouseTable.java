package com.zhanghang.idcdevice.db;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.mode.pandian.HouseData;
import com.zhanghang.self.db.BaseSQLiteHelper;
import com.zhanghang.self.db.ComlueInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-04-18.
 * 机房信息表辅助类
 */
public class HouseTable extends BaseSQLiteHelper<HouseData> {
    private static final String TABLE_NAME = "house";
    private static HouseTable sHouseTableInstance;

    public static HouseTable getTaskTableInstance(){
        synchronized (HouseTable.class){
            if(sHouseTableInstance ==null){
                ArrayList<ComlueInfo> comlues = getComlueInfos(HouseData.class);
                sHouseTableInstance = new HouseTable(comlues.toArray(new ComlueInfo[comlues.size()]));
            }
        }
        return sHouseTableInstance;
    }
    private HouseTable(ComlueInfo[] comlueNames) {
        super(DeviceApplication.getInstance(), Const.DB_FILE_NAME, TABLE_NAME, comlueNames, DeviceApplication.getInstance().getVersionCode());
    }
}
