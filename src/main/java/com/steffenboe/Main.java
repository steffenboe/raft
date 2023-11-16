package com.steffenboe;

import com.steffenboe.raft.server.Server;

public class Main {

    private static Integer[] ports = { 8080, 8081, 8082, 8083, 8084 };

    public static void main(String[] args) {
        Server server = new Server(ports);
        server.start();
    }
}