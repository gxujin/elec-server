package com.cc.tcp.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ReactorDemo {
    private Selector selector;

    public ReactorDemo() throws IOException {
        initServer(9999);
    }

    private void initServer(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));

        selector = Selector.open();
        SelectionKey selectionKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new AcceptorHandler(selector));
        System.out.println("服务正在监听：" + port);
    }

    public void start() throws IOException {
        while (selector.select() > 0) {
            Set<SelectionKey> set = selector.selectedKeys();
            Iterator<SelectionKey> iterator = set.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                dispater(selectionKey);
            }
        }
    }

    public void dispater(SelectionKey selectionKey) {
        Handler hander = (Handler) selectionKey.attachment();
        if (hander != null) {
            hander.process(selectionKey);
        }
    }

    public static void main(String[] args) throws IOException {
        new ReactorDemo().start();
    }
}