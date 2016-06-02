package com.zhanghang.idcdevice.db;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.mode.pandian.HouseData;
import com.zhanghang.idcdevice.mode.pandian.PandianResultData;
import com.zhanghang.self.db.BaseSQLiteHelper;
import com.zhanghang.self.db.ComlueInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-04-18.
 * 机房信息表辅助类
 */
public class PandianResultTable extends BaseSQLiteHelper<PandianResultData> {
    private static final String TABLE_NAME = "pandian_result";
    private static PandianResultTable sPandianTableInstance;

    public static PandianResultTable getPandianTableInstance(){
        synchronized (PandianResultTable.class){
            if(sPandianTableInstance ==null){
                ArrayList<ComlueInfo> comlues = getComlueInfos(PandianResultData.class);
                sPandianTableInstance = new PandianResultTable(comlues.toArray(new ComlueInfo[comlues.size()]));
            }
        }
        return sPandianTableInstance;
    }
    private PandianResultTable(ComlueInfo[] comlueNames) {
        super(DeviceApplication.getInstance(), Const.DB_FILE_NAME, TABLE_NAME, comlueNames, DeviceApplication.getInstance().getVersionCode());
    }
}
