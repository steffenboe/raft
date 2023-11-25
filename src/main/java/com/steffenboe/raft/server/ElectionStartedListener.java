package com.steffenboe.raft.server;

interface ElectionStartedListener {

    void onNewElection();
}