package com.steffenboe.raft;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.steffenboe.Main;

class ServerTest {

    private static Thread serverThread;

    @BeforeAll
    static void startServerThread(){
        Thread.Builder builder = Thread.ofVirtual().name("server-", 0);
        Runnable task = () -> {
            String[] args = new String[1];
            args[0] = "8080";
            Main.main(args);
        };
        serverThread = builder.start(task);
    }

    @AfterAll
    static void interruptServerThread() {
        serverThread.interrupt();
    }

    /**
     * The server should listen to incoming requests and respond.
     */
    @Test
    void shouldListen() throws IOException, InterruptedException {
        Socket clientSocket = new Socket("localhost", 8080);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        String message = "Hello World";
        out.println(message);
        
        String serverResponse = in.readLine();

        assertThat(serverResponse, is(message));
        clientSocket.close();      
    }

    

}
