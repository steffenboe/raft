package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FromFileTerm implements Term {

    private final String termFile = "currentTerm.txt";

    @Override
    public int current() {
        try (BufferedReader reader = new BufferedReader(new FileReader(termFile))) {
            String termString = reader.readLine();
            if (termString != null) {
                return Integer.parseInt(termString);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load term: " + e.getMessage());
        }
        return 1;
    }

    @Override
    public int increase() {
        int updatedTerm = current() + 1;
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter(termFile))) {
                writer.println(updatedTerm);
            }
        } catch (IOException e) {
            System.err.println("Failed to save term: " + e.getMessage());
        }
        return updatedTerm;
    }
    
}
