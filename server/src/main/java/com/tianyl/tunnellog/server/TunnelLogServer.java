package com.tianyl.tunnellog.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.ExecutionException;

public class TunnelLogServer {

    public static void main(String[] args) {
        new TunnelLogServer().start();
    }

    private NioEventLoopGroup serverBossGroup;

    private NioEventLoopGroup serverWorkerGroup;

    public TunnelLogServer() {
        serverBossGroup = new NioEventLoopGroup(1);
        serverWorkerGroup = new NioEventLoopGroup(1);
    }

    public void start() {
        String remoteHost = "127.0.0.1";
        int remotePort = 3306;
//        String remoteHost = "k3s.local.com";
//        int remotePort = 30306;
        remotePort= 8700;
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(serverBossGroup, serverWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ProxyInitializer(remoteHost, remotePort))
//                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.AUTO_READ, false)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            bootstrap.bind(3309).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("启动失败");
        }
        System.out.println("启动成功");
    }

    public void stop() {
        serverBossGroup.shutdownGracefully();
        serverWorkerGroup.shutdownGracefully();
    }
}
