package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

class Candidate implements ServerState {

    private int votes = 0;

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        return false;
    }

    int votes() {
        return votes;
    }

    @Override
    public void initialize() {
        votes++;
    }

}