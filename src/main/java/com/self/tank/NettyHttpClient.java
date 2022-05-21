package com.self.tank;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.URI;

public class NettyHttpClient {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8086;
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 向管道中添加流处理。
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpClientCodec());
                pipeline.addLast(new HttpObjectAggregator(65535));
                pipeline.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        URI uri = new URI("http://127.0.0.1:8080");
                        String msg = "hello?";
                        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                                uri.toASCIIString(), Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));

                        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                        // 发送http请求
                        ctx.channel().writeAndFlush(request);
                    }

                    // 处理服务端返回的消息
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
                        FullHttpResponse response = msg;
                        response.headers().get(HttpHeaderNames.CONTENT_TYPE);
                        ByteBuf buf = response.content();
                        System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
                    }
                });
            }
        });
        // 启动客户端.
        ChannelFuture future;
        try {
            future = bootstrap.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
        }
        group.shutdownGracefully();
    }
}
