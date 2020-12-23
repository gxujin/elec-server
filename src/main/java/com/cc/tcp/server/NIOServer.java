package com.cc.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOServer {


    //业务线程池
    static ExecutorService pool = Executors.newFixedThreadPool(100);

    private Selector selector;

    public NIOServer(int port){
        try {
            this.selector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(port));
            ssc.register(this.selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务正在监听：" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true){//一直循环
            try {
                int num = this.selector.select();//多路复用器开始监听
                if(num > 0){
                    //获取已经注册在多了复用器上的key通道集
                    Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();

                        //从容器中移除处理过的key
                        keys.remove();

                        //如果是有效的
                        if(key.isValid()){
                            if(key.isAcceptable()){
                                this.accept(key);
                            }
                            if(key.isReadable()){
                                this.read(key);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //接受一个客户端socket进行处理
    private void accept(SelectionKey key) {
        try {
            //1 获取服务通道
            ServerSocketChannel ssc =  (ServerSocketChannel) key.channel();
            //2 执行阻塞方法,当有客户端请求时,返回客户端通信通道
            SocketChannel sc = ssc.accept();
            //3 设置阻塞模式
            sc.configureBlocking(false);
            //4 注册到多路复用器上，并设置可读标识
            sc.register(this.selector, SelectionKey.OP_READ);
            System.out.println("["+ Thread.currentThread().getId() + "]客户端："+sc.getRemoteAddress()+"已接入.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void read(SelectionKey key) {
        SocketChannel sc = (SocketChannel) key.channel();
        String reqData = "";
        try{
            ByteBuffer readBuf = ByteBuffer.allocate(100);
            int count = sc.read(readBuf);
            if(count <= 0){
                key.cancel();
                sc.close();
                return;
            }
            readBuf.flip();
            byte[] bytes = new byte[readBuf.remaining()];
            readBuf.get(bytes);
            reqData = new String(bytes);
        }catch (Exception e){
            e.printStackTrace();
            try{
                sc.close();//客户端异常关闭，会发生空轮询，此处关闭SocketChannel
            }catch (Exception ex){}
            return;
        }

        String body = reqData;
        pool.execute(new Thread(()->{
            try {
                long threadId = Thread.currentThread().getId();
                System.out.println(threadId + ",request msg:" + body);
//                Thread.sleep(10000);
                String msg = "你好";
                System.out.println(threadId + ",response msg: "+msg);
                ByteBuffer writeBuf = ByteBuffer.allocate(100);
                writeBuf.put(msg.getBytes());
                //对缓冲区进行复位
                writeBuf.flip();

                //写出数据到服务端
                sc.write(writeBuf);
                //清空缓冲区数据
                writeBuf.clear();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try{
//                    sc.close();
                }catch (Exception ex){}
            }
        }));
    }

    public static void main(String[] args) {
        //开始监听
        new NIOServer(6668).run();
    }
}
