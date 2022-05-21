package com.self.tank;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class NettyServer {
    public static void main(String[] args) {
        // 创建两个NioEventLoopGroup bossGroup监听客户端请求 workGroup处理每条连接的数据读写。
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 处理http消息的编解码
                        pipeline.addLast("httpServerCodec", new HttpServerCodec());
                        pipeline.addLast("HttpRequestDecoder", new HttpRequestDecoder());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65535));
                        // 添加自定义的ChannelHandler
                        pipeline.addLast("httpServerHandler", new SimpleChannelInboundHandler<FullHttpRequest>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg)
                                    throws Exception {
                                ctx.channel().remoteAddress();
                                FullHttpRequest request = msg;
                                System.out.println("请求方法名称:" + request.method().name());
                                System.out.println("uri:" + request.uri());
                                ByteBuf buf = request.content();
                                System.out.print(buf.toString(CharsetUtil.UTF_8));
                                ByteBuf byteBuf = Unpooled.copiedBuffer("hello world", CharsetUtil.UTF_8);
                                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                        HttpResponseStatus.OK, byteBuf);
                                response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                                response.headers().add(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
                                ctx.writeAndFlush(response);
                            }
                        });
                    }
                });
        ChannelFuture future;
        try {
            // 绑定端口并监听同步。
            future = serverBootstrap.bind(8086).sync();
            // 等待服务端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
