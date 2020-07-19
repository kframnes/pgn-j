package com.framnes.pgnj;

import com.framnes.pgnj.job.AnalyzeGameJob;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main entry point into Pgn-J, a program meant to provide statistical analysis of chess games in order to identify
 * possible cheating over a large sample size.
 */
public class PgnJ {

    public final static Object LOCK = new Object();
    public static AtomicInteger COUNT;

    private final ExecutorService executor;

    private final String targetPlayer;
    private final String enginePath;
    private final PgnHolder games;

    public PgnJ(String enginePath, String pgnPath, String targetPlayer) {

        this.targetPlayer = targetPlayer;
        this.enginePath = enginePath;

        // Load PGN of games
        //
        this.games = new PgnHolder(pgnPath);
        try {
            games.loadPgn();
            COUNT = new AtomicInteger(games.getSize());
        } catch (Exception e) {
            throw new RuntimeException("There was an issue loading PGN file");
        }

        // Initialize executor service
        //
        executor = Executors.newFixedThreadPool(5);

    }

    /**
     * Using the defined engine, analyze the target players games from PGN file.
     */
    public void analyze() {

        games.getGame().stream()
                .map((game) -> new AnalyzeGameJob(enginePath, targetPlayer, game))
                .peek(AnalyzeGameJob::describeJob)
                .forEach(executor::submit);

    }

    /**
     *
     */
    public static void recordFinished() {
        if (COUNT.decrementAndGet() == 0) {
            synchronized (LOCK) {
                LOCK.notifyAll();
            }
        }
    }

    /**
     * Output report on target players accuracy.
     */
    public void report() {

    }

    public void destroy() {
        executor.shutdown();
        executor.shutdownNow();
    }

    public static void main(String [] args) {

        String enginePath = System.getProperty("enginePath");
        String pgnPath = System.getProperty("pgnPath");
        String targetPlayer = "keithframnes";

        PgnJ pgnJ = new PgnJ(enginePath, pgnPath, targetPlayer);
        pgnJ.analyze();

        synchronized (LOCK) {
            try {
                LOCK.wait();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        pgnJ.destroy();

    }

}
