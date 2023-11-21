/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;

class Follower implements ServerState {

    private boolean receivedHeartbeat;
    private final ElectionTimeoutListener electionTimeoutListener;
    private boolean isWaiting = true;
    private final long electionTimeout = 3L;

    public Follower(ElectionTimeoutListener electionTimeoutListener) {
        this.electionTimeoutListener = electionTimeoutListener;
        waitForHeartbeat();
    }

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        Message message = new Message(in.readLine());
        if (!message.isFromLeader()) {
            return false;
        }
        if (message.isAppendEntryMessage()) {
            receivedHeartbeat = true;
            return true;
        }
        return false;
    }

    boolean receivedHeartbeat() {
        return receivedHeartbeat;
    }

    private void waitForHeartbeat() {
        Thread.ofVirtual().start(() -> {
            try {
                waitForTimeout();
                verifyReceivedHeartbeat();
            } catch (InterruptedException ex) {
                System.err.println("Error while waiting for heartbeat: " + ex.getMessage());
                isWaiting = false;
                Thread.currentThread().interrupt();
            }
        });
    }

    private void waitForTimeout() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(electionTimeout));
    }

    private void verifyReceivedHeartbeat() throws InterruptedException {
        while (isWaiting) {
            if (!receivedHeartbeat) {
                electionTimeoutListener.onElectionTimeout();
                isWaiting = false;
            }
            waitForTimeout();
        }
        Thread.currentThread().interrupt();
    }

}
