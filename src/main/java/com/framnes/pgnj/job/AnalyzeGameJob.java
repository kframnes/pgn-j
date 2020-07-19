package com.framnes.pgnj.job;

import com.framnes.pgnj.PgnJ;
import com.framnes.pgnj.engine.Engine;
import com.framnes.pgnj.evaluation.EvaluatedMove;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.ArrayList;
import java.util.List;

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
        Board board = new Board();

        boolean analyzeWhite = targetPlayer == null || targetPlayer.isEmpty()
                || targetPlayer.equals(game.getWhitePlayer().getName());

        boolean analyzeBlack = targetPlayer == null || targetPlayer.isEmpty()
                || targetPlayer.equals(game.getBlackPlayer().getName());

        try {

            // This library isn't a fan of \n characters.
            //
            String moveText = game.getMoveText()
                    .toString()
                    .replace('\n', ' ');

            MoveList moves = new MoveList();
            moves.loadFromSan(moveText);

            List<EvaluatedMove> evaluatedMoveList = new ArrayList<>();
            for (int i=0; i<moves.size(); i++) {

                Move move = moves.get(i);
                String fen = moves.getFen(i);

                board.getContext().setStartFEN(fen);

                if (i >= OPENING_MOVES * 2
                        && ((analyzeWhite && Side.WHITE.equals(board.getSideToMove())
                        || (analyzeBlack && Side.BLACK.equals(board.getSideToMove()))))) {

                    EvaluatedMove evaluatedMove = engine.bestMoves(fen);
                    evaluatedMove.setGameMove(move);
                    evaluatedMoveList.add(evaluatedMove);

                }

            }

            // TESTING
            evaluatedMoveList.forEach(EvaluatedMove::printEvaluation);

        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            PgnJ.recordFinished();
        }

    }

}
