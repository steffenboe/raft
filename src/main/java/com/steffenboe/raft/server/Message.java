package com.steffenboe.raft.server;

record Message(String content) {

    String process() {
        return content;
    }
    
}
