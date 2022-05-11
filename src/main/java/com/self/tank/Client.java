package com.self.tank;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class Client {
    public static void main(String[] args) {
        // 配置线程组
        EventLoopGroup group = new NioEventLoopGroup();
        // 进行socket连接的袋子，启动类。
        Bootstrap bootstrap = new Bootstrap();

        try {
            ChannelFuture future = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            System.out.println(socketChannel);
                        }
                    })
                    .connect("localhost", 8888);
            // 对连接后的返回添加一个监听器。
            future.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (!channelFuture.isSuccess()) {
                        System.out.println("没有连接成功");
                    } else {
                        System.out.println("连接成功");
                    }
                }
            });
            future.sync();
            System.out.println("........");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            group.shutdownGracefully();
        }
    }
}
