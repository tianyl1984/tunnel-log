package com.tianyl.tunnellog.server;

public class LoggingListener {

    public static void addListener(PackageBuf buf) {
        new ListenerThread(buf).start();
    }

    public static class ListenerThread extends Thread {

        private PackageBuf buf;

        public ListenerThread(PackageBuf buf) {
            this.buf = buf;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    byte[] bs = buf.takePackage();
                    // 退出了
                    if (bs == null) {
                        ChannelUtil.print("exit");
                        break;
                    }
                    ChannelUtil.print(buf.parseData(bs));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("异常退出");
                    break;
                }
            }
        }
    }
}
