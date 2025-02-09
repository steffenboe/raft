package com.steffenboe.raft.server;

import java.util.ArrayList;
import java.util.List;

public interface RaftLog {

    /**
     * Appends a new line to the log.
     * 
     * @param line the line to add
     */
    void append(String line);

    /**
     * @return the last line from the log
     */
    String last();

    class InMemoryRaftLog implements RaftLog {

        private List<String> lines = new ArrayList<>();

        @Override
        public void append(String line) {
            if (line != null) {
                lines.add(line);
            } else {
                throw new IllegalArgumentException("line was null or empty");
            }
        }

        @Override
        public String last() {
            return lines.getLast();
        }

    }
}
