public class Pawn extends Piece {
    boolean twoSteps;

    Pawn(Chess chess, PiecesColor color, PiecesLocation loc, boolean moved, boolean twoSteps) {
        super(chess, color, PiecesName.Pawn, loc, moved);
        this.twoSteps = twoSteps;
        //trace = true;
        //verbose = true;
    }

    @Override
    public String toString() {
        String res = color + " " + name + " " + location + " " + moved + " " + twoSteps ;
        return res;
    }

    private boolean isForward(PiecesLocation newLoc) {
        int row_offset = newLoc.row-this.location.row;
        if(row_offset == 0) {
            if (verbose)
                System.out.println("Pawn:isForward(): Vertical move only!");
            return false;
        }
        if(this.color == PiecesColor.White && row_offset < 0) {
            if(verbose)
                System.out.println("Pawn:isForward(): Backward not allowed!");
            return false;
        }
        if(this.color == PiecesColor.Black && row_offset > 0) {
            if(verbose)
                System.out.println("Pawn:isForward(): Backward not allowed!");
            return false;
        }
        return true;
    }

    @Override
    boolean tryMoveTo(PiecesLocation newLoc, boolean peek) {
        int col_offset = newLoc.col-this.location.col;
        int row_offset = newLoc.row-this.location.row;

        if(trace)
            System.out.println("Pawn:tryMoveTo(): " + this.toString() + " -> " + newLoc.toString());
        //Forward only
        if(!isForward(newLoc))
            return false;
        //Vertical only
        if(col_offset != 0) {
            if(verbose)
                System.out.println("Pawn:tryMoveTo(): Vertical moving only!");
            return false;
        }

        //One step only
        if(Math.abs(row_offset) == 1
                && chess.pieceAt(newLoc) == null) {
            if(trace)
                System.out.println("Pawn:tryMoveTo(): Forward 1 step to " + newLoc.toString());
            if(peek)
                return true;
            //Action!
            twoSteps = false;
            chess.movePiece(this, newLoc);
            return true;
        }

        //Two steps for the firsthand only
        if(!moved && Math.abs(row_offset) == 2
                && chess.pieceAt(newLoc) == null) {
            if(trace)
                System.out.println("Pawn:tryMoveTo(): Forward 2 steps to " + newLoc.toString());
            if(peek)
                return true;
            //Action!
            twoSteps = true;
            chess.movePiece(this, newLoc);
            return true;
        }

        return false;
    }

    @Override
    boolean tryEatAt(PiecesLocation newLoc, boolean peek) {
        int col_offset = Math.abs(newLoc.col-this.location.col);
        int row_offset = Math.abs(newLoc.row-this.location.row);

        if(trace)
            System.out.println("Pawn:tryEatAt(): " + this.toString()  + " -> " +  newLoc.toString());

        //Forward only
        if(!isForward(newLoc))
            return false;
        //One step only
        if(row_offset > 1 || col_offset > 1) {
            if(verbose)
                System.out.println("Pawn:tryEatAt(): ONE step only!");
            return false;
        }
        //Check Forward-Left && Forward-Right Only
        if(col_offset != 1) {
            if(verbose)
                System.out.println("Pawn:tryEatAt(): Diagonal step only!");
            return false;
        }

        //Eat destination piece
        if(isMyFood(newLoc)) {
            //Eating and moving
            if(trace)
                System.out.println("Pawn:tryEatAt(): Eating and moving to " + newLoc.toString());
            if(peek)
                return true;
            //Action!
            twoSteps = false;
            chess.capturePiece(newLoc);
            chess.movePiece(this, newLoc);
            return true;
        }

        //Check the side firsthand pawn
        PiecesLocation loc = new PiecesLocation(newLoc.col, this.location.row);
        Piece p = chess.pieceAt(loc);
        if(p == null) {
            if(verbose)
                System.out.println("Pawn:tryEatAt(): No piece at " + loc.toString());
            return false;
        }
        if(p.name != PiecesName.Pawn) {
            if(verbose)
                System.out.println("Pawn:tryEatAt(): Piece is not a Pwan at " + loc.toString());
            return false;
        }
        if(p.color == this.color) {
            if(verbose)
                System.out.println("Pawn:tryEatAt(): Piece is my family at " + loc.toString());
            return false;
        }
        Pawn pawn = (Pawn)p;
        if(!pawn.twoSteps) {
            if(verbose)
                System.out.println("Pawn:tryEatAt(): Pwan is not a 2 steps moving at " + loc.toString());
            return false;
        }
        //Eating side pawn
        if(trace)
            System.out.println("Pawn:tryEatAt(): Eating side Pawn at " + loc.toString());
        if(peek)
            return true;
        //Action!
        chess.capturePiece(loc);
        if(trace)
            System.out.println("Pawn:tryEatAt(): Moving to " + newLoc.toString());
        chess.movePiece(this, newLoc);
        twoSteps = false;
        return true;
    }

}
