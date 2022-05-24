package com.self.tank.Codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;
// 将这两个编码、解码的handler加入到pipline中即可，就能实现对java对象进行编码解码了。
public class MsgPackDecode extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 将传输过来的byteBuf，二进制数组进行解码，然后调用其read方法反序列化成Object对象，并加入到list中。
        final byte[] array;
        array = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(),array,0,byteBuf.readableBytes()); // 将传输过来的二进制存入到array
        MessagePack messagePack = new MessagePack();
        list.add(messagePack.read(array));// 调用messagePack的read方法将传输过来的二进制进行反序列化
    }
}
