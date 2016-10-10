package com.zhanghang.idcdevice.db;

import com.zhanghang.idcdevice.Const;
import com.zhanghang.idcdevice.DeviceApplication;
import com.zhanghang.idcdevice.mode.TaskData;
import com.zhanghang.self.db.BaseSQLiteHelper;
import com.zhanghang.self.db.ComlueInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-04-18.
 */
public class TaskTable extends BaseSQLiteHelper<TaskData> {
    private static final String TABLE_NAME = "task";
    private static TaskTable sTaskTableInstance;

    public static TaskTable getTaskTableInstance(){
        synchronized (TaskTable.class){
            if(sTaskTableInstance ==null){
                setPrimaryKey(TaskData.class,"taskId");
                ArrayList<ComlueInfo> comlues = getComlueInfos(TaskData.class);
                sTaskTableInstance = new TaskTable(comlues.toArray(new ComlueInfo[comlues.size()]));
            }
        }
        return sTaskTableInstance;
    }
    private TaskTable(ComlueInfo[] comlueNames) {
        super(DeviceApplication.getInstance(), Const.DB_FILE_NAME, TABLE_NAME, comlueNames, DeviceApplication.getInstance().getVersionCode());
    }
}
