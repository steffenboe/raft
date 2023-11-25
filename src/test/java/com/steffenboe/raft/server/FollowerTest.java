package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;

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
    private ElectionTimeoutListener.FakeElectionTimeoutListener fakeElectionTimeoutListener;

    @BeforeEach
    void setup() {
		this.fakeElectionTimeoutListener = new ElectionTimeoutListener.FakeElectionTimeoutListener();
        this.follower = new Follower(fakeElectionTimeoutListener);
		this.follower.initialize();
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
        receiveHeartbeat();
		assertThat(fakeElectionTimeoutListener.gotInvoked(), is(false));
    }

    @Test
    void shouldNotifyOnHeartbeatTimeout() throws InterruptedException {
        waitFor5Seconds();
        assertThat(fakeElectionTimeoutListener.gotInvoked(), is(true));
    }

	@Test
	void shouldResetReceivedHeartbeat() throws IOException, InterruptedException {
		receiveHeartbeat();
		waitFor5Seconds();
		assertThat(fakeElectionTimeoutListener.gotInvoked(), is(true));
	}

	// TODO shouldSyncTermsOnHeartbeat


    private void receiveHeartbeat() throws IOException {
        PrintWriter out = mock(PrintWriter.class);
        BufferedReader in = mock(BufferedReader.class);

        when(in.readLine()).thenReturn("l;appendentry;");

        follower.processMessage(in, out);
    }

    private void waitFor5Seconds() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(5));
    }
}
