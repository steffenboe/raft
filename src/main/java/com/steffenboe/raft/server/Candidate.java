package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

class Candidate implements ServerState {

    private int votes = 0;
    private List<Integer> neighbors;

    /**
     *
     * @param neighbors all ports, except the one the current server is running
     * on
     */
    Candidate(List<Integer> neighbors) {
        this.neighbors = neighbors;
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
                try (Socket clientSocket = new Socket("localhost", port)) {
                    System.out.println("Connecting to server at " + port);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    String message = "c;requestvote";
                    out.println(message);
                    String response = in.readLine();
                    if(response.equals("true")){
                        System.out.println("Successfully gained vote from server " + port);
                        votes++;
                    }
                } catch (IOException ex) {

                }

            });
        }
    }

}
