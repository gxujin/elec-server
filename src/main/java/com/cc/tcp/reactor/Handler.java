package com.cc.tcp.reactor;

import java.nio.channels.SelectionKey;

public interface Handler {

    void process(SelectionKey selectionKey);

}
