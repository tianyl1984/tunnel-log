package com.tianyl.tunnellog.server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

public class Mysql57PackageBuf extends PackageBuf {

    public Mysql57PackageBuf() {
        super();
    }

    public int tryToTake(int cnt) {
        if (cnt < 3) {
            return 0;
        }
        byte b1 = getData(0);
        byte b2 = getData(1);
        byte b3 = getData(2);
        int length = ((b3 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b1 & 0xFF);
        if (cnt < length + 4) {
            return 0;
        }
//        System.out.println(length+4);
        return length + 4;
    }

    @Override
    public String parseData(byte[] data) {
        String com = MysqlComEnum.getDesc(data[4]);
        byte[] sqlByte = Arrays.copyOfRange(data, 5, data.length);
        String sql = new String(sqlByte, StandardCharsets.UTF_8);
        String date = DateUtil.format(new Date());
        return String.format("%s [%s] %s", date, com, sql);
    }

}
