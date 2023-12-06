package com.steffenboe.raft.server;

import java.io.IOException;

class Message {

    private final String content;

    Message(String content) {
        this.content = content;
    }

    String send(int port) throws IOException {
        SocketConnection connection = new SocketConnection();
        connection.connect(port);
        connection.send(content);
        return connection.response();
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

    boolean isRequestVoteMessage() {
        return selector().equals("requestvote");
    }

    String candidateId() {
        return content.split(";")[2];
    }

    int term() {
        System.out.println(content.split(";")[3]);
        return Integer.parseInt(content.split(";")[3]);
    }

     

    
}
