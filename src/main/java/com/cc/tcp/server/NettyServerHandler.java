package com.cc.tcp.server;

import com.alibaba.fastjson.JSONObject;
import com.cc.tcp.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    //业务线程池
    static ExecutorService pool = Executors.newFixedThreadPool(Integer.parseInt(Constants.THREAD_NUM));

    private long getThreadId(){
        return Thread.currentThread().getId();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        logger.info("[{}]客户端：{}已接入.", this.getThreadId(), incoming.remoteAddress());
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        logger.info("[{}]客户端：{}已退出.", this.getThreadId(), incoming.remoteAddress());
    }
    // 每当从客户端收到新的数据时，这个方法会在收到消息时被调用
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       try{
           ByteBuf buf = (ByteBuf) msg;
           byte[] bytes = new byte[buf.readableBytes()];
           buf.readBytes(bytes);
           String body = new String(bytes, "UTF-8");

           Channel incoming = ctx.channel();
           logger.info("[{}]客户端：{}收到新消息：{}", Thread.currentThread().getId(), incoming.remoteAddress(), body);
           pool.execute(new MsgTask(ctx, body));
           logger.info("[{}]客户端：{}的消息已提交：{}", Thread.currentThread().getId(), incoming.remoteAddress(), body);
       }catch (Exception e){
           logger.error("消息提交处理异常", e);
       }finally {
           ReferenceCountUtil.release(msg);
       }
    }

    // 数据读取完后被调用
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    // 当Netty由于IO错误或者处理器在处理事件时抛出的异常时被调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    class MsgTask implements Runnable {

        private ChannelHandlerContext ctx;
        private String body;

        public MsgTask(ChannelHandlerContext ctx, String body) {
            this.ctx = ctx;
            this.body = body;
        }

        @Override
        public void run() {
            try{
                long threadId = Thread.currentThread().getId();
                logger.info("[{}]接收报文：{}", threadId, body);
                JSONObject reqJson = new JSONObject();
                reqJson.put("body", body);
                String resJsonStr = "";
//           resJsonStr = HttpUtils.httpPostJson(Constants.HTTP_SERVICE_URL, reqJson.toJSONString());
                //测试数据
                resJsonStr = "{\"data\": \"\"}";
                JSONObject resJson = JSONObject.parseObject(resJsonStr);
                String resData = (String) resJson.get("data");
                resData = StringUtils.isBlank(resData) ? "hello" : resData;
                logger.info("[{}]返回报文：{}", threadId, resData);
                ByteBuf respByteBuf = Unpooled.copiedBuffer(resData.getBytes("UTF-8"));
                ctx.writeAndFlush(respByteBuf);
            }catch (Exception e){
                logger.error("报文处理异常", e);
            }finally {
                ctx.close();
            }
        }
    }
}

