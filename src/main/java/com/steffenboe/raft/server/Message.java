package com.steffenboe.raft.server;

class Message {

    private final String from;

    Message(String from) {
        this.from = from;
    }

    boolean isFromLeader() {
        return source().equals("l");
    }

    boolean isAppendEntryMessage(){
        return selector().equals("appendentry");
    }

    private String source() {
        return from.split(";")[0];
    }

    private String selector() {
        return from.split(";")[1];
    }

     

    
}
