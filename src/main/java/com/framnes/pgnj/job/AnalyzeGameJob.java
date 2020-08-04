package com.framnes.pgnj.job;

import com.framnes.pgnj.PgnJ;
import com.framnes.pgnj.engine.Engine;
import com.framnes.pgnj.evaluation.EvaluatedMove;
import com.framnes.pgnj.stats.Stats;
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
    private final Side side;

    private final Stats stats;

    public AnalyzeGameJob(String enginePath, String targetPlayer, Game game, Stats stats) {
        this.enginePath = enginePath;
        this.targetPlayer = targetPlayer;
        this.game = game;

        this.stats = stats;

        if (targetPlayer.equals(game.getWhitePlayer().getName())) {
            this.side = Side.WHITE;
        } else if (targetPlayer.equals(game.getBlackPlayer().getName())) {
            this.side = Side.BLACK;
        } else {
            side = null;
        }

    }

    @Override
    public void run() {

        System.out.println(
            String.format(
                    "Analyzing %s - %s [%s]", game.getWhitePlayer().getName(), game.getBlackPlayer().getName(),
                    side != null ? side.name() : "BOTH"
            )
        );

        // Initialize engine.
        //
        Engine engine = new Engine(enginePath);
        if (!engine.startNewGame()) {
            throw new RuntimeException("There was a problem communicating with the engine");
        }

        try {

            // This library isn't a fan of \n characters.
            //
            String moveText = game.getMoveText()
                    .toString()
                    .replace('\n', ' ');

            MoveList moves = new MoveList();
            moves.loadFromSan(moveText);

            // Evaluate moves.
            //
            List<EvaluatedMove> evaluatedMoveList = new ArrayList<>();
            for (int i=0; i<moves.size(); i++) {

                String fen = i==0 ? moves.getStartFen() : moves.getFen(i);
                Move move = moves.get(i);

                EvaluatedMove evaluatedMove = engine.bestMoves(fen);
                evaluatedMove.setGameMove(move);

                if (evaluatedMoveList.size() > 0) {
                    EvaluatedMove lastMove = evaluatedMoveList.get(evaluatedMoveList.size()-1);
                    lastMove.setGameMoveEvaluation(evaluatedMove.getPositionEvaluation() * -1); // flip perspective
                }

                evaluatedMoveList.add(evaluatedMove);

            }

            // Evaluate final position.
            //
            String finalFen = moves.getFen(moves.size());
            EvaluatedMove evaluatedMove = engine.bestMoves(finalFen);
            if (evaluatedMoveList.size() > 1) {
                EvaluatedMove lastMove = evaluatedMoveList.get(evaluatedMoveList.size()-1);
                lastMove.setGameMoveEvaluation(evaluatedMove.getPositionEvaluation());
            }

            // Add analysis to Stats object.
            //
            stats.addEvaluatedMoves(evaluatedMoveList, side, OPENING_MOVES);
            //evaluatedMoveList.forEach(EvaluatedMove::printEvaluation);

        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            PgnJ.recordFinished();
        }

    }

}
