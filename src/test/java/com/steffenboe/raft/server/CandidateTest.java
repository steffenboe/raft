package com.steffenboe.raft.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

class CandidateTest {

	/**
	 * On initialization, a candidate should vote for itself.
	 */
	@Test
	void shouldVoteForSelf() {
		Candidate candidate = new Candidate();
		assertThat(candidate.votes(), is(0));
		candidate.initialize();
		assertThat(candidate.votes(), is(1));
	}
}
