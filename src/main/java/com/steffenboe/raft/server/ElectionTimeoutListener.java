package com.steffenboe.raft.server;

interface ElectionTimeoutListener {

    void onElectionTimeout();

    void onElectionWon();

    class FakeElectionTimeoutListener implements ElectionTimeoutListener {

        private boolean electionTimeoutInvoked = false;
        private boolean electionWonInvoked = false;

        @Override
        public void onElectionTimeout() {
            electionTimeoutInvoked = true;
        }

        boolean electionTimeoutInvoked() {
            return electionTimeoutInvoked;
        }

        @Override
        public void onElectionWon() {
            electionWonInvoked = true;
        }

        boolean electionWonInvoked() {
            return electionWonInvoked;
        }

    }
}