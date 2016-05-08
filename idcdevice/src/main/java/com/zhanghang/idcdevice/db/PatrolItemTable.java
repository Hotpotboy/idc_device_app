package com.zhanghang.idcdevice.db;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.mode.PatrolItemData;
import com.zhanghang.self.db.BaseSQLiteHelper;
import com.zhanghang.self.db.ComlueInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-04-18.
 */
public class PatrolItemTable extends BaseSQLiteHelper<PatrolItemData> {
    private static final String TABLE_NAME = "patrol_item";
    private static PatrolItemTable sPatrolItemTableInstance;

    public static PatrolItemTable getPatrolItemTableInstance(){
        synchronized (PatrolItemTable.class){
            if(sPatrolItemTableInstance ==null){
                ArrayList<ComlueInfo> comlues = getComlueInfos(PatrolItemData.class);
                sPatrolItemTableInstance = new PatrolItemTable(comlues.toArray(new ComlueInfo[comlues.size()]));
            }
        }
        return sPatrolItemTableInstance;
    }
    private PatrolItemTable(ComlueInfo[] comlueNames) {
        super(DeviceApplication.getInstance(), Const.DB_FILE_NAME, TABLE_NAME, comlueNames, DeviceApplication.getInstance().getVersionCode());
    }
}
