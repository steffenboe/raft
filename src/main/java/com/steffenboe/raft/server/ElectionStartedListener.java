package com.steffenboe.raft.server;

interface ElectionStartedListener {

    void onNewElection();

    void onWonElection();

    class FakeElectionStartedListener implements ElectionStartedListener {

        private boolean onWonElectionTriggered = false;

        @Override
        public void onNewElection() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onWonElection() {
            onWonElectionTriggered = true;
        }

        boolean wonElectionTriggered() {
            return onWonElectionTriggered;
        }
        
    }
}