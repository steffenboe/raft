package com.steffenboe.raft.server;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import org.junit.jupiter.api.Test;

class LeaderTest {

    private static final List<Integer> PORTS = List.of(8080);

    /**
     * Leader should send heartbeats periodically, keeping the other server in
     * follower state.
     */
    @Test
    void shouldSendHeartbeat() throws InterruptedException {
        Server follower = new Server(PORTS, 3L);
        follower.start();

        Leader leader = new Leader(PORTS);
        leader.initialize();

        Thread.sleep(Duration.ofSeconds(5));

        assertThat(follower.state(), instanceOf(Follower.class));
    }

	// TODO heartbeat period configurable
}
