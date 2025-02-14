package com.steffenboe.raft;

record Line(int commitIndex, String content, boolean committed) {

    public Line withCommitted(boolean committed) {
        return new Line(commitIndex, content, committed);
    }
}
