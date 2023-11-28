package com.steffenboe.raft.server;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

class CandidateTest {

    private static final Integer[] PORT_RANGE = {8080, 8081};
    private ElectionStartedListener.FakeElectionStartedListener fakeElectionStartedListener = new ElectionStartedListener.FakeElectionStartedListener();

    /**
     * On initialization, a candidate should vote for itself.
     */
    @Test
    void shouldVoteForSelf() {
        Candidate candidate = new Candidate(Collections.emptyList(), fakeElectionStartedListener);
        assertThat(candidate.votes(), is(0));
        candidate.initialize();
        assertThat(candidate.votes(), is(1));
    }

    @Test
    void shouldSendRequestVoteRequests() throws InterruptedException {
        Server follower = new Server(PORT_RANGE);
        follower.start();
        Candidate candidate = new Candidate(List.of(follower.getPort()), fakeElectionStartedListener);
        candidate.initialize();
        Thread.sleep(Duration.ofSeconds(2));
        assertThat(candidate.votes(), is(2));
    }

    /**
     * The follower is only allowed to vote once per term.
     */
    @Test
    void shouldOnlyVoteOncePerTerm() throws InterruptedException {
        Server follower = new Server(PORT_RANGE);
        follower.start();

        Candidate candidate1 = new Candidate(List.of(follower.getPort()), fakeElectionStartedListener);
        Candidate candidate2 = new Candidate(List.of(follower.getPort()), fakeElectionStartedListener);

        initialize(candidate1);
        initialize(candidate2);

        assertThat(candidate2.votes(), is(1));
    }

    // TODO winning an election
    // edge case: both followers transition at the same time to candidate
    @Test
    void shouldWinElection() throws InterruptedException {
        Server follower = new Server(PORT_RANGE, 20L);
        follower.start();

        Server follower2 = new Server(PORT_RANGE, 20L);
        follower2.start();

        Thread.sleep(Duration.ofSeconds(1));

        Candidate candidate = new Candidate(List.of(follower.getPort(), follower2.getPort()), fakeElectionStartedListener);
        initialize(candidate);

        Thread.sleep(Duration.ofSeconds(3));

        assertThat(fakeElectionStartedListener.wonElectionTriggered(), is(true));
    }

    private void initialize(Candidate candidate) throws InterruptedException {
        candidate.initialize();
        Thread.sleep(Duration.ofSeconds(1));
    }
}
