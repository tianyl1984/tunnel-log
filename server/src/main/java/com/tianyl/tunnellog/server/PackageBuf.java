package com.tianyl.tunnellog.server;

import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class PackageBuf {

    public static final AttributeKey<PackageBuf> ATTRIBUTE = AttributeKey.valueOf("PackageBuf");

    protected final AtomicBoolean stop = new AtomicBoolean(false);

    protected final ReentrantLock takeLock = new ReentrantLock();

    protected final Condition notEmpty = takeLock.newCondition();

    private final List<Byte> data = new CopyOnWriteArrayList<>();

    public PackageBuf() {
        LoggingListener.addListener(this);
    }

    public void addData(byte[] toAdd) throws InterruptedException {
        List<Byte> list = new ArrayList<>();
        for (byte b : toAdd) {
            list.add(b);
        }
        data.addAll(list);
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

    public byte getData(int idx) {
        return data.get(idx);
    }

    public byte[] getDatas(int size) {
        byte[] bs = new byte[size];
        for (int i = 0; i < size; i++) {
            bs[i] = data.get(i);
        }
        return bs;
    }

    public byte[] takePackage() throws InterruptedException {
        final int c;
        final byte[] result;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            // 为空时等待写入
            while (data.isEmpty()) {
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
                int cnt = data.size();
                takeCnt = tryToTake(cnt);
                if (takeCnt == 0) {
                    notEmpty.await();
                } else {
                    break;
                }
            }
            result = new byte[takeCnt];
            for (int i = 0; i < takeCnt; i++) {
                result[i] = data.get(0);
                data.remove(0);
            }
            if (data.size() > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        return result;
    }

    public abstract int tryToTake(int cnt);

    public abstract String parseData(byte[] data);
}
