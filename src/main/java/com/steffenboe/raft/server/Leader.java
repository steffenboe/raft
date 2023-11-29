/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;

class Leader implements ServerState {

    private final List<Integer> neighbors;

    Leader(List<Integer> neighbors) {
        this.neighbors = neighbors;
    }

    @Override
    public void initialize() {
        neighbors.forEach(port -> {
            Thread.ofVirtual().start(() -> {
                try {
                    while (true) {
                        Message message = new Message("l;appendentry;");
                        message.send(port);
                        Thread.sleep(Duration.ofSeconds(1));
                    }
                } catch (InterruptedException | IOException ex) {
                    System.err.println(ex);
                };
            });
        });
    }

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        return false;
    }

}
