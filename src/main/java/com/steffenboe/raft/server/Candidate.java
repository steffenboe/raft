package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Candidate implements ServerState {

    private int votes = 0;
    private final List<Integer> neighbors;
    private String id;
    private Server server;

    /**
     *
     * @param neighbors all ports, except the one the current server is running
     * on
     */
    Candidate(Server server, List<Integer> neighbors) {
        this.neighbors = neighbors;
        this.id = UUID.randomUUID().toString();
        this.server = server;
    }

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        return false;
    }

    synchronized int votes() {
        return votes;
    }

    @Override
    public void initialize() {
        System.out.println("Voting for self, then issuing request vote requests to neighors...");
        votes++;
        List<Thread> requestVoteThreads = requestVotesFromNeighbors();
        waitForThreadsToFinish(requestVoteThreads);
        evaluateVotes();
    }

    private void waitForThreadsToFinish(List<Thread> requestVoteThreads) {
        System.out.println("Finished issuing requests, waiting for results...");
        for (Thread thread : requestVoteThreads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
    }

    private void evaluateVotes() {
        System.out.println("Votes received: " + votes());
        if (votes() >= ((neighbors.size() + 1) / 2) + 1) {
            System.out.println("Election won with " + votes + " votes!");
            server.onWonElection();
        }
    }

    private List<Thread> requestVotesFromNeighbors() {
        List<Thread> requestVoteThreads = new ArrayList<>();
        for (Integer port : neighbors) {
            requestVoteThreads.add(Thread.ofVirtual().start(() -> {
                System.out.println("Requesting vote from: " + port);
                String response = "";
                try {
                    Message message = new Message("c;requestVote;" + id);
                    response = message.send(port);
                } catch (IOException ex) {
                    System.out.println("Requesting vote failed, reason: " + ex.getMessage());
                    Thread.currentThread().interrupt();
                }
                
                if (response.equals("true")) {
                    System.out.println("Successfully gained vote from server " + port);
                    votes++;
                }
                
            }));
        }
        return requestVoteThreads;
    }

}
