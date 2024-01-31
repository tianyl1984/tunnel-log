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

    public abstract byte[] takePackage() throws InterruptedException;

    public abstract String parseData(byte[] data);
}
