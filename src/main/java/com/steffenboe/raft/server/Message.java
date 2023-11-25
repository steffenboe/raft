package com.steffenboe.raft.server;

class Message {

    private final String content;

    Message(String content) {
        this.content = content;
    }

    boolean isFromLeader() {
        return source().equals("l");
    }

    boolean isAppendEntryMessage(){
        return selector().equals("appendentry");
    }

    private String source() {
        return content.split(";")[0];
    }

    private String selector() {
        return content.split(";")[1];
    }

     

    
}
