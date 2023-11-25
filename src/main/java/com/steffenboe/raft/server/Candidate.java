package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

class Candidate implements ServerState {

    private int votes = 0;
    private final List<Integer> neighbors;
    private String id;

    /**
     *
     * @param neighbors all ports, except the one the current server is running
     * on
     */
    Candidate(List<Integer> neighbors) {
        this.neighbors = neighbors;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        return false;
    }

    int votes() {
        return votes;
    }

    @Override
    public void initialize() {
        System.out.println("Voting for self, then issuing request vote requests to neighors...");
        votes++;
        for (int port : neighbors) {
            Thread.ofVirtual().start(() -> {
                String response = "";
                try {
                    SocketConnection socketConnection = new SocketConnection();
                    socketConnection.connect(port);
                    socketConnection.send("c;requestvote;" + id);
                    response = socketConnection.response();
                } catch (IOException ex) {
                    System.out.println("Requesting vote failed, reason: " + ex.getMessage());
                    Thread.currentThread().interrupt();
                }
                
                if (response.equals("true")) {
                    System.out.println("Successfully gained vote from server " + port);
                    votes++;
                }

            });
        }
    }

}
