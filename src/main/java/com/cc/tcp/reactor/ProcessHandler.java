package com.cc.tcp.reactor;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ProcessHandler implements Handler {

    private Selector selector;

    public ProcessHandler(Selector selector){
        this.selector = selector;
    }

    @Override
    public void process(SelectionKey selectionKey) {
        try {
            SocketChannel sc = (SocketChannel) selectionKey.channel();
            String reqData = "";
            try{
                ByteBuffer readBuf = ByteBuffer.allocate(10);
                int count = sc.read(readBuf);
                if(count <= 0){
                    selectionKey.cancel();
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

            if("q".equals(reqData)){
                sc.close();
                System.out.println("客户端主动退出.");
                return;
            }

            String body = reqData;
            long threadId = Thread.currentThread().getId();
            System.out.println(threadId + ",request msg:" + body);
            String msg = "nihao";
            System.out.println(threadId + ",response msg: "+msg);
            ByteBuffer writeBuf = ByteBuffer.allocate(100);
            writeBuf.put(msg.getBytes());
            //对缓冲区进行复位
            writeBuf.flip();

            //写出数据到服务端
            sc.write(writeBuf);
            //清空缓冲区数据
            writeBuf.clear();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
