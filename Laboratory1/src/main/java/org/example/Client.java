package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {
    private static final int PORT = 12345;
    private static final int GRID_SIZE = 5;
    private static final int INDENTATION = 100;
    private static final Color BACKGROUND_COLOR = new Color(213, 181, 156);
    private static final Color LINES_COLOR = new Color(0, 0, 0, 128);

    private PrintWriter out;
    private BufferedReader in;
    private int playerId;
    private int crntPlayer = 1;

    private final Color[][] hLines = new Color[GRID_SIZE][GRID_SIZE - 1];
    private final Color[][] vLines = new Color[GRID_SIZE - 1][GRID_SIZE];
    private final JLabel LabelPlr;
    private final JLabel Player1LabelScr;
    private final JLabel Player2LabelScr;
    private final JLabel turnLabel;

    public Client(String serverAddress) {
        try {
            Socket socket = new Socket(serverAddress, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new ServerListener()).start();
        } catch (IOException event) {
            event.printStackTrace();
        }

        setTitle("Dots and Boxes");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Инициализация линий полупрозрачным цветом
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (j < GRID_SIZE - 1) {
                    hLines[i][j] = LINES_COLOR;
                }
                if (i < GRID_SIZE - 1) {
                    vLines[i][j] = LINES_COLOR;
                }
            }
        }

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

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

        infoPanel.add(LabelPlr);
        infoPanel.add(new JLabel("Score: "));
        infoPanel.add(Player1LabelScr);
        infoPanel.add(Player2LabelScr);
        infoPanel.add(turnLabel);
        add(infoPanel, BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (playerId != crntPlayer) return;

                int x = event.getX() - INDENTATION;
                int y = event.getY() - INDENTATION;
                int fieldSize = (getWidth() - 2 * INDENTATION) / GRID_SIZE;

                if (x < 0 || y < 0) {
                    return;
                }

                if (x >= GRID_SIZE * fieldSize || y >= GRID_SIZE * fieldSize) {
                    return;
                }


                int rw = y / fieldSize;
                int cl = x / fieldSize;

                int dx = x % fieldSize;
                int dy = y % fieldSize;

                boolean clickedHorizontalTop = dy < 10 && rw < GRID_SIZE && cl < GRID_SIZE - 1;
                boolean clickedHorizontalBottom = dy > fieldSize - 10 && rw < GRID_SIZE - 1 && cl < GRID_SIZE - 1;
                boolean clickedVerticalLeft = dx < 10 && rw < GRID_SIZE - 1 && cl < GRID_SIZE;
                boolean clickedVerticalRight = dx > fieldSize - 10 && rw < GRID_SIZE - 1 && cl < GRID_SIZE - 1;

                if (clickedHorizontalTop && hLines[rw][cl].equals(LINES_COLOR)) {
                    hLines[rw][cl] = (playerId == 1) ? Color.BLUE : Color.RED;
                    out.println("move,h," + rw + "," + cl);
                } else if (clickedHorizontalBottom && hLines[rw + 1][cl].equals(LINES_COLOR)) {
                    hLines[rw + 1][cl] = (playerId == 1) ? Color.BLUE : Color.RED;
                    out.println("move,h," + (rw + 1) + "," + cl);
                } else if (clickedVerticalLeft && vLines[rw][cl].equals(LINES_COLOR)) {
                    vLines[rw][cl] = (playerId == 1) ? Color.BLUE : Color.RED;
                    out.println("move,v," + rw + "," + cl);
                } else if (clickedVerticalRight && vLines[rw][cl + 1].equals(LINES_COLOR)) {
                    vLines[rw][cl + 1] = (playerId == 1) ? Color.BLUE : Color.RED;
                    out.println("move,v," + rw + "," + (cl + 1));
                } else {return;}
                repaint();
            }
        });

        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int fieldSize = (getWidth() - 2 * INDENTATION) / GRID_SIZE;
        g.setColor(Color.BLACK);

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                g.fillOval(INDENTATION + j * fieldSize - 5, INDENTATION + i * fieldSize - 5, 10, 10);
            }
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                // Рисуем горизонтальную линию
                if (j < GRID_SIZE - 1) {
                    g.setColor(hLines[i][j]);
                    g.fillRect(INDENTATION + j * fieldSize, INDENTATION + i * fieldSize - 5, fieldSize, 10);
                }

                // Рисуем вертикальную линию
                if (i < GRID_SIZE - 1) {
                    g.setColor(vLines[i][j]);
                    g.fillRect(INDENTATION + j * fieldSize - 5, INDENTATION + i * fieldSize, 10, fieldSize);
                }
            }
        }
    }
    private class ServerListener implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("start")) {
                        playerId = Integer.parseInt(message.split(",")[1]);
                        SwingUtilities.invokeLater(() -> LabelPlr.setText("Player: " + (playerId == 1 ? "Player1" : "Player2")));
                    } else if (message.startsWith("player")) {
                        String[] tokens = message.split(":")[1].split(",");
                        String type = tokens[0];
                        int rw = Integer.parseInt(tokens[1]);
                        int cl = Integer.parseInt(tokens[2]);

                        if (type.equals("h")) {
                            hLines[rw][cl] = (crntPlayer == 1) ? Color.BLUE : Color.RED;
                        } else if (type.equals("v")) {
                            vLines[rw][cl] = (crntPlayer == 1) ? Color.BLUE : Color.RED;
                        }
                        repaint();
                    } else if (message.startsWith("turn")) {
                        crntPlayer = Integer.parseInt(message.split(":")[1]);
                        SwingUtilities.invokeLater(() -> turnLabel.setText("Turn: Player " + crntPlayer));
                    } else if (message.startsWith("score")) {
                        String[] scores = message.split(":")[1].split(",");
                        SwingUtilities.invokeLater(() -> {
                            Player1LabelScr.setText(scores[0]);
                            Player2LabelScr.setText(scores[1]);
                        });
                    } else if (message.startsWith("game_over")) {
                        String winner = message.split(":")[1];
                        JOptionPane.showMessageDialog(Client.this, winner);
                        System.exit(0);
                    }
                }
            } catch (IOException event) {
                event.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        new Client(serverAddress);
    }
}