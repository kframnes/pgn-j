package com.framnes.pgnj;

import com.framnes.pgnj.engine.Engine;

/**
 * Main entry point into Pgn-J, a program meant to provide statistical analysis of chess games in order to identify
 * possible cheating over a large sample size.
 */
public class PgnJ {

    public PgnJ(String enginePath) {

        Engine engine = new Engine(enginePath);
        if (!engine.engineIsValid()) {
            throw new RuntimeException("Engine path is not properly configured");
        }

    }

    public static void main(String [] args) {

        String enginePath = System.getProperty("enginePath");

        new PgnJ(enginePath);

    }

}
