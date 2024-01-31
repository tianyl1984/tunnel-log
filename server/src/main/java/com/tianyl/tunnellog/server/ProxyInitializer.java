package com.tianyl.tunnellog.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ProxyInitializer extends ChannelInitializer<SocketChannel> {

    private final String remoteHost;
    private final int remotePort;

    public ProxyInitializer(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(
                new com.tianyl.tunnellog.server.LoggingHandler(),
//                new LoggingHandler(LogLevel.INFO),
                new ProxyFrontendHandler(remoteHost, remotePort));
    }

}
