package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class Client extends JFrame {
    private static final int GRID_SIZE = 5;
    private static final int INDENTATION = 100;
    private static final int PANEL_WIDTH = 600;
    private static final int PANEL_HEIGHT = 500;
    private static final Color BACKGROUND_COLOR = new Color(213, 181, 156);
    private static final Color LINES_COLOR = new Color(0, 0, 0, 128);

    private ServerInterface server;
    private int playerId;
    private int crntPlayer = 1;

    private final Color[][] hLines = new Color[GRID_SIZE][GRID_SIZE - 1];
    private final Color[][] vLines = new Color[GRID_SIZE - 1][GRID_SIZE];
    private final JLabel LabelPlr;
    private final JLabel Player1LabelScr;
    private final JLabel Player2LabelScr;
    private final JLabel turnLabel;
    private final GamePanel gamePanel;

    public Client(String serverAddress) {
        Font customFont = new Font("Serif", Font.BOLD, 18);
        LabelPlr = new JLabel("Player: ");
        LabelPlr.setFont(customFont);
        Player1LabelScr = new JLabel("0");
        Player1LabelScr.setFont(customFont);
        Player1LabelScr.setForeground(Color.BLUE);
        Player2LabelScr = new JLabel("0");
        Player2LabelScr.setFont(customFont);
        Player2LabelScr.setForeground(Color.RED);
        turnLabel = new JLabel("Turn: Player 1");
        turnLabel.setFont(customFont);

        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        gamePanel.setBackground(BACKGROUND_COLOR);

        try {
            URL url = new URL("http://localhost:8080/ws/game?wsdl");
            QName qname = new QName("http://example.org/", "ServerService");
            Service service = Service.create(url, qname);
            server = service.getPort(ServerInterface.class);

            String initResponse = server.initializeGame(1);
            if (initResponse.startsWith("start")) {
                playerId = Integer.parseInt(initResponse.split(",")[1]);
                SwingUtilities.invokeLater(() -> LabelPlr.setText("Player: " + (playerId == 1 ? "Player1" : "Player2")));
            } else if (initResponse.startsWith("error")) {
                JOptionPane.showMessageDialog(this, "Too many players connected");
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Dots and Boxes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (j < GRID_SIZE - 1) hLines[i][j] = LINES_COLOR;
                if (i < GRID_SIZE - 1) vLines[i][j] = LINES_COLOR;
            }
        }

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(LabelPlr);
        infoPanel.add(new JLabel("Score: "));
        infoPanel.add(Player1LabelScr);
        infoPanel.add(Player2LabelScr);
        infoPanel.add(turnLabel);

        add(infoPanel, BorderLayout.EAST);
        add(gamePanel, BorderLayout.CENTER);

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (playerId != crntPlayer) {
                    return;
                }

                int x = event.getX() - INDENTATION;
                int y = event.getY() - INDENTATION;
                int fieldSize = (PANEL_WIDTH - 2 * INDENTATION) / GRID_SIZE;

                if (x < 0) {
                    return;
                }
                if (y < 0) {
                    return;
                }
                if (x >= GRID_SIZE * fieldSize) {
                    return;
                }
                if (y >= GRID_SIZE * fieldSize) {
                    return;
                }

                int row = y / fieldSize;
                int col = x / fieldSize;
                int dx = x % fieldSize;
                int dy = y % fieldSize;

                if (tryClickHorizontal(row, col, dx, dy, fieldSize) ||
                        tryClickVertical(row, col, dx, dy, fieldSize)) {
                    gamePanel.repaint();
                }
            }

            private boolean tryClickHorizontal(int row, int col, int dx, int dy, int fieldSize) {
                if (dy < 10 && row < GRID_SIZE && col < GRID_SIZE - 1) {
                    return processLineClick(hLines, row, col, "h");
                } else if (dy > fieldSize - 10 && row < GRID_SIZE - 1 && col < GRID_SIZE - 1) {
                    return processLineClick(hLines, row + 1, col, "h");
                }
                return false;
            }

            private boolean tryClickVertical(int row, int col, int dx, int dy, int fieldSize) {
                if (dx < 10 && row < GRID_SIZE - 1 && col < GRID_SIZE) {
                    return processLineClick(vLines, row, col, "v");
                } else if (dx > fieldSize - 10 && row < GRID_SIZE - 1 && col < GRID_SIZE - 1) {
                    return processLineClick(vLines, row, col + 1, "v");
                }
                return false;
            }

            private boolean processLineClick(Color[][] lines, int row, int col, String orientation) {
                if (lines[row][col].equals(LINES_COLOR)) {
                    lines[row][col] = (playerId == 1) ? Color.BLUE : Color.RED;
                    processMove(orientation, row, col);
                    return true;
                }
                return false;
            }
        });

        pack();
        setVisible(true);
        new Thread(new ServerListener()).start();
    }

    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int totalSize = PANEL_WIDTH - 2 * INDENTATION;
            int fieldSize = totalSize / GRID_SIZE;

            drawGridDots(g, fieldSize);
            drawHorizontalLines(g, fieldSize);
            drawVerticalLines(g, fieldSize);
        }

        private void drawGridDots(Graphics g, int fieldSize) {
            g.setColor(Color.BLACK);
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    int x = INDENTATION + j * fieldSize - 5;
                    int y = INDENTATION + i * fieldSize - 5;
                    g.fillOval(x, y, 10, 10);
                }
            }
        }

        private void drawHorizontalLines(Graphics g, int fieldSize) {
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE - 1; j++) {
                    g.setColor(hLines[i][j]);
                    int x = INDENTATION + j * fieldSize;
                    int y = INDENTATION + i * fieldSize - 5;
                    g.fillRect(x, y, fieldSize, 10);
                }
            }
        }

        private void drawVerticalLines(Graphics g, int fieldSize) {
            for (int i = 0; i < GRID_SIZE - 1; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    g.setColor(vLines[i][j]);
                    int x = INDENTATION + j * fieldSize - 5;
                    int y = INDENTATION + i * fieldSize;
                    g.fillRect(x, y, 10, fieldSize);
                }
            }
        }
    }

    private void processMove(String type, int rw, int cl) {
        try {
            String response = server.processMove(type, rw, cl, playerId);
            if (response.startsWith("error")) {
                JOptionPane.showMessageDialog(this, "Invalid move");
                return;
            }

            String[] parts = response.split(";");
            for (String part : parts) {
                handleResponsePart(part);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleResponsePart(String part) {
        if (part.startsWith("player")) {
            handlePlayerMove(part);
        } else if (part.startsWith("turn")) {
            updateTurn(part);
        } else if (part.startsWith("score")) {
            updateScore(part);
        } else if (part.startsWith("h")) {
            updateHorizontalLine(part);
        } else if (part.startsWith("v")) {
            updateVerticalLine(part);
        } else if (part.startsWith("game_over")) {
            handleGameOver(part);
        }
    }

    private void handlePlayerMove(String part) {
        String[] tokens = part.split(":")[1].split(",");
        String moveType = tokens[0];
        int row = Integer.parseInt(tokens[1]);
        int col = Integer.parseInt(tokens[2]);

        if (moveType.equals("h")) {
            hLines[row][col] = getCurrentPlayerColor();
        } else if (moveType.equals("v")) {
            vLines[row][col] = getCurrentPlayerColor();
        }
    }

    private void updateTurn(String part) {
        crntPlayer = Integer.parseInt(part.split(":")[1]);
        SwingUtilities.invokeLater(() -> turnLabel.setText("Turn: Player " + crntPlayer));
    }

    private void updateScore(String part) {
        String[] scores = part.split(":")[1].split(",");
        SwingUtilities.invokeLater(() -> {
            Player1LabelScr.setText(scores[0]);
            Player2LabelScr.setText(scores[1]);
        });
    }

    private void updateHorizontalLine(String part) {
        String[] tokens = part.split(":")[1].split(",");
        int row = Integer.parseInt(tokens[0]);
        int col = Integer.parseInt(tokens[1]);
        int player = Integer.parseInt(tokens[2]);
        hLines[row][col] = getPlayerColor(player);
    }

    private void updateVerticalLine(String part) {
        String[] tokens = part.split(":")[1].split(",");
        int row = Integer.parseInt(tokens[0]);
        int col = Integer.parseInt(tokens[1]);
        int player = Integer.parseInt(tokens[2]);
        vLines[row][col] = getPlayerColor(player);
    }

    private void handleGameOver(String part) {
        String result = part.split(":")[1];
        JOptionPane.showOptionDialog(this, result, "Game Over", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, new Object[]{"OK"}, "OK");
        System.exit(0);
    }

    private Color getCurrentPlayerColor() {
        return (crntPlayer == 1) ? Color.BLUE : Color.RED;
    }

    private Color getPlayerColor(int player) {
        return (player == 1) ? Color.BLUE : Color.RED;
    }


    private class ServerListener implements Runnable {
        public void run() {
            try {
                while (true) {
                    String state = server.getCurrentState();
                    String[] parts = state.split(";");
                    boolean needRepaint = false;

                    for (String part : parts) {
                        if (part.startsWith("score")) {
                            String[] scores = part.split(":")[1].split(",");
                            SwingUtilities.invokeLater(() -> {
                                Player1LabelScr.setText(scores[0]);
                                Player2LabelScr.setText(scores[1]);
                            });
                            needRepaint = true;
                        } else if (part.startsWith("turn")) {
                            crntPlayer = Integer.parseInt(part.split(":")[1]);
                            SwingUtilities.invokeLater(() -> turnLabel.setText("Turn: Player " + crntPlayer));
                            needRepaint = true;
                        } else if (part.startsWith("h")) {
                            String[] tokens = part.split(":")[1].split(",");
                            int row = Integer.parseInt(tokens[0]);
                            int col = Integer.parseInt(tokens[1]);
                            int player = Integer.parseInt(tokens[2]);
                            hLines[row][col] = (player == 1) ? Color.BLUE : Color.RED;
                            needRepaint = true;
                        } else if (part.startsWith("v")) {
                            String[] tokens = part.split(":")[1].split(",");
                            int row = Integer.parseInt(tokens[0]);
                            int col = Integer.parseInt(tokens[1]);
                            int player = Integer.parseInt(tokens[2]);
                            vLines[row][col] = (player == 1) ? Color.BLUE : Color.RED;
                            needRepaint = true;
                        } else if (part.startsWith("game_over")) {
                            String result = part.split(":")[1];
                            JOptionPane.showMessageDialog(Client.this, result);
                            System.exit(0);
                        }
                    }

                    if (needRepaint) repaint();

                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Client("localhost");
    }
}