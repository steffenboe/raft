package com.steffenboe.raft;

class ServerStartFailedException extends RuntimeException {

    public ServerStartFailedException(String message) {
        super(message);
    }

}
