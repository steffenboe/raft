/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.steffenboe.raft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

class Leader implements ServerState {

    private final List<Integer> neighbors;
    private final Log log;

    private static final UUID ID = UUID.randomUUID();

    private int commitIndex = 1;

    Leader(List<Integer> neighbors, Log log) {
        this.neighbors = neighbors;
        this.log = log;
    }

    @Override
    public void initialize() {
        appendEntry("");
    }

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        String message = in.readLine();
        System.out.println(this + "Received message: " + message);
        log.append(new Line(commitIndex, message, false));
        System.out.println(this + "Appended message to log...");
        appendEntry(message);
        System.out.println(this + "Appended new entry sucessfully to followers...");
        return true;
    }

    private void appendEntry(String entry) {
        neighbors.forEach(port -> Thread.ofVirtual().start(() -> {
            try {
                System.out.println(this + "Sending append entry message to port: " + port);
                sendAppendEntryMessage(entry, port);
                Thread.sleep(Duration.ofSeconds(1));
            } catch (InterruptedException | IOException ex) {
                System.err.println(ex.getMessage());
            }
        }));
    }

    private synchronized void sendAppendEntryMessage(String entry, Integer port) throws IOException {
        Message message = new Message("l;appendentry;" + blankIfNullOr(entry));
        String result = message.send(port);
        if (!blankIfNullOr(entry).isEmpty() && result.equals("true")) {
            log.commit(commitIndex);
            commitIndex++;
        }
    }

    private String blankIfNullOr(String entry) {
        return entry != null ? entry : "";
    }

    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    public String toString() {
        return String.format("[LEADER %s] ", ID.toString());
    }

}
