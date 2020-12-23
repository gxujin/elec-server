package com.cc.tcp.server;

import com.cc.tcp.util.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        new NettyServer(Integer.parseInt(Constants.SOCKET_PORT)).run();
    }

    public void run() throws Exception {
        // accept线程组，用来接受连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // I/O线程组， 用于处理业务逻辑
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 服务端启动引导
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup) // 绑定两个线程组
                    .channel(NioServerSocketChannel.class) // 指定通道类型
                    .option(ChannelOption.SO_BACKLOG, Integer.parseInt(Constants.SOCKET_NUM)) // 设置TCP连接的缓冲区，默认值50
//                    .handler(new LoggingHandler(LogLevel.INFO)) // 设置日志级别
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline(); // 获取处理器链
                            pipeline
                                    .addLast(new ElecUnpackDecoder()) // 添加省电报文解码器
                                    .addLast(new NettyServerHandler()); // 添加新的件处理器
                        }
                    });
            logger.info("服务正在监听：{}", port);
            // 通过bind启动服务
            ChannelFuture f = b.bind(port).sync();
            // 阻塞主线程，直到网络服务被关闭
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("服务启动失败", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
