package com.steffenboe;

record Message(String content) {

    String process() {
        return content;
    }
    
}
