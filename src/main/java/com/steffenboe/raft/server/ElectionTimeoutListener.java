package com.steffenboe.raft.server;

interface ElectionTimeoutListener {

    void onElectionTimeout();

    class FakeElectionTimeoutListener implements ElectionTimeoutListener {

        private boolean gotInvoked = false;

        @Override
        public void onElectionTimeout() {
            gotInvoked = true;
        }

        boolean gotInvoked() {
            return gotInvoked;
        }

    }
}