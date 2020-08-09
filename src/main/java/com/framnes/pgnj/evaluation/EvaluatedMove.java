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

    public int getPositionEvaluation() {
        return positionEvaluation;
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

    /**
     * Should the stats consider this move for CP loss purposes.
     *
     * We make this determination based on whether the CP difference is negative (if it's positive then the position
     * is mate) and whether or not the difference exceeds our threshhold (currently 1000).
     *
     * @return
     */
    public boolean includeForCpLoss() {
        return getGameMoveEvaluation() - getPositionEvaluation() < 0
                && getGameMoveEvaluation() - getPositionEvaluation() > -1000;
    }

    /**
     * Should the stats consider this move for T[index] purposes.
     *
     * We make this determination by seeing if the move was forced (were there other choices).  So we exclude moves
     * from T1 where there was no second option, from T2 when there was no third option, etc.
     *
     * @param index the engine move index being considered
     * @return true if the move should be used; otherwise false
     */
    public boolean includeForTAnalysis(int index) {
        if (index+1 == engineMoves.length) return true;
        return index+1 < engineMoves.length && engineMoves[index+1] != null;
    }

    public int getCpLoss() {
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

}
