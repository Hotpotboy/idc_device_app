package com.zhanghang.idcdevice.interfaces;

/**
     * 操作接口
     */
    public interface PandianOperationListener {
    /**
     * 添加设备操作码
     */
    int OPERATION_CODE_ADD_DEVICE = 1;
    /**
     * 删除设备操作码
     */
    int OPERATION_CODE_DELETE_DEVICE = 2;
    /**
     * 删除机柜操作码
     */
    int OPERATION_CODE_DELETE_CABINET = 3;
    /**
     * 打开机柜操作码
     */
    int OPERATION_CODE_OPEN_CABINET = 4;
        /**
         * @param operationCode 操作码
         * @param ext           额外数据
         */
        void operation(int operationCode, Object ext);
    }