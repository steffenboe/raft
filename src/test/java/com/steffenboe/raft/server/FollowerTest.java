package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FollowerTest {

	private Follower follower;

	@BeforeEach
	void setup() {
		this.follower = new Follower();
	}

	@Test
	void shouldNotProcessMessagesNotFromALeader() throws IOException {
		PrintWriter out = mock(PrintWriter.class);
		BufferedReader in = mock(BufferedReader.class);

		when(in.readLine()).thenReturn("f;appendentry;");

		assertThat(follower.processMessage(in, out), is(false));
	}

	/**
	 * Follower should receive heartbeat from leader.
	 */
	@Test
	void shouldReceiveHeartbeat() throws IOException {
		PrintWriter out = mock(PrintWriter.class);
		BufferedReader in = mock(BufferedReader.class);

		when(in.readLine()).thenReturn("l;appendentry;");

		follower.processMessage(in, out);

		assertThat(follower.receivedHeartbeat(), is(true));
	}

	// TODO should periodically evaluate when lastHeartbeat was received

	// TODO should start election on missing heartbeat
}
