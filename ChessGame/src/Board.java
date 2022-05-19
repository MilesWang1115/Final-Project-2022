import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Board extends JPanel {
    private static final long serialVersionUID = 1L;
    public JLabel label;
    public Chess chess;
    private int size;
    public BoardListener listener;
    Clip moveClip;
    AudioInputStream moveAis;
    private final static String[] alphas = { "A", "B", "C", "D", "E", "F", "G", "H"};

    Board(Chess chess, int size) {
        this.label = null;
        this.chess = chess;
        this.size = size;
        this.listener = null;
        moveClip = null;
        moveAis = null;
    }

    public void setLabel(JLabel label) {
        this.label = label;
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
    }

    public void paintBoard() {
        Graphics g = this.getGraphics();
        //Paint the board
        ArrayList<ArrayList<Piece>> board = chess.getBoard();
        for(int row=7; row>=0; row--) {
            //Get a row
            ArrayList<Piece> row_brd = board.get(row);
            for(int col=0; col<8; col++) {
                //Draw grid
                g.setColor(getColorByLocation(new PiecesLocation(col, row)));
                int x = col * size + 12;
                int y = (7 - row) * size + 12;
                g.fillRect(x, y, size, size);
                //Draw piece
                Piece piece = row_brd.get(col);
                if(piece == null)
                    continue;
                Image icon = new ImageIcon(piece.getPieceIconName()).getImage();
                g.drawImage(icon, x, y, size, size, this);
            }
        }
        //Paint the coordinate
        g.setColor(Color.BLACK);
        for(int col = 1; col <= 8; col++) {
            g.drawString(alphas[col-1], col * size - size/2 - 4 + 12, 11);
            g.drawString(alphas[col-1], col * size - size/2 - 4 + 12, 8*size + 24);
        }
        for(int row = 1; row <=8; row++) {
            g.drawString(String.valueOf(9-row), 2, row * size - size/2 + 12);
            g.drawString(String.valueOf(9-row), 8*size + 13, row * size - size/2 + 12);
        }


        //Show the message
        drawLabel("  " + chess.thisColor() + " 请走棋" );
    }

    public void paintBoard2() {
        Graphics g = this.getGraphics();
        //Paint the board
        ArrayList<ArrayList<Piece>> board = chess.getBoard();
        for(int row=7; row>=0; row--) {
            //Get a row
            ArrayList<Piece> row_brd = board.get(row);
            for(int col=0; col<8; col++) {
                //Draw grid
                g.setColor(getColorByLocation(new PiecesLocation(col, row)));
                int x = col * size + 12;
                int y = (7 - row) * size + 12;
                g.fillRect(x, y, size, size);
                //Draw piece
                Piece piece = row_brd.get(col);
                if(piece == null)
                    continue;
                Image icon = new ImageIcon(piece.getPieceIconName2()).getImage();
                g.drawImage(icon, x, y, size, size, this);
            }
        }
        //Paint the coordinate
        g.setColor(Color.BLACK);
        for(int col = 1; col <= 8; col++) {
            g.drawString(alphas[col-1], col * size - size/2 - 4 + 12, 11);
            g.drawString(alphas[col-1], col * size - size/2 - 4 + 12, 8*size + 24);
        }
        for(int row = 1; row <=8; row++) {
            g.drawString(String.valueOf(9-row), 2, row * size - size/2 + 12);
            g.drawString(String.valueOf(9-row), 8*size + 13, row * size - size/2 + 12);
        }


        //Show the message
        drawLabel("  " + chess.thisColor() + " 请走棋" );
    }

    public void drawSelection(PiecesLocation loc, Color color ) {
        if(loc == null)
            return;
        Graphics g = this.getGraphics();
        int x = loc.col * size + 1 + 12;
        int y = (7-loc.row) * size + 1 + 12;
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(3.0f));
        g.drawRect(x, y, size - 2, size - 2);
    }

    public void drawLabel(String text) {
        label.setText(text);
    }

    public void setupListener(BoardListener listener) {
        this.listener = listener;
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
    }

    public Color getColorByLocation(PiecesLocation loc) {
        if( (loc.row + loc.col)%2 == 0)
            return (Color.LIGHT_GRAY);
        else
            return (Color.GRAY);
    }

    public void playSound() {
        try {
            moveClip = AudioSystem.getClip();
            moveAis = AudioSystem.getAudioInputStream(new File("chess-move.wav"));
            moveClip.open(moveAis);
            moveClip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stopSound() {
        if(moveClip != null) {
            moveClip.stop();
            moveClip.close();
        }
        if(moveAis != null)
            try {
                moveAis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public int getPieceSize() {
        return this.size;
    }

    public void setPieceSize(int size) {
        this.size = size;
    }
}

