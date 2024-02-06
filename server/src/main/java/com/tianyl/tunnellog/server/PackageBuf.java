package com.tianyl.tunnellog.server;

import io.netty.util.AttributeKey;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class PackageBuf {

    public static final AttributeKey<PackageBuf> ATTRIBUTE = AttributeKey.valueOf("PackageBuf");

    // 目前的个数
    protected final AtomicInteger count = new AtomicInteger();

    protected final AtomicBoolean stop = new AtomicBoolean(false);

    protected final ReentrantLock putLock = new ReentrantLock();

    protected final Condition notFull = putLock.newCondition();

    protected final ReentrantLock takeLock = new ReentrantLock();

    protected final Condition notEmpty = takeLock.newCondition();

    // 容量，最大值
    protected int capacity;

    protected byte[] data;

    public PackageBuf(int capacity) {
        this.capacity = capacity;
        data = new byte[capacity];
        LoggingListener.addListener(this);
    }

    public void addData(byte[] toAdd) throws InterruptedException {
        final int c;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            while (count.get() == capacity) {
                notFull.await();
            }
//            ChannelUtil.print("add:" + toAdd.length + ":" + count.get());
            int cnt = count.get();
            System.arraycopy(toAdd, 0, data, cnt, toAdd.length);
            c = count.getAndAdd(toAdd.length);
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        signalNotEmpty();
    }

    public void stop() {
        stop.set(true);
        signalNotEmpty();
    }

    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    protected void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    public byte[] takePackage() throws InterruptedException {
        final int c;
        final byte[] result;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            // 为空时等待写入
            while (count.get() == 0) {
                if (stop.get()) {
                    return null;
                }
                notEmpty.await();
            }
            int takeCnt = 0;
            while (true) {
                if (stop.get()) {
                    return null;
                }
                int cnt = count.get();
                takeCnt = tryToTake(cnt);
                if (takeCnt == 0) {
                    notEmpty.await();
                } else {
                    break;
                }
            }
            result = new byte[takeCnt];
            // 复制结果
            System.arraycopy(data, 0, result, 0, result.length);
            // 释放data
            byte[] newData = new byte[capacity];
            System.arraycopy(data, result.length, newData, 0, capacity - result.length);
            data = newData;
//            ChannelUtil.print("takePackage before:" + count.get() + ",result:" + result.length);
            c = count.getAndAdd(-result.length);
//            ChannelUtil.print("takePackage after:" + count.get());
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        signalNotFull();
        return result;
    }

    public abstract int tryToTake(int cnt);

    public abstract String parseData(byte[] data);
}
