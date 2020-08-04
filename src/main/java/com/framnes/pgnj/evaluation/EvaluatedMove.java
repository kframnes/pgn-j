package com.framnes.pgnj.evaluation;

import com.github.bhlangonijr.chesslib.move.Move;

/**
 * We need to store the positionEvaluation before the move, the move actual made, and the engine moves + evaluations
 * that we determined.
 */
public class EvaluatedMove {

    private EngineMove[] engineMoves;
    private int positionEvaluation;

    private Move gameMove;
    private int gameMoveEvaluation;

    public EvaluatedMove(EngineMove[] engineMoves) {
        this.engineMoves = engineMoves;
        this.positionEvaluation = engineMoves[0] != null ? engineMoves[0].getEvaluation() : 0;
    }

    public EngineMove[] getEngineMoves() {
        return engineMoves;
    }

    public int getPositionEvaluation() {
        return positionEvaluation;
    }

    public Move getGameMove() {
        return gameMove;
    }

    public void setGameMove(Move gameMove) {
        this.gameMove = gameMove;
    }

    public Integer getGameMoveEvaluation() {
        return gameMoveEvaluation;
    }

    public void setGameMoveEvaluation(Integer gameMoveEvaluation) {
        this.gameMoveEvaluation = gameMoveEvaluation;
    }

    public int getCpLoss() {
        // Integer.MAX_VALUE == getGameMoveEvaluation() means the position was mate.
        return getPositionEvaluation() < getGameMoveEvaluation() ? 0 : getGameMoveEvaluation() - getPositionEvaluation();
    }

    public int getEngineMatchIndex() {

        for (int i=0; i<engineMoves.length; i++) {
            if (engineMoves[i].getMove().equals(gameMove.toString())) {
                return i;
            }
        }
        return -1;

    }

    public void printEvaluation() {
        System.out.println(String.format("[%d] %s / %s --> %s [ %s | %s | %s ]",
                positionEvaluation,
                gameMove.getSan(),
                gameMove.toString(),
                gameMoveEvaluation,
                engineMoves[0] != null ? engineMoves[0].getMove() : "--",
                engineMoves[1] != null ? engineMoves[1].getMove() : "--",
                engineMoves[2] != null ? engineMoves[2].getMove() : "--"));
    }

}
