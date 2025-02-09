package com.steffenboe.raft.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LeaderTest {

    private static final List<Integer> PORTS = List.of(8080);

    private Term term;
    private RaftLog log;

    @BeforeEach
    void setup() {
        term = new Term.InMemoryTerm();
        log = new RaftLog.InMemoryRaftLog();
    }

    /**
     * Leader should send heartbeats periodically, keeping the other server in
     * follower state.
     */
    @Test
    void shouldSendHeartbeat() throws InterruptedException {
        Server follower = new Server(PORTS, term, log, 3L);
        follower.start();

        Leader leader = new Leader(PORTS, new RaftLog.InMemoryRaftLog());
        leader.initialize();

        Thread.sleep(Duration.ofSeconds(5));

        assertThat(follower.state(), instanceOf(Follower.class));
    }

    /**
     * Leader should process a command, by replicating it to all followers.
     */
    @Test
    void shouldProcessCommand() {
        Leader leader = new Leader(PORTS, log);
        try (var in = new BufferedReader(new StringReader("add;new log"));
                var out = new PrintWriter(new StringWriter())) {
            leader.processMessage(in, out);
            assertThat(log.last(), is("new log"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void shouldReplicateLogEntries() throws InterruptedException {
        Server follower = new Server(PORTS, term, new RaftLog.InMemoryRaftLog());
        follower.start();
        Leader leader = new Leader(PORTS, log);
        leader.initialize();
        try (var in = new BufferedReader(new StringReader("add;new log"));
                var out = new PrintWriter(new StringWriter())) {
            leader.processMessage(in, out);
            Thread.sleep(100);
            assertThat(follower.lastLog(), is("new log"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // TODO heartbeat period configurable
}
