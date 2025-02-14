/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.steffenboe.raft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.UUID;

class Follower implements ServerState {

    private final Server server;

    private final UUID id;

    private double electionTimeout = Math.random() * 5;
    private String votedFor;
    private Thread heartbeatWait;

    private final Log log;
    private int commitIndex = 1;

    public Follower(Server server, Log log) {
        this.server = server;
        this.log = log;
        this.id = UUID.randomUUID();
    }

    public Follower(Server server, Log log, double electionTimeout) {
        this(server, log);
        this.electionTimeout = electionTimeout;
    }

    @Override
    public synchronized boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        Message message = new Message(in.readLine());
        if (message.isAppendEntryMessage()) {
            if (!message.isFromLeader()) {
                return false;
            }
            if (message.isEmpty()) {
                processHeartbeat();
            } else {
                log.append(new Line(commitIndex, message.content(), false));
                System.out.println(this + "FOLLOWER appended  " + message.content() + " at commitIndex " + commitIndex);
            }
            return true;
        }

        if (message.isRequestVoteMessage()) {
            System.out.println(this + "Received request vote from candidate with id: " + message.candidateId());
            if (votedFor == null || votedFor.isEmpty()) {
                votedFor = message.candidateId();
                System.out.println(this + "Voting for candidate with id: " + votedFor);
                out.println("true");
            } else {
                out.println("false");
            }

            return true;
        }
        return false;
    }

    private Thread waitForHeartbeat() {
        return Thread.ofVirtual().start(() -> {
            try {
                waitForTimeout();
                System.out.println(this+ "No heartbeat received, notifiying election timeout...");
                server.onNewElection();
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        });
    }

    private void waitForTimeout() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds((long) electionTimeout));
    }

    private void processHeartbeat() {
        heartbeatWait.interrupt();
        heartbeatWait = waitForHeartbeat();
    }

    @Override
    public void initialize() {
        this.heartbeatWait = waitForHeartbeat();
    }

    @Override
    public String toString() {
        return String.format("[FOLLOWER %s] ", id.toString());
    }

}
