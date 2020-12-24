package com.cc.tcp.reactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptorHandler implements Handler {

    private Selector selector;

    public AcceptorHandler(Selector selector){
        this.selector = selector;
    }

    @Override
    public void process(SelectionKey selectionKey) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel channel = serverSocketChannel.accept();
            System.out.println("建立链接：" + channel.getRemoteAddress());
            channel.configureBlocking(false);
            SelectionKey readSelectionKey = channel.register(selector, SelectionKey.OP_READ);
            readSelectionKey.attach(new ProcessHandler(selector));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
