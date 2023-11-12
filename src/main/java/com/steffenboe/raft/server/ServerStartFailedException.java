package com.steffenboe.raft.server;

class ServerStartFailedException extends RuntimeException {

    public ServerStartFailedException(String message) {
        super(message);
    }

}
