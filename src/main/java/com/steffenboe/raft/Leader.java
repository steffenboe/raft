/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.steffenboe.raft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class Leader implements ServerState {

    private final List<Integer> neighbors;
    private final Log log;

    private static final UUID ID = UUID.randomUUID();

    private int commitIndex = 1;
    private final Map<Integer, Integer> matchIndex = java.util.Collections.synchronizedMap(new HashMap<>());

    Leader(List<Integer> neighbors, Log log) {
        this.neighbors = neighbors;
        this.log = log;
        neighbors.forEach(port -> {
            matchIndex.put(port, 0);
        });
    }

    @Override
    public void initialize() {
        Thread.ofPlatform().start(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                    appendEntry("");
                } catch (InterruptedException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        });
    }

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        String message = in.readLine();
        System.out.println(this + "Received message: " + message);
        log.append(new Line(commitIndex, message.split(";")[1], false));
        System.out.println(this + "Appended message to log...");
        try {
            appendEntry(message.split(";")[1]);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(this + "Appended new entry sucessfully to followers...");
        return true;
    }

    private void appendEntry(String entry) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        neighbors.forEach(port -> threads.add(Thread.ofPlatform().start(() -> {
            try {
                sendAppendEntryMessage(entry, port);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        })));
        for (Thread thread : threads) {
            thread.join();
        }
        Thread.sleep(100);
        if (!entry.isBlank() && isMajorityAcknowledged()) {
            log.commit(commitIndex);
            System.out.println(this + " Committed log entry: " + commitIndex);
            commitIndex++;
        }
    }

    private synchronized void sendAppendEntryMessage(String entry, Integer port)
            throws IOException {
        Message message = new Message("l;appendentry;" + blankIfNullOr(entry));
        String result = message.send(port);
        if (!blankIfNullOr(entry).isEmpty() && "true".equals(result)) {
            System.out.println(this + " Received response from follower: " + result);
            synchronized (matchIndex) {
                System.out.println(
                        this + " Updated match index for follower: " + port + " to " + log.last().commitIndex());
                matchIndex.put(port, log.last().commitIndex());
            }
        }
    }

    private boolean isMajorityAcknowledged() {
        int count = 1; 
        for (int index : matchIndex.values()) {
            if (index >= commitIndex) {
                count++;
            }
        }
        return count > neighbors.size() / 2;
    }

    private String blankIfNullOr(String entry) {
        return entry != null ? entry : "";
    }

    public int getCurrentCommitIndex() {
        return commitIndex;
    }

    public String lastLog() {
        return log.last().content();
    }

    @Override
    public String toString() {
        return String.format("[LEADER %s] ", ID.toString());
    }

}
