package com.tianyl.tunnellog.server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Mysql57PackageBuf extends PackageBuf {

    public Mysql57PackageBuf(int capacity) {
        super(capacity);
    }

    public byte[] takePackage() throws InterruptedException {
        final int c;
        final byte[] result;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            // 为空时等待写入
            while (count.get() < 3) {
                if (stop.get()) {
                    return null;
                }
                notEmpty.await();
            }
            byte b1 = data[0];
            byte b2 = data[1];
            byte b3 = data[2];
            int length = ((b3 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b1 & 0xFF);
            while (count.get() < length + 4) {
                if (stop.get()) {
                    return null;
                }
                notEmpty.await();
            }
            result = new byte[length + 4];
            // 复制结果
            System.arraycopy(data, 0, result, 0, result.length);
            // 释放data
            byte[] newData = new byte[capacity];
            System.arraycopy(data, result.length, newData, 0, capacity - result.length);
            data = newData;
            c = count.getAndAdd(-result.length);
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        signalNotFull();
        return result;
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
