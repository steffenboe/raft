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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FollowerTest {

	private Follower follower;

	@BeforeEach
	public void setup() {
		this.follower = new Follower();
	}

	@Test
	public void shouldNotProcessMessagesNotFromALeader() throws IOException {
		PrintWriter out = mock(PrintWriter.class);
		BufferedReader in = mock(BufferedReader.class);
		assertThat(follower.processMessage(in, out), is(false));
	}
}
