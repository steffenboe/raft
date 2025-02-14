package com.steffenboe.raft;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private static final List<Integer> PORTS = List.of(8080, 8081);

    private Term term;
    private Log log;

    @BeforeEach
    void setup() {
        term = new Term.InMemoryTerm();
        log = new Log.InMemoryLog();
    }

    /**
     * Leader should send heartbeats periodically, keeping the other server in
     * follower state.
     */
    @Test
    void shouldSendHeartbeatPeriodically() throws InterruptedException {
        Server follower = new Server(PORTS, term, log, 3L);
        follower.start();

        Leader leader = new Leader(PORTS, new Log.InMemoryLog());
        leader.initialize();

        Thread.sleep(Duration.ofSeconds(1));
        assertThat(follower.state(), instanceOf(Follower.class));
        Thread.sleep(Duration.ofSeconds(1));
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
            assertThat(log.last().content(), is("new log"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void shouldReplicateLogEntries() throws InterruptedException {
        Server follower = new Server(PORTS, term, new Log.InMemoryLog());
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

    @Test
    void shouldCommit() throws InterruptedException {
        Server follower = new Server(PORTS, term, new Log.InMemoryLog());
        follower.start();
        Leader leader = new Leader(PORTS, log);
        leader.initialize();
        try (var in = new BufferedReader(new StringReader("add;set x 10"));
                var out = new PrintWriter(new StringWriter())) {
            leader.processMessage(in, out);
            Thread.sleep(Duration.ofSeconds(2));
            assertThat(leader.getCommitIndex(), is(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Leader should only commit to its log when a majority of followers have
     * committed and acknowledged.
     */
    @Test
    void shouldNotCommitAsLongMajorityFollowersDidNotConfirm() throws InterruptedException {
        Server follower1 = new Server(PORTS, term, new Log.InMemoryLog());
        follower1.start();
        Server follower2 = new Server(PORTS, term, new Log.InMemoryLog());
        follower2.start();
        Leader leader = new Leader(PORTS, log);
        leader.initialize();
        try (var in = new BufferedReader(new StringReader("add;set x 10"));
                var out = new PrintWriter(new StringWriter())) {
            leader.processMessage(in, out);
            Thread.sleep(Duration.ofSeconds(2));
            assertThat(leader.getCommitIndex(), is(2));
            assertThat(follower1.lastLog(), is("set x 10"));
            assertThat(follower2.lastLog(), is("set x 10"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
