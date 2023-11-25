package com.steffenboe.raft.server;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

class CandidateTest {

    private Integer[] PORT_RANGE = {8080, 8081};

    /**
     * On initialization, a candidate should vote for itself.
     */
    @Test
    void shouldVoteForSelf() {
        Candidate candidate = new Candidate(Collections.emptyList());
        assertThat(candidate.votes(), is(0));
        candidate.initialize();
        assertThat(candidate.votes(), is(1));
    }

	@Test
	void shouldSendRequestVoteRequests() throws InterruptedException {
		Server follower = new Server(PORT_RANGE);
		follower.start();
		Candidate candidate = new Candidate(List.of(follower.getPort()));
		candidate.initialize();
		Thread.sleep(Duration.ofSeconds(2));
		assertThat(candidate.votes(), is(2));
	}

	// TODO can not vote twice 
}
