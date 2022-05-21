package com.self.tank;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class Server {
    public static void main(String[] args) {
        // 监听连接的线程池。
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(2);
        // 处理每个已经连接上的客户端连接。
        EventLoopGroup workerGroup = new NioEventLoopGroup(3);
        // 服务端的袋子，启动对象。
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            ChannelFuture channelFuture = serverBootstrap.group(bossLoopGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) //指定channel的类型
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 每一个客户端连接成功后会调用的回调方法,被请入店中，并且把连接accept得到的
                        // 客户端的SocketChannel传入。
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 管道，可添加一些handle进行流式处理，类似于责任链中的chain，链表。
                            ChannelPipeline channelPipeline = socketChannel.pipeline();
                            channelPipeline.addLast(new ServerChildHandler());
                            System.out.println(socketChannel);
                        }
                    }).bind(8076) // 监听端口。
                    .sync(); // sync，表示同步，不然前面的方法都是异步的在Netty中。
            System.out.println("server started");
            // 用于阻塞，拿到serverchannel，会等待调用close()方法，才会继续执行。
            channelFuture.channel().closeFuture().sync();  //进行阻塞，等待服务端链路关闭。
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossLoopGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}

class ServerChildHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("start read");
        ByteBuf byteBuf = null;
        try {
            byteBuf = (ByteBuf) msg;
            System.out.println(byteBuf);
            System.out.println(byteBuf.toString(CharsetUtil.UTF_8));
            System.out.println(byteBuf.refCnt());
        } finally {
            if (byteBuf != null) ReferenceCountUtil.release(byteBuf);
            System.out.println(byteBuf.refCnt());
        }
    }
}
