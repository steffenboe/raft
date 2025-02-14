package com.steffenboe.raft;

import java.util.List;

public class Main {

    private static List<Integer> ports = List.of(8080, 8081, 8082, 8083, 8084);

    public static void main(String[] args) {
        Server server = new Server(ports, new FromFileTerm(), new Log.InMemoryLog());
        server.start();
    }
}