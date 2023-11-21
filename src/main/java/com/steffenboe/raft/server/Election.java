package  com.steffenboe.raft.server;

class Election implements ElectionTimeoutListener {

    @Override
    public void onElectionTimeout() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}