package com.steffenboe.raft;

public interface Term {
    
    /**
     * @return Get the current term.
     */
    int current();

    /**
     * Increase the current term by one.
     * @return the updated term
     */
    int increase();

    class InMemoryTerm implements Term {

        private int currentTerm = 1;

        @Override
        public int current() {
            return currentTerm;
        }

        @Override
        public int increase() {
            currentTerm++;
            return currentTerm;
        }

    }

}
