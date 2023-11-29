package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Server that listens for incoming requests.
 */
public class Server {

    private final Integer[] ports;
    private ServerSocket serverSocket;
    private int index = 0;
    private Thread thread;

    private ServerState state;

    private int currentTerm = 1;
    private long electionTimeoutInSeconds = 3L;

    public Server(Integer[] ports) {
        this.ports = ports;
    }

    public Server(Integer[] ports, long electionTimeout){
        this(ports);
        this.electionTimeoutInSeconds = electionTimeout;
    }

    public void start() {
        this.thread = new Thread(this::listen);
        thread.start();
        this.state = new Follower(this, electionTimeoutInSeconds);
        state.initialize();
    }

    /**
     * Server listens for Client-Connections and responds to requests.
     */
    private void listen() {
        System.out.println("Trying startup on port " + ports[index] + "...");
        try (ServerSocket socket = new ServerSocket(ports[index])) {
            this.serverSocket = socket;
            System.out.println("Server is up and running on port " + serverSocket.getLocalPort());
            while (true) {
                acceptConnection(serverSocket);
            }
        } catch (BindException e) {
            index++;
            listen();

        } catch (NumberFormatException | IOException e) {
            System.err.println(String.format("Unrecoverable error: %s, shutting down.", e.getMessage()));
            throw new RuntimeException(e);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("No available port found in given list, starting server failed.");
        }
    }

    private void acceptConnection(ServerSocket serverSocket) throws IOException {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connection established.");
        Thread.ofVirtual().start(() -> {
            try (PrintWriter out = getPrintWriter(clientSocket); BufferedReader in = getBufferedReader(clientSocket);) {
                boolean result = state.processMessage(in, out);
                if (!result) {
                    out.println("closing connection");
                    System.out.println("Closing client connection.");
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private BufferedReader getBufferedReader(Socket clientSocket) throws IOException {
        return new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
    }

    private PrintWriter getPrintWriter(Socket clientSocket) throws IOException {
        return new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public int getPort() {
        return ports[index];
    }

    public void stop() {
        thread.interrupt();
        this.state = null;
        this.currentTerm = 0;
        this.index = 0;
        this.serverSocket = null;
    }

    public ServerState state() {
        return state;
    }

    public void onNewElection() {
        System.out.println("New election started, transitioning to candidate state...");
        currentTerm++;
        this.state = new Candidate(this, Arrays.asList(ports).stream().filter(port -> !port.equals(ports[index])).toList());
        this.state.initialize();
    }

    Integer getCurrentTerm() {
        return currentTerm;
    }

    public void onWonElection() {
        this.state = new Leader();
        this.state.initialize();
    }
}
