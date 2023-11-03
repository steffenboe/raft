package com.steffenboe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server that listens for incoming requests.
 */
class Server {

    private int port;

    Server(int port) {
        this.port = port;
    }

    /**
     * Server listens for Client-Connections and responds to requests.
     */
    void listen() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is up and running on port " + serverSocket.getLocalPort());
            while (true) {
                acceptConnection(serverSocket);
            }
        } catch (NumberFormatException | IOException e) {
            System.err.println(String.format("Stopping server, reason: %s", e.getMessage()));
        }
    }

    private void acceptConnection(ServerSocket serverSocket) throws IOException {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connection established.");
        Thread.ofVirtual().start(() -> {
            try (PrintWriter out = getPrintWriter(clientSocket);
                    BufferedReader in = getBufferedReader(clientSocket);) {
                processMessage(out, in);
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

    private void processMessage(PrintWriter out, BufferedReader in) throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received message from client: " + inputLine);
            Message message = new Message(inputLine);
            out.println(message.process());
        }
    }
}
