package com.steffenboe.raft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public interface ServerState {

    public void initialize();

    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException;
}
