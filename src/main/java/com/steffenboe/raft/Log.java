package com.steffenboe.raft;

import java.util.ArrayList;
import java.util.List;

public interface Log {

    /**
     * Appends a new line to the log.
     * 
     * @param line the line to add
     */
    void append(Line line);

    /**
     * @return the last line from the log
     */
    Line last();

    void commit(int commitIndex);

    class InMemoryLog implements Log {

        private List<Line> lines = new ArrayList<>();

        @Override
        public void append(Line line) {
            if (line != null) {
                lines.add(line);
            } else {
                throw new IllegalArgumentException("line was null or empty");
            }
        }

        @Override
        public Line last() {
            return lines.size() > 0 ? lines.getLast() : new Line(0, "", false);
        }

        @Override
        public void commit(int commitIndex) {
            int index = commitIndex - 1;
            System.out.println("Committing index: " + commitIndex);
            lines.add(index, lines.get(index).withCommitted(true));
            System.out.println("Committed index: " + commitIndex + " successfully.");
        }

    }
}
