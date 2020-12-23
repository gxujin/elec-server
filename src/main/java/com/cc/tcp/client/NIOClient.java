package com.cc.tcp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOClient {
    // 选择器
    private Selector selector = null;

    public NIOClient(String ipAddr, int port){
        try{
            // 打开客户端套接字通道
            SocketChannel socketChannel = SocketChannel.open();
            // 设置为非阻塞状态
            socketChannel.configureBlocking(false);
            // 打开选择器
            selector = Selector.open();
            // 注册
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            // 发起连接
            socketChannel.connect(new InetSocketAddress(ipAddr, port));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        for(int i=0;i<1;i++) {
            new Thread(() -> {
                try {
                    NIOClient client = new NIOClient("127.0.0.1", 6668);
                    client.send("wo shi zhangsan");
                } catch (Exception e) {
                }
            }).start();
        }
    }

    public void send(String msg) throws IOException {
        boolean closed = false;
        while (!closed) {
            int count = selector.select();
            if (count > 0) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();

                    if (key.isConnectable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        if(socketChannel.isConnectionPending()) {
                            // 完成连接
                            boolean result = socketChannel.finishConnect();
                            if(result){
                                System.out.println("服务连接成功.");
                                socketChannel.register(selector, SelectionKey.OP_WRITE);
                            }else {
                                System.out.println("服务连接失败.");
                            }
                        }
                    }else if (key.isReadable()) {// 通道的可读事件就绪
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(100);
//                        System.out.println("isReadable>>>>>>");
//                        System.out.println("初始化>>>>>>");
//                        System.out.println("position：" + buffer.position());
//                        System.out.println("limit：" + buffer.limit());
//                        System.out.println("capacity：" + buffer.capacity());

                        int len = socketChannel.read(buffer);
//                        System.out.println("read后>>>>>>");
//                        System.out.println("position：" + buffer.position());
//                        System.out.println("limit：" + buffer.limit());
//                        System.out.println("capacity：" + buffer.capacity());
                        if (len > 0) {
                            buffer.flip();
//                            System.out.println("flip后>>>>>>");
//                            System.out.println("position：" + buffer.position());
//                            System.out.println("limit：" + buffer.limit());
//                            System.out.println("capacity：" + buffer.capacity());
                            System.out.println("server：" + new String(buffer.array(), "UTF-8"));
                            closed = true;
                        }
                        socketChannel.close();
                    }else if (key.isWritable()) {// 通道的可写事件就绪
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        // 发送数据给Server
                        ByteBuffer buffer = ByteBuffer.allocate(100);
//                            System.out.println("isWritable初始化>>>>>>");
//                            System.out.println("position：" + buffer.position());
//                            System.out.println("limit：" + buffer.limit());
//                            System.out.println("capacity：" + buffer.capacity());
//                            System.out.println("put后>>>>>>");
                        buffer.put(msg.getBytes("UTF-8"));
//                            System.out.println("position：" + buffer.position());
//                            System.out.println("limit：" + buffer.limit());
//                            System.out.println("capacity：" + buffer.capacity());
                        buffer.flip();
//                            System.out.println("flip后>>>>>>");
//                            System.out.println("position：" + buffer.position());
//                            System.out.println("limit：" + buffer.limit());
//                            System.out.println("capacity：" + buffer.capacity());
                        socketChannel.write(buffer);
                        System.out.println("client：" + msg);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                }
            }
        }
    }
}
