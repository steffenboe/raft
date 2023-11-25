package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

class Candidate implements ServerState {

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}