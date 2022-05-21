package com.self.tank;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.logging.Logger;

public class NettyClient1 {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        //创建客户端的服务启动助手完成相应配置 7
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {//创建一个通道初始化对象11
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //往pipeline中添加自定义的handler14
                        socketChannel.pipeline().addLast(new ClientTestTcpHandler());
                    }
                });
        System.out.println("...Client is Ready...");
        //启动客户端去连接服务器端(通过启动助手)18
        ChannelFuture cf = null;
        try {
            cf = b.connect("127.0.0.1", 9999).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //关闭连接(异步非阻塞)20
        cf.channel().closeFuture().sync();
    }
}

class NettyClientHandler extends ChannelInboundHandlerAdapter {
    //通道就绪事件(就是在bootstrap启动助手配置中addlast了handler之后就会触发此事件)
    //但我觉得也可能是当有客户端连接上后才为一次通道就绪 6
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client :" + ctx);
        //向服务器端发消息 9
        ctx.writeAndFlush(Unpooled.copiedBuffer("你好啊！", CharsetUtil.UTF_8));
    }

    //数据读取事件12
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //传来的消息包装成字节缓冲区14
        ByteBuf byteBuf = (ByteBuf) msg;
        //Netty提供了字节缓冲区的toString方法，并且可以设置参数为编码格式：CharsetUtil.UTF_816
        System.out.println("服务器端发来的消息：" + byteBuf.toString(CharsetUtil.UTF_8));
    }
}
class ClientTestTcpHandler extends ChannelInboundHandlerAdapter{
private static final Logger logger = Logger.getLogger(ClientTestTcpHandler.class.getName());
private int count;
private byte[] req;

    public ClientTestTcpHandler() {
        req = ("QUERY TIME ORDER"+System.getProperty("line.separator")).getBytes();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf mess = null;
        for(int i = 0;i<100;i++){
            mess = Unpooled.buffer(req.length);
            mess.writeBytes(req);//把字节数组中的字节写入到buf中。
            ctx.writeAndFlush(mess);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String body = new String(bytes,"UTF-8");
        System.out.println("Now is :"+body+";the count is :"+ ++count);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warning("Unexpected exception from downstream :"+cause.getMessage());
        ctx.close();
    }
}


