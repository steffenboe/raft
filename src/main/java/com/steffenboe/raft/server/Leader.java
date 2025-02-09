/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;

class Leader implements ServerState {

    private final List<Integer> neighbors;
    private final RaftLog log;

    Leader(List<Integer> neighbors, RaftLog log) {
        this.neighbors = neighbors;
        this.log = log;
    }

    @Override
    public void initialize() {
        appendEntry("");
    }

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        String message = in.readLine().split(";")[1];
        System.out.println("Received message: " + message);
        log.append(message);
        System.out.println("Appended message to log...");
        appendEntry(message);
        System.out.println("Published new entry sucessfull");
        return true;
    }

    private void appendEntry(String entry) {
        neighbors.forEach(port -> {
            Thread.ofVirtual().start(() -> {
                try {
                    while (true) {
                        sendAppendEntryMessage(entry, port);
                        Thread.sleep(Duration.ofSeconds(1));
                    }
                } catch (InterruptedException | IOException ex) {
                    System.err.println(ex.getMessage());
                }
            });
        });
    }

    private void sendAppendEntryMessage(String entry, Integer port) throws IOException {
        Message message = new Message("l;appendentry;" + (entry != null ? entry : ""));
        message.send(port);
    }

}
