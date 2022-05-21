package com.self.tank;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

public class NettyServer1 {
    public static void main(String[] args) throws InterruptedException {
        //创建两个线程池
        //创建一个线程组，接收客户端的连接 7
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //创建一个线程组，用于处理网络操作 9
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //创建服务器端启动助手（用于配置参数）11
        //精华部分，设置通道的底层实现，
        //通过NioServerSocketChannel
        //这也是Netty的与NIO搭配的地方(此处作为服务器端通道的实现)16
        // 设置线程队列中等待连接的个数 .childOption(ChannelOption.SO_KEEPALIVE, true)
        //是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并
        //且在两个小时左右
        //上层没有任何数据传输的情况下，这套机制才会被激活。
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 12)
                .childHandler(new ChannelInitializer<SocketChannel>() {//(用内部类的方法)
                    public void initChannel(SocketChannel sc) {
                        System.out.println(sc);
                        sc.pipeline().addLast(new NettyServerHandler());//往pipeline链中添加
                    }
                });
        System.out.println("...Server is Ready...");
        //ChannelFuture接口，用于在之后的某个时间点确定结果31
        ChannelFuture sf = null;//绑定端口 非阻塞 异步32
        try {
            sf = serverBootstrap.bind(9999).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("....Server is Start....");
        //关闭通道，关闭线程组34
        sf.channel().closeFuture().sync();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}

class NettyServerHandler extends ChannelInboundHandlerAdapter {
    //数据读取事件 5
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //传来的消息包装成字节缓冲区
        ByteBuf byteBuf = (ByteBuf) msg;
        //Netty提供了字节缓冲区的toString方法，并且可以设置参数为编码格式：
        //CharsetUtil.UTF_8
        System.out.println("客户端发来的消息：" + byteBuf.toString(CharsetUtil.UTF_8));
    }

    //数据读取完毕事件13
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //数据读取完毕，将信息包装成一个Buffer传递给下一个Handler，Unpooled.copiedBuffer会返回一个Buffer
        //调用的是事件处理器的上下文对象的writeAndFlush方法
        //意思就是说将  你好  传递给了下一个handler17
        ctx.writeAndFlush(Unpooled.copiedBuffer("你好!", CharsetUtil.UTF_8));
    }

    //异常发生的事件
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //异常发生时关闭上下文对象
        ctx.close();
    }
}