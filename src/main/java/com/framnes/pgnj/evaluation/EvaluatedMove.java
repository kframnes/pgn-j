package com.framnes.pgnj.evaluation;

import com.github.bhlangonijr.chesslib.move.Move;

/**
 * We need to store the evaluation before the move, the move actual made, and the engine moves + evaluations
 * that we determined.
 */
public class EvaluatedMove {

    private EngineMove[] engineMoves;
    private int evaluation;
    private Move gameMove;

    public EvaluatedMove(EngineMove[] engineMoves) {
        this.engineMoves = engineMoves;
        this.evaluation = engineMoves[0].getEvaluation();
    }

    public EngineMove[] getEngineMoves() {
        return engineMoves;
    }

    public int getEvaluation() {
        return evaluation;
    }

    public Move getGameMove() {
        return gameMove;
    }

    public void setGameMove(Move gameMove) {
        this.gameMove = gameMove;
    }

    public void printEvaluation() {
        System.out.println(String.format("[%d] %s / %s [%s | %s | %s | ... ]",
                evaluation,
                gameMove.getSan(),
                gameMove.toString(),
                engineMoves[0] != null ? engineMoves[0].getMove() : "--",
                engineMoves[1] != null ? engineMoves[1].getMove() : "--",
                engineMoves[2] != null ? engineMoves[2].getMove() : "--"));
    }

}
