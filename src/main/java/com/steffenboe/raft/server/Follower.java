/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.steffenboe.raft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


class Follower implements ServerState {

    @Override
    public boolean processMessage(BufferedReader in, PrintWriter out) throws IOException {
        return false;
    }
}
