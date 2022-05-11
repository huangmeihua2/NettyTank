package com.self.tank;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {
    public static void main(String[] args) {
        // 监听连接的线程池
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        // 处理每个已经连接上的客户端连接。
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            ChannelFuture channelFuture = serverBootstrap.group(bossLoopGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) //指定channel的类型
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 每一个客户端连接成功后会调用的回调方法。
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            System.out.println(socketChannel);
                        }
                    }).bind(8888) // 监听端口。
                    .sync(); //sync。
            channelFuture.channel().closeFuture().sync(); // 用于阻塞，拿到serverchannel，会等待调用close()方法，才会继续执行。
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossLoopGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
