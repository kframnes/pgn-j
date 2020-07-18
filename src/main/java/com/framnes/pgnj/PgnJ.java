package com.framnes.pgnj;

import com.framnes.pgnj.engine.Engine;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

/**
 * Main entry point into Pgn-J, a program meant to provide statistical analysis of chess games in order to identify
 * possible cheating over a large sample size.
 */
public class PgnJ {

    public PgnJ(String enginePath, String pgnPath) {

        PgnHolder pgn = new PgnHolder(pgnPath);
        try {
            pgn.loadPgn();
        } catch (Exception e) {
            throw new RuntimeException("There was an issue loading PGN file");
        }

        Engine engine = new Engine(enginePath);
        if (!engine.isReady()) {
            throw new RuntimeException("There was a problem communicating with the engine");
        }

    }

    public static void main(String [] args) {

        String enginePath = System.getProperty("enginePath");
        String pgnPath = System.getProperty("pgnPath");

        new PgnJ(enginePath,pgnPath);

    }

}
