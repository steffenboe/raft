package com.steffenboe.raft.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CandidateTest {

    private static final List<Integer> PORT_RANGE = List.of(8080, 8081);
    @Mock
    private Server server;

    private Term term;
    private RaftLog log;

    @BeforeEach
    void startServerThread() {
        term = new Term.InMemoryTerm();
        log = new RaftLog.InMemoryRaftLog();
    }

    /**
     * On initialization, a candidate should vote for itself.
     */
    @Test
    void shouldVoteForSelf() {
        Candidate candidate = new Candidate(server, Collections.emptyList());
        assertThat(candidate.votes(), is(0));
        candidate.initialize();
        assertThat(candidate.votes(), is(1));
    }

    @Test
    void shouldSendRequestVoteRequests() throws InterruptedException {
        Server follower = new Server(PORT_RANGE, term, log);
        System.out.println("Follower started on port " + follower.getPort());
        follower.start();
        Candidate candidate = new Candidate(server, List.of(follower.getPort()));
        System.out.println("Candidate started..");
        candidate.initialize();
        Thread.sleep(Duration.ofSeconds(2));
        assertThat(candidate.votes(), is(2));
    }

    /**
     * The follower is only allowed to vote once per term.
     */
    @Test
    void shouldOnlyVoteOncePerTerm() throws InterruptedException {
        Server follower = new Server(PORT_RANGE, term, log);
        follower.start();

        Candidate candidate1 = new Candidate(server, List.of(follower.getPort()));
        Candidate candidate2 = new Candidate(server, List.of(follower.getPort()));

        initialize(candidate1);
        initialize(candidate2);

        assertThat(candidate2.votes(), is(1));
    }

    // TODO winning an election
    // edge case: both followers transition at the same time to candidate
    @Test
    void shouldWinElection() throws InterruptedException {
        Server follower = new Server(PORT_RANGE, term, log, 20L);
        follower.start();

        Server follower2 = new Server(PORT_RANGE, term, log, 20L);
        follower2.start();

        Thread.sleep(Duration.ofSeconds(2));

        Candidate candidate = new Candidate(server, List.of(follower.getPort(), follower2.getPort()));
        initialize(candidate);

        Thread.sleep(Duration.ofSeconds(3));

        verify(server).onWonElection();
    }

    /**
     * During election, it is possible that a candidate receives an AppendEntry
     * Command from a Server claiming to be leader.
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    void receivesAppendEntryDuringElection() throws InterruptedException, IOException {
        Server server = candidateServer();
        assertThat(server.state(), instanceOf(Candidate.class));
        ServerState candidate = server.state();

        sendMockMessageTo(candidate, "l;appendentry;;2;");

        assertThat(server.state(), instanceOf(Follower.class));
    }

    @Test
    void shouldCompareTermsOnMessage() throws InterruptedException, IOException {
        Server server = candidateServer();
        ServerState candidate = server.state();
        sendMockMessageTo(candidate, "l;appendentry;;0;");

        assertThat(server.state(), instanceOf(Candidate.class));

    }

    private void initialize(Candidate candidate) throws InterruptedException {
        candidate.initialize();
        Thread.sleep(Duration.ofSeconds(1));
    }

    private Server candidateServer() throws InterruptedException {
        Server server = new Server(PORT_RANGE, term, log, 1);
        server.start();
        Thread.sleep(Duration.ofSeconds(2));
        return server;
    }

    private void sendMockMessageTo(ServerState candidate, String message) throws IOException {
        BufferedReader in = mock(BufferedReader.class);
        when(in.readLine()).thenReturn(message);
        PrintWriter out = mock(PrintWriter.class);

        candidate.processMessage(in, out);
    }
}
