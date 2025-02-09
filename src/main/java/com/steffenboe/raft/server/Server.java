package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Server that listens for incoming requests.
 */
public class Server {

    private final List<Integer> ports;
    private ServerSocket serverSocket;
    private int index = 0;
    private Thread thread;

    private ServerState state;

    private int currentTerm = 1;
    private double electionTimeoutInSeconds = Math.random() * 5;

    private Term term;
    private RaftLog log;

    public Server(List<Integer> ports, Term term, RaftLog log) {
        this.ports = ports;
        this.term = term;
        this.log = log;
    }

    public Server(List<Integer> ports, Term term, RaftLog log, long electionTimeout) {
        this(ports, term, log);
        this.electionTimeoutInSeconds = electionTimeout;
    }

    public void start() {
        currentTerm = term.current();
        this.thread = new Thread(this::listen);
        thread.start();
        transformToFollower();
    }

    private void transformToFollower() {
        System.out.println("Transforming to follower state...");
        this.state = new Follower(this, log, electionTimeoutInSeconds);
        System.out.println("Initializing follower...");
        state.initialize();
    }

    /**
     * Server listens for Client-Connections and responds to requests.
     */
    private void listen() {
        System.out.println("Trying startup on port " + ports.get(index) + "...");
        try (ServerSocket socket = new ServerSocket(ports.get(index))) {
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
            processMessage(clientSocket);
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
        return ports.get(index);
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
        currentTerm = term.increase();
        this.state = new Candidate(this, neighbors(), currentTerm);
        this.state.initialize();
    }

    private List<Integer> neighbors() {
        return ports.stream().filter(port -> !port.equals(ports.get(index))).toList();
    }

    Integer getCurrentTerm() {
        return currentTerm;
    }

    public void onWonElection() {
        this.state = new Leader(neighbors(), log);
        this.state.initialize();
    }

    private void processMessage(Socket clientSocket) {
        try (PrintWriter out = getPrintWriter(clientSocket); BufferedReader in = getBufferedReader(clientSocket);) {
            boolean messageProcessed = state.processMessage(in, out);
            if (!messageProcessed) {
                closeConnection(out, clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection(final PrintWriter out, Socket clientSocket) throws IOException {
        try (clientSocket) {
            out.println("closing connection");
            System.out.println("Closing client connection.");
        }
    }

    public void onLostElection() {
        System.out.println("Lost election, transforming back to follower state.");
        transformToFollower();
    }

    public String lastLog() {
        return log.last();
    }
}
