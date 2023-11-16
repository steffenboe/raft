package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServerTest {

    private static Server server;
    private static final Integer[] PORT_RANGE = {8080, 8081};

    @BeforeAll
    static void startServerThread() {
        server = new Server(PORT_RANGE);
        server.start();
    }

    @AfterAll
    static void interruptServerThread() {
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
        try (Socket clientSocket = new Socket("localhost", 8080)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String message = "f;Hello World";
            out.println(message);
            String response = in.readLine();
            assertThat(response, is("closing connection"));
        } 
    }

    /**
     * Server should start on one of 2 available ports.
     */
    @Test
    void shouldRetryWhenPortInUse() throws IOException {
        Server server = new Server(PORT_RANGE);
        server.start();

        assertThat(new Socket("localhost", 8081).isConnected(), is(true));
    }

}
