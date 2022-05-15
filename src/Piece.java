import java.util.ArrayList;

public abstract class Piece {
    protected Chess chess;
    protected PiecesColor color;
    protected PiecesName name;
    protected PiecesLocation location;
    protected boolean moved;
    protected boolean visible;

    //Debug optional
    public boolean trace;
    public boolean verbose;

    abstract boolean tryMoveTo(PiecesLocation newLoc, boolean peek);
    abstract boolean tryEatAt(PiecesLocation newLoc, boolean peek);

    Piece(Chess chess, PiecesColor color, PiecesName name, PiecesLocation loc, boolean moved) {
        this.chess = chess;
        this.color = color;
        this.name = name;
        this.location = loc;
        this.moved = moved;
        this.visible = true;

        trace = false;
        verbose = false;
    }

    public String toString() {
        String res = color + " " + name + " " + location + " " + moved ;
        return res;
    }

    public String shortName() {
        return name.toString().substring(0, 1);
    }

    public PiecesColor color() {
        return color;
    }

    private int getStep(int offset) {
        int step = 0;
        if(offset < 0)
            step = -1;
        else if(offset > 0)
            step = 1;
        return step;
    }

    //Is there any piece on the path?
    public boolean overPiece(PiecesLocation newLoc) {
        int col_offset = newLoc.col - location.col;
        int row_offset = newLoc.row - location.row;

        int col_step = getStep(col_offset);
        int row_step = getStep(row_offset);

        if(col_step == 0 && row_step == 0) { //Not moving?
            if(trace)
                System.out.println("overPiece(): Not moving? BUG!!!");
            return false;
        }

        int col = this.location.col;
        int row = this.location.row;
        while(true) {
            //Move one step
            col += col_step;
            row += row_step;
            //Arrived destination?
            if(col == newLoc.col && row == newLoc.row)
                break;
            //Out of range?
            if(col < 0 || col > 7 || row < 0 || row > 7) {
                System.out.println("overPiece(): BUG! The path is out of the board! col=" + col
                        + ", row=" + row);
            }
            //Is there any piece here?
            Piece p = chess.pieceAt(new PiecesLocation(col, row));
            if(p != null) {
                if(trace)
                    System.out.println("overPiece(): Found " + p.toString());
                return true;
            }
        }

        return false;
    }

    //Find out all the nearest moves
    public ArrayList<PiecesLocation> findValidMove() {
        ArrayList<PiecesLocation> valid_loc = new ArrayList<>();

        //Get the scan range
        int col_min = location.col - 2;
        if(col_min < 0)
            col_min = 0;
        int col_max = location.col + 2;
        if(col_max > 7)
            col_max = 7;
        int row_min = location.row - 2;
        if(row_min < 0)
            row_min = 0;
        int row_max = location.row + 2;
        if(row_max > 7)
            row_max = 7;

        //Scan
        for(int col=col_min; col <= col_max; col++) {
            for(int row=row_min; row <= row_max; row++) {
                //Skip the center position
                if(col == location.col && row == location.row)
                    continue;
                PiecesLocation loc = new PiecesLocation(col, row);
                if(tryEatAt(loc, true) || tryMoveTo(loc, true))
                    valid_loc.add(loc);
            }
        }

        //Check result
        if(trace) {
            if(valid_loc.size() == 0)
                System.out.println("findValidMove(): Freez piece: " + this.toString());
        }
        return valid_loc;
    }

    public boolean isMyFood(PiecesLocation loc) {
        Piece piece = chess.pieceAt(loc);
        //No food
        if(piece == null) {
            if(verbose)
                System.out.println("isMyFood(): Piece not found at " + loc);
            return false;
        }
        //Don't eat your family
        if(piece.color == this.color) {
            if(verbose)
                System.out.println("isMyFood(): " + piece + " is my family.");
            return false;
        }
        return true;
    }

    public String getPieceIconName() {
        String fix=".png";
        switch(color) {
            case Black:
                fix = "_black.png";
                break;
            case White:
                fix = "_white.png";
                break;
        }
        return name.toString().toLowerCase() + fix;
    }
}

