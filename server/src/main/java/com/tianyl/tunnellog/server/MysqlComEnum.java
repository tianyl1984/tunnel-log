package com.tianyl.tunnellog.server;

public enum MysqlComEnum {
    Com_Quit((byte) 1, "Quit"),
    Com_Init_DB((byte) 2, "InitDB"),
    Com_Query((byte) 3, "Query"),
    Com_Field_List((byte) 4, "FieldList"),
    Com_Create_DB((byte) 5, "CreateDB"),
    Com_Drop_DB((byte) 6, "DropDB"),
    Com_Refresh((byte) 7, "Refresh"),
    Com_Shutdown((byte) 8, "Shutdown"),
    Com_Statistics((byte) 9, "Statistics"),
    Com_Process_Info((byte) 10, "ProcessInfo"),
    Com_Connect((byte) 11, "Connect"),
    Com_Process_Kill((byte) 12, "ProcessKill"),
    Com_Debug((byte) 13, "Debug"),
    Com_Ping((byte) 14, "Ping"),
    Com_Time((byte) 15, "Time"),
    Com_Delayed_Insert((byte) 16, "DelayedInsert"),
    Com_Change_User((byte) 17, "ChangeUser"),
    Com_Binlog_Dump((byte) 18, "BinlogDump"),
    Com_Table_Dump((byte) 19, "TableDump"),
    Com_Connect_Out((byte) 20, "ConnectOut"),
    Com_Regiser_Slave((byte) 21, "RegiserSlave"),
    Com_Stmt_Prepare((byte) 22, "StmtPrepare"),
    Com_Stmt_Execute((byte) 23, "StmtExecute"),
    Com_Stmt_Send_Long_Data((byte) 24, "StmtSendLongData"),
    Com_Stmt_Close((byte) 25, "StmtClose"),
    Com_Stmt_Reset((byte) 26, "StmtReset"),
    Com_Set_Option((byte) 27, "SetOption"),
    Com_Stmt_Fetch((byte) 28, "StmtFetch"),
    ;

    private MysqlComEnum(byte value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private byte value;

    private String desc;

    public static String getDesc(byte val) {
        for (MysqlComEnum com : MysqlComEnum.values()) {
            if (com.value == val) {
                return com.desc;
            }
        }
        return "unknow";
    }
}
