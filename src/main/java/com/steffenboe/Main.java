package com.steffenboe;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(Integer.valueOf(args[0]));
        server.listen();
    }
}