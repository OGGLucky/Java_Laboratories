package org.example;

import javax.jws.WebService;
import java.awt.*;

@WebService(endpointInterface = "org.example.ServerInterface")
public class Server implements ServerInterface {
    private static final int GRID_SIZE = 5;
    private static int currentPlayer = 1;
    private static int player1Score = 0;
    private static int player2Score = 0;
    private static int connectedPlayers = 0;
    private static boolean gameOver = false;
    private static final int[][] cells = new int[GRID_SIZE - 1][GRID_SIZE - 1];
    private static final Color[][] hLines = new Color[GRID_SIZE][GRID_SIZE - 1];
    private static final Color[][] vLines = new Color[GRID_SIZE - 1][GRID_SIZE];


    @Override
    public synchronized String processMove(String type, int rw, int cl, int playerId) {
        if (gameOver) {
            return buildGameOverResponse();
        }
        if (playerId != currentPlayer) {
            return "error:not_your_turn";
        }

        if (!placeLine(type, rw, cl, playerId)) {
            return "error:invalid_move";
        }

        StringBuilder response = new StringBuilder();
        response.append(buildPlayerMoveResponse(type, rw, cl, playerId));

        if (!verifyCompletedSquares()) {
            switchPlayer();
            response.append(";turn:").append(currentPlayer);
        }

        if (isGameOver()) {
            gameOver = true;
            response.append(";").append(buildGameOverResponse());
        }

        response.append(";").append(getCurrentState());
        return response.toString();
    }

    private String buildGameOverResponse() {
        return "game_over:" + getGameResult();
    }

    private boolean placeLine(String type, int rw, int cl, int playerId) {
        if ("h".equals(type) && hLines[rw][cl] == null) {
            hLines[rw][cl] = getPlayerColor(playerId);
            return true;
        }
        if ("v".equals(type) && vLines[rw][cl] == null) {
            vLines[rw][cl] = getPlayerColor(playerId);
            return true;
        }
        return false;
    }

    private String buildPlayerMoveResponse(String type, int rw, int cl, int playerId) {
        return "player" + playerId + ":" + type + "," + rw + "," + cl;
    }

    private void switchPlayer() {
        currentPlayer = 3 - currentPlayer;
    }

    private Color getPlayerColor(int playerId) {
        return (playerId == 1) ? Color.BLUE : Color.RED;
    }

    private boolean verifyCompletedSquares() {
        boolean isSquareComplete = false;

        for (int rw = 0; rw < GRID_SIZE - 1; rw++) {
            for (int cl = 0; cl < GRID_SIZE - 1; cl++) {
                if (isSquareClosed(rw, cl)) {
                    handleCompletedSquare(rw, cl);
                    isSquareComplete = true;
                }
            }
        }

        return isSquareComplete;
    }

    private boolean isSquareClosed(int rw, int cl) {
        return hLines[rw][cl] != null && hLines[rw + 1][cl] != null &&
                vLines[rw][cl] != null && vLines[rw][cl + 1] != null &&
                cells[rw][cl] == 0;
    }

    private void handleCompletedSquare(int rw, int cl) {
        markSquare(rw, cl);
        updateScore();
    }


    private void markSquare(int rw, int cl) {
        cells[rw][cl] = currentPlayer;
    }

    private void updateScore() {
        if (currentPlayer == 1) player1Score++;
        else player2Score++;
    }

    private boolean isGameOver() {
        for (int[] row : cells) {
            for (int cell : row) {
                if (cell == 0) return false;
            }
        }
        return true;
    }

    private String getGameResult() {
        if (player1Score > player2Score) return "Победа Player1";
        if (player2Score > player1Score) return "Победа Player2";
        return "Ничья";
    }

    @Override
    public String getCurrentState() {
        StringBuilder state = new StringBuilder("score:" + player1Score + "," + player2Score + ";turn:" + currentPlayer);
        for (int rw = 0; rw < GRID_SIZE; rw++) {
            for (int cl = 0; cl < GRID_SIZE - 1; cl++) {
                if (hLines[rw][cl] != null) {
                    state.append(";h:").append(rw).append(",").append(cl).append(",").append(hLines[rw][cl].equals(Color.BLUE) ? "1" : "2");
                }
            }
        }
        for (int rw = 0; rw < GRID_SIZE - 1; rw++) {
            for (int cl = 0; cl < GRID_SIZE; cl++) {
                if (vLines[rw][cl] != null) {
                    state.append(";v:").append(rw).append(",").append(cl).append(",").append(vLines[rw][cl].equals(Color.BLUE) ? "1" : "2");
                }
            }
        }
        if (gameOver) state.append(";game_over:").append(getGameResult());
        return state.toString();
    }

    @Override
    public synchronized String initializeGame(int playerId) {
        if (++connectedPlayers > 2) return "error:too_many_players";
        return "start," + connectedPlayers;
    }
}