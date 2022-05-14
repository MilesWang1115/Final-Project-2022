public class King extends Piece {

    King(Chess chess, PiecesColor color, PiecesLocation loc, boolean moved) {
        super(chess, color, PiecesName.King, loc, moved);
        //trace = true;
        //verbose = true;
    }

    private boolean validTo(PiecesLocation newLoc) {
        // One step only
        if(Math.abs(newLoc.col-this.location.col) > 1) {
            if(verbose)
                System.out.println("King:validTo(): One step only! (col)");
            return false;
        }
        if(Math.abs(newLoc.row-this.location.row) > 1) {
            if(verbose)
                System.out.println("King:validTo(): One step only! (row)");
            return false;
        }
        return true;
    }

    private boolean tryCastlingMove(PiecesLocation newLoc, boolean peek) {
        //The King should not be moved before castling
        //Term: 2-(2)-iv
        if(moved) {
            if(verbose)
                System.out.println("tryCastlingMove(): The King was moved!");
            return false;
        }

        //The King should not be in 'Check' status
        //Term: 2-(2)-ii
        PiecesLog l = chess.getLastLog();
        if(l != null) {
            if(l.action == PiecesAction.Check && l.color != this.color ) {
                if(trace)
                    System.out.println("tryCastlingMove(): Can't perform castling in 'Check' status!");
                return false;
            }
        }

        //Check the offset of the King
        int col_offset = newLoc.col - location.col;
        int row_offset = newLoc.row - location.row;
        if(row_offset != 0) {
            if(verbose)
                System.out.println("King:tryCastlingMove(): Left and Right moving only!");
            return false;
        }
        if(Math.abs(col_offset) != 2 && Math.abs(col_offset) != 3) {
            if(verbose)
                System.out.println("King:tryCastlingMove(): King must move 2 or 3 steps!");
            return false;
        }

        //Check the Rook
        int step;
        PiecesLocation rook_loc;
        if(col_offset < 0) {
            rook_loc = new PiecesLocation(0, location.row);
            step = -1;
        }
        else {
            rook_loc = new PiecesLocation(7, location.row);
            step = 1;
        }
        Piece p = chess.pieceAt(rook_loc);
        if(p == null) {
            if(verbose)
                System.out.println("King:tryCastlingMove(): Piece not found at " + rook_loc.toString());
            return false;
        }
        if(p.name != PiecesName.Rook) {
            if(verbose)
                System.out.println("King:tryCastlingMove(): Rook not found at " + rook_loc.toString());
            return false;
        }

        //The Rook should be not moved
        //Term: 2-(2)-iv
        Rook rook = (Rook)p;
        if(rook.moved) {
            if(trace)
                System.out.println("King:tryCastlingMove(): The Rook was moved!");
            return false;
        }

        //It should be a clear path.
        //Term: 2-(2)-i
        if(overPiece(rook_loc)) {
            if(trace)
                System.out.println("King:tryCastlingMove(): There is a piece between King and Rook!");
            return false;
        }

        //Check if the King will be attacked in the path
        //Term: 2-(2)-iii
        int col = location.col + step;
        Piece attacker;
        while(col != rook.location.col) {
            //Try to move on the path to find out potential attacker.
            attacker = chess.simulateAttack(this, new PiecesLocation(col, location.row));
            if(attacker != null) {
                if(trace)
                    System.out.println("King:tryCastlingMove(): Attacker found " + attacker.toString());
                return false;
            }
            col += step;
            if(col < 0 || col > 7) {
                System.out.println("King:tryCastlingMove(): Out of board during checking:" + col + ", Step:" + step);
                return false;
            }
        }

        //Ok, Perform castling
        if(trace)
            System.out.println("King:tryCastlingMove(): King is castling to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        if(col_offset < 0)
            rook_loc = new PiecesLocation(newLoc.col+1, newLoc.row);
        else
            rook_loc = new PiecesLocation(newLoc.col-1, newLoc.row);
        //Move rook
        chess.movePiece(p, rook_loc);
        //Move King
        chess.movePiece(this, newLoc);
        if(trace)
            System.out.println("King:tryCastlingMove(): Rook is castling to " + rook_loc.toString());
        return true;
    }

    @Override
    boolean tryMoveTo(PiecesLocation newLoc, boolean peek) {
        if(verbose)
            System.out.println("King:tryMoveTo(): " + this.toString()  + " -> " +  newLoc.toString());

        if(!validTo(newLoc)) {
            //Check castling
            return tryCastlingMove(newLoc, peek);
        }

        //The destination should be empty
        if(chess.pieceAt(newLoc) != null) {
            if(trace)
                System.out.println("King:tryMoveTo(): Not an empty location!");
            return false;
        }

        //A King should not going to feed the enemy!
        //So make a simulation
        Piece attacker = chess.simulateAttack(this, newLoc);
        if(attacker != null) {
            if(trace)
                System.out.println("King:tryMoveTo(): King will attacked by " + attacker.toString());
            return false;
        }

        //Move the King
        if(trace)
            System.out.println("King:tryMoveTo(): Moving 1 step to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.movePiece(this, newLoc);
        return true;
    }

    @Override
    boolean tryEatAt(PiecesLocation newLoc, boolean peek) {
        if(trace)
            System.out.println("King:tryEatAt(): " + this.toString()  + " -> " +  newLoc.toString());

        if(!validTo(newLoc))
            return false;

        Piece enemy = chess.pieceAt(newLoc);
        if(enemy == null){
            if(verbose)
                System.out.println("King:tryEatAt(): There is no piece to eat.");
            return false;
        }
        if(enemy.color == color) {
            if(verbose)
                System.out.println("King:tryEatAt(): A King should not eat his courtiers!");
            return false;
        }
        if(enemy.name == PiecesName.King) {
            if(trace)
                System.out.println("King:tryEatAt(): A King should not eat another King!");
            return false;
        }

        //A King should not go to feed the enemy!
        //So make a simulation
        Piece attacker = chess.simulateAttack(this, newLoc);
        if(attacker != null) {
            if(trace)
                System.out.println("King:tryEatAt(): King will attacked by " + attacker.toString());
            return false;
        }

        //Eating and moving
        if(trace)
            System.out.println("King:tryEatAt(): Eating and moving to " + newLoc.toString());
        if(peek)
            return true;
        //Action!
        chess.capturePiece(newLoc);
        chess.movePiece(this, newLoc);

        return true;
    }


}

