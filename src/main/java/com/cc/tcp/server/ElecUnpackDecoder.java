package com.cc.tcp.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 省电报文解码器
 */
public class ElecUnpackDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ElecUnpackDecoder.class);

    //报文长度域的长度
    private static final int lengthFieldLength = 5;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int bytesCnt = byteBuf.readableBytes();
        if (bytesCnt < 5) {//没有达到报文长度域的长度，暂不处理
            return;
        }

        int beginIndex = byteBuf.readerIndex();

        String lengthStr = byteBuf.readBytes(lengthFieldLength).toString(CharsetUtil.UTF_8);
        Integer length = 0;
        try{
            length = Integer.parseInt(lengthStr, 10);
        }catch (Exception e){
            logger.debug("报文长度域解析错误[{}]", lengthStr);
        }
        if(length <= 0){
            return;
        }
        int contentLength = length - lengthFieldLength;
        bytesCnt = byteBuf.readableBytes();
        if (bytesCnt < contentLength) {
            byteBuf.readerIndex(beginIndex);
            return;
        }

        byteBuf.readerIndex(beginIndex + length);

        ByteBuf otherByteBufRef = byteBuf.slice(beginIndex, length).retain();

        list.add(otherByteBufRef);
    }
}
