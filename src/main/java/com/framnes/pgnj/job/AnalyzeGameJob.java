package com.framnes.pgnj.job;

import com.framnes.pgnj.engine.Engine;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveList;

public class AnalyzeGameJob implements Runnable {

    private final static int OPENING_MOVES = 5;

    private final String enginePath;
    private final String targetPlayer;
    private final Game game;

    public AnalyzeGameJob(String enginePath, String targetPlayer, Game game) {
        this.enginePath = enginePath;
        this.targetPlayer = targetPlayer;
        this.game = game;
    }

    public void describeJob() {
        System.out.println(
            String.format(
                "Analyzing %s - %s ...", game.getWhitePlayer().getName(), game.getBlackPlayer().getName()
            )
        );
    }

    @Override
    public void run() {

        // Initialize engine.
        //
        Engine engine = new Engine(enginePath);
        if (!engine.startNewGame()) {
            throw new RuntimeException("There was a problem communicating with the engine");
        }

        // We're either analyzing white, black or both (when target is blank).
        //
        boolean analyzeWhite = targetPlayer == null
                || targetPlayer.isEmpty()
                || targetPlayer.equals(game.getWhitePlayer().getName());

        boolean analyzeBlack = targetPlayer == null
                || targetPlayer.isEmpty()
                || targetPlayer.equals(game.getBlackPlayer().getName());

        try {

            // This library isn't a fan of \n characters.
            //
            String moveText = game.getMoveText()
                    .toString()
                    .replace('\n', ' ');

            MoveList moves = new MoveList();
            moves.loadFromSan(moveText);

            String[] engineMoves;
            for (int i=0; i<moves.size(); i++) {

                Move move = moves.get(i);
                String fen = moves.getFen(i);

                // Look at the previously calculated engine moves and the actual move that was made
                //

                if (i >= OPENING_MOVES * 2) {
                    engineMoves = engine.bestMoves(fen); // TODO ... work on getting actual best moves (T1, T2, T3)
                    break;
                }

            }

        } catch (MoveConversionException moveConversionException) {
            throw new RuntimeException(moveConversionException);
        }

    }

}
