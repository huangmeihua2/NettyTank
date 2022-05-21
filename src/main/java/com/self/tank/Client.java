package com.self.tank;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DuplexChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.util.CharsetUtil;

public class Client {
    public static void main(String[] args) {
        // 配置线程组
        EventLoopGroup group = new NioEventLoopGroup();
        // 进行socket连接的袋子，启动类。
        Bootstrap bootstrap = new Bootstrap();
        try {
            ChannelFuture future = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {//创建一个通道初始化对象11                    @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //往pipeline中添加自定义的handler14
                            socketChannel.pipeline().addLast(new NettyClientHandler1());
                        }
                    })
                    .connect("localhost", 8076);
            // 对连接后的返回添加一个监听器。
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (!channelFuture.isSuccess()) {
                        System.out.println("没有连接成功");
                    } else {
                        System.out.println("连接成功");
                    }
                }
            });
            future.sync(); //同步
            System.out.println("........");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            group.shutdownGracefully();
        }
    }
}

// 普通类继承抽象类的话，必须实现它的所有的抽象方法。
class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        System.out.println("initChannel start");
        // 本身这个ClientChannelInitializer对象也会被加入到handle中的。
        ChannelPipeline channelPipeline = socketChannel.pipeline().addLast(new ClientHandler());
        System.out.println(channelPipeline.toString());
    }
}

class ClientHandler extends ChannelInboundHandlerAdapter {

    // 连接初始化的时候就可以用了，即会被执行。
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("start write");
        ChannelFuture channelFuture = ctx.writeAndFlush(Unpooled.copiedBuffer("你好!", CharsetUtil.UTF_8));
        System.out.println(channelFuture.toString());
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //传来的消息包装成字节缓冲区14
        ByteBuf byteBuf = (ByteBuf) msg;
        //Netty提供了字节缓冲区的toString方法，并且可以设置参数为编码格式：CharsetUtil.UTF_816
        System.out.println("服务器端发来的消息：" + byteBuf.toString(CharsetUtil.UTF_8));
    }
}
class NettyClientHandler1 extends ChannelInboundHandlerAdapter {
    //通道就绪事件(就是在bootstrap启动助手配置中addlast了handler之后就会触发此事件)
    //但我觉得也可能是当有客户端连接上后才为一次通道就绪 6
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client :" + ctx);
        //向服务器端发消息 9
        ChannelFuture channelFuture = ctx.writeAndFlush(Unpooled.copiedBuffer("你好啊！", CharsetUtil.UTF_8));
        System.out.println(channelFuture.toString());
    }
    //数据读取事件12
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //传来的消息包装成字节缓冲区14
        ByteBuf byteBuf = (ByteBuf) msg;
        //Netty提供了字节缓冲区的toString方法，并且可以设置参数为编码格式：CharsetUtil.UTF_816
        System.out.println("服务器端发来的消息：" + byteBuf.toString(CharsetUtil.UTF_8));
    }
}
