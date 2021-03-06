import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class BoardListener implements ActionListener,
        MouseListener, MouseMotionListener {
    private Chess chess;
    private Board board;
    private int clicks;
    private PiecesLocation pieceLocation, newLocation;
    public boolean busy;
    private PiecesLocation lastLocation;

    BoardListener(Board board) {
        this.chess = board.chess;
        this.board = board;
        clicks = 0;
        pieceLocation = null;
        newLocation = null;
        busy = false;
        lastLocation = null;
    }

    private PiecesLocation getLocation(MouseEvent e) {
        int x = e.getX() / board.getPieceSize();
        int y = e.getY() / board.getPieceSize();
        if(x < 0 || x > 7 || y < 0 || y > 7)
            return null;
        PiecesLocation loc = new PiecesLocation(x, 7 - y);
        return loc;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(busy)
            return;
        PiecesLocation loc = getLocation(e);
        if(clicks == 1 && loc.equals(pieceLocation))
            return;
        if(loc == null)
            return;
        if(lastLocation != null && loc.equals(lastLocation))
            return;
        //Draw a mask
        Color color;
        //Clear the last selection
        if(lastLocation != null) {
            if(clicks == 1 && lastLocation.equals(pieceLocation))
                color = Color.GREEN;
            else
                color = board.getColorByLocation(lastLocation);
            board.drawSelection(lastLocation, color);
        }
        //Draw new selection
        lastLocation = loc;
        board.drawSelection(loc, Color.MAGENTA);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(busy)
            return;
        switch(clicks) {
            case 0:
                //Get the 1st location
                pieceLocation = getLocation(e);
                if(pieceLocation == null)
                    return;
                //Is there a piece at this location?
                Piece piece = chess.pieceAt(pieceLocation);
                if( piece == null)
                    return;
                //Is this piece is correct color?
                if( piece.color != chess.thisColor())
                    return;
                clicks++;
                //Draw a frame
                board.drawSelection(pieceLocation, Color.GREEN);
                //Debug
                if(chess.trace) {
                    ArrayList<PiecesLocation> valid_loc = piece.findValidMove();
                    System.out.println("Piece " + piece + " may move to " + valid_loc.size() + " location(s).");
                    for(PiecesLocation pl: valid_loc)
                        System.out.println("    -> " + pl);
                }
                break;
            case 1:
                //Get the 2nd location
                newLocation = getLocation(e);
                if(newLocation == null)
                    return;
                clicks = 0;
                //Clear the frame
                board.drawSelection(pieceLocation,board.getColorByLocation(pieceLocation));
                //Try to move the piece
                Status status = chess.tryMove(pieceLocation, newLocation);
                if(status.error) {
                    JOptionPane.showMessageDialog(
                            board, status.note, "??????Q???", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(status.endGame) {
                    JOptionPane.showMessageDialog(
                            board, status.note, "GAME OVER", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
                PiecesAleration aleration = status.get(0);
                if(aleration == null)
                    return;
                if(aleration.action == PiecesAction.Illegal) {
                    JOptionPane.showMessageDialog(
                            board, "???????????????????????????", "?????????", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                //During repainting, disable mouse click
                busy = true;
                //Refresh the board
                board.paintBoard();
                //Play moving sound
                board.playSound();
                //And prepare next turns
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        prepareNextTurns();
                    }
                });
        }
    }

    public void prepareNextTurns() {
        PiecesLocation red = null;
        PiecesLocation yellow = null;

        //Prepare next turns
        boolean ackCheck = false;
        boolean endGame = false;
        while(!endGame) {
            Status status = chess.tryNextTurns(ackCheck);
            if(status.error) {
                JOptionPane.showMessageDialog(
                        board, status.note, "??????Q???", JOptionPane.ERROR_MESSAGE);
                break;
            }
            if(status.size() == 0) //Nothing to be prepared
                break;
            PiecesAleration aleration = status.get(0);
            switch(aleration.action) {
                case Check:
                    ackCheck = true;
                    PiecesColor color = chess.getEnemyColor();
                    String message;
                    if(color == PiecesColor.Black)
                        message = "??????????????????";
                    else
                        message = "??????????????????";
                    JOptionPane.showMessageDialog(
                            board, message, "????????????", JOptionPane.WARNING_MESSAGE);
                    //Save warning info
                    red = aleration.piece.location;
                    yellow = aleration.location;
                    break;
                case Checkmate:
                case PerpetualCheck:
                case Repetition:
                case Stalemate:
                    String result = "";
                    if(status.result.draw)
                        result = "??????";
                    else {
                        if(status.result.winner == PiecesColor.Black)
                            result = "?????????";
                        else
                            result = "?????????";
                    }
                    JOptionPane.showMessageDialog(
                            board, status.note, result, JOptionPane.INFORMATION_MESSAGE);
                    endGame = true;
                    break;
                case Promotion:
                    //Let user to choose a new piece
                    Object[] pieces = { "Queen", "Rook", "Bishop", "Knight" };
                    String name = null;
                    while(name == null)
                        name = (String)JOptionPane.showInputDialog(
                                board, "?????????????????????", "?????????",
                                JOptionPane.PLAIN_MESSAGE, null, pieces, pieces[0] );
                    //Promote Pawn
                    //Term: 2-(3)
                    try {
                        status = chess.tryPromotionPawn((Pawn)aleration.piece, name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
            if(status.error) {
                JOptionPane.showMessageDialog(
                        board, status.note, "??????Q???", JOptionPane.ERROR_MESSAGE);
                break;
            }
        }
        //Now user can operating mouse again
        busy = false;
        board.paintBoard();
        //Draw warning
        if(red != null)
            board.drawSelection(red, Color.RED);
        if(yellow != null)
            board.drawSelection(yellow, Color.ORANGE);
        if(endGame)
            System.exit(0);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }

}

