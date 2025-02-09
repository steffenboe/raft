package com.steffenboe.raft.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerTest {

    private Server server;
    private static final List<Integer> PORT_RANGE = List.of(8080, 8081);

    private Term term;
    private RaftLog log;

    @BeforeEach
    void startServerThread() {
        term = new Term.InMemoryTerm();
        server = new Server(PORT_RANGE, term, log);
        server.start();
    }

    @AfterEach
    void interruptServerThread() {
        server.stop();
    }

    /**
     * The server should listen to incoming requests and respond.
     */
    @Test
    void shouldListen() throws IOException, InterruptedException {
        assertThat(server.state(), instanceOf(Follower.class));
    }

    @Test
    void shouldAbortWhenNotProcessedMessage() throws IOException, InterruptedException {
        SocketConnection socketConnection = new SocketConnection();
        socketConnection.connect(8080);
        socketConnection.send("f;Hello World");
        String response = socketConnection.response();
        assertThat(response, is("closing connection"));
    }

    /**
     * Server should start on one of 2 available ports.
     */
    @Test
    void shouldRetryWhenPortInUse() throws IOException {
        Server secondServer = new Server(PORT_RANGE, term, log);
        secondServer.start();

        assertThat(new Socket("localhost", 8081).isConnected(), is(true));
    }

    @Test
    void shouldStartNewElection() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(6));
        assertThat(server.getCurrentTerm(), is(2));
        assertThat(server.state(), instanceOf(Candidate.class));
    }

}
