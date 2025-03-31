package org.example;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class Server {
    private static final int PORT = 12345;
    private static final List<PlayerHandler> players = new ArrayList<>();
    private static final int MAX_PLAYERS = 2;
    private static final int GRID_SIZE = 5;
    private static final int[][] cells = new int[GRID_SIZE - 1][GRID_SIZE - 1];

    private static final Color[][] hLines = new Color[GRID_SIZE][GRID_SIZE - 1];
    private static final Color[][] vLines = new Color[GRID_SIZE - 1][GRID_SIZE];

    private static int currentPlayer = 1;
    private static int player1Score = 0;
    private static int player2Score = 0;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");
            while (players.size() < MAX_PLAYERS) {
                Socket playerSocket = serverSocket.accept();
                PlayerHandler playerHandler = new PlayerHandler(playerSocket, players.size() + 1);
                players.add(playerHandler);
                new Thread(playerHandler).start();
            }

            System.out.println("The game is on");
            players.get(0).transmitMessage("start,1");
            players.get(1).transmitMessage("start,2");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void processMove(String type, int rw, int cl, int playerId) {
        if (playerId != currentPlayer) {
            return;
        }
        
        boolean linePlaced = false;
        
        if (type.equals("h") && hLines[rw][cl] == null) {
            hLines[rw][cl] = (playerId == 1) ? Color.BLUE : Color.RED;
            linePlaced = true;
            
        } else if (type.equals("v") && vLines[rw][cl] == null) {
            vLines[rw][cl] = (playerId == 1) ? Color.BLUE : Color.RED;
            linePlaced = true;
        }
        
        if (linePlaced) {
            sendToAll("player" + playerId + ":" + type + "," + rw + "," + cl);
            boolean completedSquare = verifyCompletedSquares();
            if (!completedSquare) {
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
                sendToAll("turn:" + currentPlayer);
            }
            determineEndGameCondition();
        }
    }

    private static boolean verifyCompletedSquares() {
        boolean isSquareComplete = false;

        for (int rw = 0; rw < GRID_SIZE - 1; rw++) {
            for (int cl = 0; cl < GRID_SIZE - 1; cl++) {
                if (isSquareClosed(rw, cl)) {
                    markSquare(rw, cl);
                    updateScore();
                    notifyScores();
                    isSquareComplete = true;
                }
            }
        }
        return isSquareComplete;
    }

    private static boolean isSquareClosed(int rw, int cl) {
        return hLines[rw][cl] != null && hLines[rw + 1][cl] != null &&
                vLines[rw][cl] != null && vLines[rw][cl + 1] != null &&
                cells[rw][cl] == 0;
    }

    private static void markSquare(int rw, int cl) {
        cells[rw][cl] = currentPlayer;
    }

    private static void updateScore() {
        if (currentPlayer == 1) {
            player1Score++;
        } else {
            player2Score++;
        }
    }

    private static void notifyScores() {
        sendToAll("score:" + player1Score + "," + player2Score);
    }


    private static void determineEndGameCondition() {
        int cellCount = (GRID_SIZE - 1) * (GRID_SIZE - 1);
        if (player1Score + player2Score >= cellCount) {
            String endMessage;
            if (player1Score > player2Score) {
                endMessage = "Player 1 is victorious!";
            } else if (player2Score > player1Score) {
                endMessage = "Victory for Player 2!";
            } else {
                endMessage = "The game ends in a tie!";
            }
            sendToAll("game_over:" + endMessage);
        }
    }

    static void sendToAll(String msgContent) {
        players.forEach(client -> client.transmitMessage(msgContent));
    }

    static class PlayerHandler implements Runnable {
        private final Socket connection;
        private PrintWriter outputWriter;
        private BufferedReader inputReader;
        private final int playerIdentifier;

        public PlayerHandler(Socket connection, int playerIdentifier) {
            this.connection = connection;
            this.playerIdentifier = playerIdentifier;

            try {
                outputWriter = new PrintWriter(connection.getOutputStream(), true);
                inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void transmitMessage(String msgContent) {
            outputWriter.println(msgContent);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String incomingMsg = inputReader.readLine();
                    if (incomingMsg == null) break;
                    if (incomingMsg.startsWith("move")) {
                        handleIncomingMove(incomingMsg.split(","));
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                closeConnection();
            }
        }

        private void handleIncomingMove(String[] messageParts) {
            if (messageParts.length < 4) return;
            String moveType = messageParts[1];
            int rowPosition = Integer.parseInt(messageParts[2]);
            int columnPosition = Integer.parseInt(messageParts[3]);
            processMove(moveType, rowPosition, columnPosition, playerIdentifier);
        }

        private void closeConnection() {
            try {
                connection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}