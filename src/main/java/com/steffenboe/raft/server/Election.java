package  com.steffenboe.raft.server;

class Election implements ElectionTimeoutListener {

    private final ElectionStartedListener electionStartedListener;

    public Election(ElectionStartedListener electionStartedListener) {
        this.electionStartedListener = electionStartedListener;
    }

    @Override
    public void onElectionTimeout() {
        System.out.println("Started new election...");
        electionStartedListener.onNewElection();
    }

    @Override
    public void onElectionWon() {
        System.out.println("Election won!");
        electionStartedListener.onWonElection();

    }
    
}