package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServerTest {

    private static Thread serverThread;
    private static final Integer[] PORT_RANGE = {8080, 8081};

    @BeforeAll
    static void startServerThread() {
        Thread.Builder builder = Thread.ofVirtual().name("server-", 0);
        Runnable task = startServerInstance();
        serverThread = builder.start(task);
    }

    @AfterAll
    static void interruptServerThread() {
        serverThread.interrupt();
    }

    private static Runnable startServerInstance() {
        Runnable task = () -> {
            Server server = new Server(PORT_RANGE);
            server.listen();
        };
        return task;
    }

    /**
     * The server should listen to incoming requests and respond.
     */
    @Test
    void shouldListen() throws IOException, InterruptedException {
        try (Socket clientSocket = new Socket("localhost", 8080)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String message = "Hello World";
            out.println(message);
            
            String serverResponse = in.readLine();

            assertThat(serverResponse, is(message));
        }
    }

    /**
     * Server should start on one of 2 available ports.
     */
    @Test
    void shouldRetryWhenPortInUse() throws IOException {
        Thread.Builder builder = Thread.ofVirtual().name("server-", 1);
        Runnable task = startServerInstance();
        serverThread = builder.start(task);

        assertThat(new Socket("localhost", 8081).isConnected(), is(true));
        
        serverThread.interrupt();
    }

}
