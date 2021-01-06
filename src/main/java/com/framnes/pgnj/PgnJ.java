package com.framnes.pgnj;

import com.framnes.pgnj.job.AnalyzeGameJob;
import com.framnes.pgnj.stats.Stats;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public static AtomicInteger PROGRESS = new AtomicInteger(0);

    private final ExecutorService executor;

    private final String targetPlayer;
    private final String enginePath;
    private final List<PgnHolder> games;

    public PgnJ(String enginePath, String pgnPath, String targetPlayer) {

        this.games = new ArrayList<>();
        this.targetPlayer = targetPlayer;
        this.enginePath = enginePath;

        if (!pgnPath.endsWith("/")) {
            pgnPath += "/";
        }

        File targetFile = new File(pgnPath+targetPlayer+".pgn");
        File targetDirectory = new File(pgnPath+targetPlayer);

        if (targetFile.exists()) {
            System.out.println("Processing file at: " + targetFile.getAbsolutePath());
            System.out.println();
            this.games.add(new PgnHolder(pgnPath + targetPlayer + ".pgn"));
        } else if (targetDirectory.exists() && targetDirectory.isDirectory()) {
            System.out.println("Processing directory at: " + targetDirectory.getAbsolutePath());
            System.out.println();
            File[] pgns = targetDirectory.listFiles((file) -> file.getName().endsWith(".pgn"));
            for (File pgn : pgns) {
                this.games.add(new PgnHolder(pgn.getAbsolutePath()));
            }
        }

        try {
            COUNT = new AtomicInteger(0);
            for (PgnHolder pgnHolder: games) {
                pgnHolder.loadPgn();
                COUNT.getAndAdd(pgnHolder.getGame().size());
            }
        } catch (Exception e) {
            throw new RuntimeException("There was an issue loading PGN file");
        }

        // Initialize executor service
        //
        executor = Executors.newFixedThreadPool(4);

    }

    /**
     * Using the defined engine, analyze the target players games from PGN file.
     */
    public void analyze(Stats stats) {
        System.out.println("Analyzing " + COUNT.get() + " games...");
        games.stream().flatMap((pgnHolder -> pgnHolder.getGame().stream()))
                .map((game) -> new AnalyzeGameJob(enginePath, targetPlayer, game, stats))
                .forEach(executor::submit);
    }

    /**
     * In order to know when to stop running we count how many games were given to us for analysis and after each game
     * has completed we have it decrement a counter.  Once we reach 0 we release the lock.
     */
    public static void recordFinished() {
        if (COUNT.decrementAndGet() == 0) {
            synchronized (LOCK) {
                LOCK.notifyAll();
            }
        }
    }

    /**
     * Shut down the executor.
     */
    public void destroy() {
        executor.shutdown();
        executor.shutdownNow();
    }

    public static void main(String [] args) {

        long start = new Date().getTime();

        String enginePath = System.getProperty("enginePath");
        String pgnPath = System.getProperty("pgnPath");
        String targetPlayer = System.getProperty("player");

        PgnJ pgnJ = new PgnJ(enginePath, pgnPath, targetPlayer);
        Stats stats = new Stats(3);

        pgnJ.analyze(stats);

        synchronized (LOCK) {
            try {
                LOCK.wait();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        long finish = new Date().getTime();

        stats.addTiming(finish - start);
        stats.outputEvaluation();

        pgnJ.destroy();

    }

}
