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
    private Thread heartbeatWait;

    public Follower(ElectionTimeoutListener electionTimeoutListener) {
        this.electionTimeoutListener = electionTimeoutListener;
        heartbeatWait = waitForHeartbeat();
    }

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        Message message = new Message(in.readLine());
        if (!message.isFromLeader()) {
            return false;
        }
        if (message.isAppendEntryMessage()) {
            System.out.println("Received heartbeat...");
            heartbeatWait.interrupt();
            heartbeatWait = waitForHeartbeat();
            return true;
        }
        return false;
    }

    private Thread waitForHeartbeat() {
        return Thread.ofVirtual().start(() -> {
            try {
                System.out.println("Waiting for next heartbeat for " + electionTimeout + "s");
                waitForTimeout();
                heartBeatTimeout();
            } catch (InterruptedException ex) {
                isWaiting = false;
            }
        });
    }

    private void waitForTimeout() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(electionTimeout));
    }

    private void heartBeatTimeout() throws InterruptedException {
        System.out.println("No heartbeat received, starting new election...");
        electionTimeoutListener.onElectionTimeout();
    }

}
