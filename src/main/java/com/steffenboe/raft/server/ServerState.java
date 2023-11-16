package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

interface ServerState {

    public void processMessage(PrintWriter out, BufferedReader in) throws IOException;

}
