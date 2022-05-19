import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

public class Chess {
    private ArrayList<Piece> pieces;
    private PiecesColor thisTurnColor;
    private ArrayList<PiecesLog> logs;
    public boolean trace;
    private Piece capturedPiece;
    public Clip bgm;

    private static final PiecesName[] beginLayout = new PiecesName[] {
            PiecesName.Rook, PiecesName.Knight, PiecesName.Bishop,
            PiecesName.Queen, PiecesName.King,
            PiecesName.Bishop, PiecesName.Knight, PiecesName.Rook
    };

    public Piece newPiece(PiecesColor color, PiecesName name, PiecesLocation loc, boolean moved)
            throws Exception {
        switch(name) {
            case Pawn:
                return new Pawn(this, color, loc, moved, false);
            case King:
                return new King(this, color, loc, moved);
            case Queen:
                return new Queen(this, color, loc, moved);
            case Rook:
                return new Rook(this, color, loc, moved);
            case Knight:
                return new Knight(this, color, loc, moved);
            case Bishop:
                return new Bishop(this, color, loc, moved);
            default:
                throw new Exception("newPiece(): Invalid Piece Name:" + name);
        }
    }

    public void init() {
        //Clear logs
        logs = new ArrayList<>();
        //White turns
        thisTurnColor = PiecesColor.White;
        //Beginning Layout
        pieces = new ArrayList<>();
        try {
            for(int col = 0; col < 8; col++) {
                pieces.add(newPiece(
                        PiecesColor.Black,
                        beginLayout[col],
                        new PiecesLocation(col, 7),
                        false)
                );
                pieces.add(newPiece(
                        PiecesColor.Black,
                        PiecesName.Pawn,
                        new PiecesLocation(col, 6),
                        false)
                );
                pieces.add(newPiece(
                        PiecesColor.White,
                        beginLayout[col],
                        new PiecesLocation(col, 0),
                        false)
                );
                pieces.add(newPiece(
                        PiecesColor.White,
                        PiecesName.Pawn,
                        new PiecesLocation(col, 1),
                        false)
                );
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        //Initial the rest
        capturedPiece = null;
        //Initial the bgm
        AudioInputStream ais;
        try {
            bgm = AudioSystem.getClip();
            ais = AudioSystem.getAudioInputStream(new File("chess-bgm.wav"));
            bgm.open(ais);
            bgm.start();
            bgm.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Chess() {
        //If you want to know what's happened, set trace to 'true'
        trace = false;
        //Initial game
        init();
    }

    public Piece pieceAt(PiecesLocation loc) {
        for(Piece piece:this.pieces) {
            if(piece.visible && piece.location.equals(loc))
                return piece;
        }
        return null;
    }

    public void capturePiece(PiecesLocation loc) {
        Piece p = pieceAt(loc);
        if(p == null) {
            if(trace)
                System.out.println("capturePiece(): Piece not found at " + loc);
            return;
        }
        pieces.remove(p);
        if(trace)
            System.out.println("capturePiece(): Remove " + p);
        //Save the captured piece for Status
        capturedPiece = p;
        //Log
        logs.add(new PiecesLog(
                thisTurnColor,
                PiecesAction.Capture,
                p.toString()));
    }

    public void movePiece(Piece piece, PiecesLocation newLoc) {
        if(trace)
            System.out.println("Move " + piece + " to " + newLoc);
        //Before change the location, log it.
        logs.add(new PiecesLog(
                thisTurnColor,
                PiecesAction.Move,
                piece + " " + newLoc));
        //Replace the location with new one
        piece.location = newLoc;
        piece.moved = true;
    }

    private void toggleTurns() {
        if(thisTurnColor == PiecesColor.White)
            thisTurnColor = PiecesColor.Black;
        else
            thisTurnColor = PiecesColor.White;
    }

    public PiecesColor thisColor() {
        return thisTurnColor;
    }

    public PiecesColor getEnemyColor() {
        if(thisTurnColor == PiecesColor.White)
            return(PiecesColor.Black);
        else
            return(PiecesColor.White);
    }

    //Get the last log
    public PiecesLog getLastLog() {
        if(logs.size() == 0)
            return null;
        int last_index = logs.size() - 1;
        return logs.get(last_index);
    }

    //Generate a 2-D ArrayList to show the chess board
    public ArrayList<ArrayList<Piece>> getBoard() {
        ArrayList<ArrayList<Piece>> res = new ArrayList<>();
        for(int row=0; row<8; row++) {
            ArrayList<Piece> a_row = new ArrayList<>();
            for(int col=0; col<8; col++)
                a_row.add(pieceAt(new PiecesLocation(col, row)));
            res.add(a_row);
        }
        return res;
    }

    //Game controller: Promotion Pawn
    private Pawn getPromotionPawn() {
        for(Piece p:pieces) {
            if(!p.visible)
                continue;
            if(p.name != PiecesName.Pawn || p.color != thisColor())
                continue;
            if(p.color == PiecesColor.White && p.location.row == 7) {
                return (Pawn)p;
            }
            if(p.color == PiecesColor.Black && p.location.row == 0) {
                return (Pawn)p;
            }
        }
        return null;
    }

    //Game controller: Promotion Pawn
    //Term: 2-(3)
    public Status tryPromotionPawn(Pawn pawn, String name) throws Exception {
        //Generate new piece
        Piece new_piece = newPiece(pawn.color, PiecesName.valueOf(name), pawn.location, true);
        //Remove the pawn from the board
        pieces.remove(pawn);
        //Add new piece to the board
        pieces.add(new_piece);
        if(trace)
            System.out.println("promotionPawn(): Replace " + pawn + " with " + new_piece);
        //write log
        logs.add(new PiecesLog(
                thisColor(),
                PiecesAction.Promotion,
                new_piece.toString()));
        //Send back the status
        Status status = new Status();
        status.add(new PiecesAlteration(
                new_piece,
                pawn.location,
                PiecesAction.Promotion));
        return status;
    }

    //Game controller: Move Piece
    public Status tryMove(PiecesLocation pieceLoc, PiecesLocation newLoc) {
        if(trace) {
            System.out.println("tryMove(" + pieceLoc + " -> " + newLoc + ")");
        }
        //Find the piece
        Piece piece = pieceAt(pieceLoc);
        if(piece == null) {
            return new Status("Piece not found at " + pieceLoc);
        }
        //Check the turns
        if(piece.color != thisColor()) {
            return new Status("Please move " + thisColor() + " piece only!");
        }
        //Check moving parameters
        if(pieceLoc.equals(newLoc)) {
            return new Status("Please move the piece to a different location!");
        }
        //Validate this moving
        Status status = new Status();
        //Is there Inside Men/Women?
        piece.location = newLoc;
        Status inside_man = tryCheck(thisColor());
        if(inside_man.size() != 0) {
            piece.location = pieceLoc;
            boolean you_are_inside_man = true;
            if(inside_man.size() == 1) {
                //If there is ONLY one attacker, I should compare the location of attacker and newLoc
                PiecesAlteration pa = inside_man.get(0);
                Piece attacker = pa.piece;
                if(attacker.location.equals(newLoc)) {
                    //Really? Are you going to eat this attacker?
                    if(piece.tryEatAt(newLoc, true)) {
                        you_are_inside_man = false;
                        if(trace)
                            System.out.println("tryMove(): Really? You are going to eat this attacker!!!");
                    } else {
                        if(trace)
                            System.out.println("tryMove(): Unfortunately, you are NOT able to eat this attacker!");
                    }
                } else {
                    if(trace)
                        System.out.println("tryMove(): Are you a coward? You should duel with the enemy!");
                }
            } else {
                if(trace)
                    System.out.println("tryMove(): After moving away, " + inside_man.size() + " attackers will eat your king!");
            }
            if(you_are_inside_man) {
                //You are an Inside Man!
                if (trace)
                    System.out.println("tryMove(): " + piece + " is an INSIDE MAN!");
                status.add(new PiecesAlteration(
                        piece,
                        newLoc,
                        PiecesAction.Illegal));
                return status;
            }
        }
        piece.location = pieceLoc;
        //Try move/eat piece
        if(piece.tryEatAt(newLoc, false)) {	// Capture
            status.add(new PiecesAlteration(
                    capturedPiece,
                    capturedPiece.location,
                    PiecesAction.Capture));
            status.add(new PiecesAlteration(
                    piece,
                    pieceLoc,
                    PiecesAction.Move
            ));
        }
        else if(piece.tryMoveTo(newLoc, false)) { // Move
            status.add(new PiecesAlteration(
                    piece,
                    pieceLoc,
                    PiecesAction.Move));
        }
        else {	//Illegal Move
            status.add(new PiecesAlteration(
                    piece,
                    newLoc,
                    PiecesAction.Illegal));
        }
        return status;
    }

    //Find an enemy piece which can attack my piece@loc
    public Piece tryAttack(PiecesColor myColor, PiecesLocation loc) {
        if(trace) {
            System.out.println("tryAttack(" + myColor + ", " + loc + ")");
        }
        //Find out check piece
        for(Piece p:pieces) {
            if(!p.visible)
                continue;
            if(p.color == myColor)
                continue;
            //Let me have a look without eating!
            if(p.tryEatAt(loc, true)) {
                if(trace)
                    System.out.println("tryAttack(): " + p + " ---E " + loc);
                return p;
            }
        }
        if(trace)
            System.out.println("tryAttack(): No piece is able to attack " + myColor + " " + loc);
        return null;
    }

    //Simulate an attack to given piece!
    public Piece simulateAttack(Piece piece, PiecesLocation newLoc) {
        if(trace) {
            System.out.println("simulatedAttack(): " + piece + " ~~> " + newLoc);
        }
        //If there is a piece at the new location, make it invisible
        Piece org_piece = pieceAt(newLoc);
        if(org_piece != null)
            org_piece.visible = false;
        //Save the original location
        PiecesLocation org_loc = piece.location;
        //Change the location to the simulated location
        piece.location = newLoc;
        //Try to find an attacker
        Piece attacker = tryAttack(piece.color, newLoc);
        //Restore the original location to the piece
        piece.location = org_loc;
        //Restore the original piece to visible
        if(org_piece != null)
            org_piece.visible = true;
        return attacker;
    }

    private King getKing(PiecesColor color) {
        for(Piece p:pieces) {
            if(p.name == PiecesName.King && p.color == color) {
                return (King)p;
            }
        }
        return null;
    }

    //Game controller: Check
    public Status tryCheck(PiecesColor enemy_color) {
        if(trace) {
            System.out.println("tryCheck(" + enemy_color + ")");
        }
        //Find out the enemy King
        King enemy_king = getKing(enemy_color);
        if(enemy_king == null) {
            if(trace)
                System.out.println("tryCheck(): " + enemy_color + " King not found! BUG!!!");
            return new Status(enemy_color + " King not found! Where is he?");
        }

        //Find out attacker
        Status status = new Status();
        Piece attacker = tryAttack(enemy_color, enemy_king.location);
        if(attacker == null) {
            //System.out.println("tryCheck(): No 'Check'");
            return status;
        }
        if(trace)
            System.out.println("tryCheck(): " + attacker + " ---E " + enemy_king);

        //Add check status
        status.add(new PiecesAlteration(
                attacker,
                enemy_king.location,
                PiecesAction.Check));
        logs.add(new PiecesLog(attacker, enemy_king));
        return status;
    }

    //Game Controller: Next Turn
    public Status tryNextTurns(boolean ackCheck) {
        PiecesColor enemy_color = getEnemyColor();
        //Promotion Pawn?
        //Term: 2-(3)
        Status status ;
        Pawn pawn = getPromotionPawn();
        if(pawn != null) {
            if(trace)
                System.out.println("tryNextTurns(): Before next turns, perform Promotion Pawn.");
            status = new Status();
            status.add(new PiecesAlteration(
                    pawn,
                    pawn.location,
                    PiecesAction.Promotion));
            return status;
        }

        //Check?
        if(!ackCheck) {
            status = tryCheck(enemy_color);
            if(status.size() != 0) {
                if(trace)
                    System.out.println("tryNextTurns(): Before next turns, Check enemy king.");
                if(isCheckmate()) {
                    if(trace)
                        System.out.println("tryNextTurns(): Checkmate! " + enemy_color + " Lose!");
                    PiecesAlteration pa = status.get(0);
                    status = new Status(true, false, "因为" + enemy_color + "无法'应将'因而输棋",
                            new Result(PiecesAction.Checkmate));
                    status.add(new PiecesAlteration(
                            pa.piece,
                            pa.location,
                            PiecesAction.Checkmate
                    ));
                }
                return status;
            }
        }

        //Perpetual Check?
        //Term: 3-(2)-i
        status = tryPerpetualCheck();
        if(status.size() != 0)
            return status;

        //Is make repetition for 3 times?
        //Term: 3-(2)-ii
        if(isRepetition()) {
            Piece piece = getKing(enemy_color);
            assert piece != null;
            status.add(new PiecesAlteration(
                    piece,
                    piece.location,
                    PiecesAction.Repetition
            ));
            status.endGame = true;
            status.result = new Result(PiecesAction.Repetition);
            status.note = thisColor() + " 因为当前局面已经重复3次而判定为和棋";
            return status;
        }

        //Ok now turn to another color
        toggleTurns();

        //Is stalemate?
        if(isStalemate()) {
            Piece piece = getKing(enemy_color);
            PiecesAction action;
            if(ackCheck) {
                //Term: 3-(1)
                action = PiecesAction.Checkmate;
                status.note = thisColor() + " 因为无子可动而无法'应将'因而输棋";
                status.result = new Result(getEnemyColor(), action);
            }
            else {
                //Term: 3-(2)-iii
                action = PiecesAction.Stalemate;
                status.note = thisColor() + " 因为无子可动而被逼和";
                status.result = new Result(action);
            }
            assert piece != null;
            status.add(new PiecesAlteration(
                    piece,
                    piece.location,
                    action
            ));
            status.endGame = true;
            return status;
        }

        return status;
    }

    private boolean isStalemate() {
        if(trace) {
            System.out.println("isStalemate(" + thisColor() + ")");
        }
        int total_valid_move = 0;
        for(Piece p:pieces) {
            if(!p.visible)
                continue;
            if(p.color != thisColor())
                continue;
            ArrayList<PiecesLocation> valid_loc = p.findValidMove();
			/*
			if(trace) {
				System.out.println("Piece " + p + " may move to " + valid_loc.size() + " location(s).");
				for(PiecesLocation pl: valid_loc)
					System.out.println("    -> " + pl);
			}
			*/
            total_valid_move += valid_loc.size();
        }
        if(total_valid_move == 0) {
            if(trace)
                System.out.println("isStalemate(): All " + thisColor() + " pieces are frozen!");
            return true;
        }
        if(trace)
            System.out.println("isStalemate(): " + thisColor() + " has " + total_valid_move + " moves.");
        return false;
    }

    private Status tryPerpetualCheck() {
        if(trace) {
            System.out.println("tryPerpetualCheck(" + thisColor() + ")");
        }
        Status status = new Status();
        if(logs.size() < 6) {
            if(trace)
                System.out.println("tryPerpetualCheck(): logs < 6" );
            return status;
        }
        PiecesColor enemy_color = getEnemyColor();
        int check_counter = 0;
        for(int i=logs.size()-1; i>=logs.size()-6; i-=2) {
            PiecesLog pl_check = logs.get(i);
            PiecesLog pl_move = logs.get(i-1);
            if(trace) {
                System.out.println("tryPerpetualCheck(): log: " + pl_check);
                System.out.println("tryPerpetualCheck(): log: " + pl_move);
            }
            if(pl_check.color != thisColor() || pl_check.action != PiecesAction.Check) {
                return status;
            }
            if(pl_move.color != enemy_color || pl_move.action != PiecesAction.Move) {
                return status;
            }
            check_counter ++;
            if(check_counter == 3) {
                if(trace) {
                    System.out.println("tryPerpetualCheck(): counter = " + check_counter);
                }
                Piece piece = getKing(enemy_color);
                assert piece != null;
                status.add(new PiecesAlteration(
                        piece,
                        piece.location,
                        PiecesAction.PerpetualCheck
                ));
                status.endGame = true;
                status.result = new Result(PiecesAction.PerpetualCheck);
                status.note = thisColor() + " 因为持续对 " + enemy_color
                        + " 发动将军达到3次并且 " + enemy_color + " 无法避免而判定为长将和棋";
                return status;
            }
        }
        return status;
    }

    private boolean isRepetition() {
        if(logs.size() < 3)
            return false;
        //Find wave peak
        String peak = logs.get(logs.size()-1).toString();
        //Calculate wave length
        int wave_length = 0;
        for(int i=logs.size()-2; i>0; i--)
            if(logs.get(i).toString().equals(peak)) {
                wave_length = logs.size() - 1 - i;
                break;
            }
        if(wave_length < 4) {
            if(trace)
                System.out.println("isRepetition(): Short wave length: " + wave_length);
            return false;
        }
        if(trace)
            System.out.println("isRepetition(): Wave Length: " + wave_length);
        //Compare waves for 3 cycles
        int position = logs.size()-1;
        for(int wave=0; wave < wave_length; wave++) {
            //Pickup wave sample
            if(position-wave < 0) {
                if(trace)
                    System.out.println("isRepetition(): Short wave: "
                            + position + "-" + wave);
                return false;
            }
            String sample = logs.get(position-wave).toString();
            for(int cycle=1; cycle < 3; cycle++ ) {
                if(position-wave_length*cycle-wave < 0) {
                    if(trace)
                        System.out.println("isRepetition(): Short cycle: "
                                + position + "-" + wave_length
                                + "*" + cycle + "-" + wave);
                    return false;
                }
                String next_cycle = logs.get(position-wave_length*cycle-wave).toString();
                if(!sample.equals(next_cycle)) {
                    if(trace) {
                        System.out.println("isRepetition(): Difference moves: " + sample);
                        System.out.println("\tCycle-" + cycle + ": " + next_cycle);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isCheckmate() {
        PiecesColor enemy_color = getEnemyColor();
        //For each enemy's piece
        for(Piece piece: pieces) {
            if(piece.color() != enemy_color)
                continue;
            PiecesLocation org_location = piece.location;
            //Move this piece to each location to see if it can stop checking
            for(int col=0; col<8; col++) {
                for(int row=0; row<8; row++) {
                    PiecesLocation new_location = new PiecesLocation(col, row);
                    Piece piece_at_new_location = pieceAt(new_location);
                    if(piece_at_new_location == null) {
                        if(!piece.tryMoveTo(new_location, true))
                            continue;
                        piece.location = new_location;
                        Status status = tryCheck(enemy_color);
                        piece.location = org_location;
                        if(status.size() == 0) {
                            if(trace)
                                System.out.println("isCheckmate(): If " + piece + " moved to " + new_location + " "
                                        + enemy_color + " King can escape from Check!");
                            return false;
                        }
                    }
                    else if(piece_at_new_location.color() != enemy_color) {
                        if(!piece.tryEatAt(new_location, true))
                            continue;
                        piece_at_new_location.visible = false;
                        piece.location = new_location;
                        Status status = tryCheck(enemy_color);
                        piece.location = org_location;
                        piece_at_new_location.visible = true;
                        if(status.size() == 0) {
                            if(trace)
                                System.out.println("isCheckmate(): If " + piece + " eaten " + piece_at_new_location
                                        + " " + enemy_color + " King can escape from Check!");
                            return false;
                        }
                    }
                }
            }
        }
        if(trace)
            System.out.println("isCheckmate(): " + enemy_color + "  is NOT able to escape from Check!");
        return true;
    }

    //Save chess to a file
    public void save(String file) throws IOException {
        if(file == null)
            file = "chess_save.txt";
        //Task2-6
        PrintWriter pw = new PrintWriter(file);
        //Board size
        pw.println("8x8");
        //Turns
        //Task2-2
        pw.println(thisColor().toString());
        //Number of pieces
        pw.println(pieces.size());
        //Output each pieces
        for(Piece p:pieces)
            pw.println(p);

        //Save the moving logs
        //Task2-4
        //Number of logs
        pw.println(logs.size());
        //Output each moves
        for(PiecesLog l:logs)
            pw.println(l);
        pw.close();

        if(trace)
            System.out.println("Saved to " + file);
    }

    //Load chess from a file
    public void load(String file) throws Exception {
        //Task2-5-(4)
        if(file == null)
            file = "chess_save.txt";
        if(!file.contains(".txt")){
            JOptionPane.showMessageDialog(null,"并非文本文件","读取错误",JOptionPane.ERROR_MESSAGE);
        }
        //Task2-1
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line ;
        //Board size
        line = br.readLine();
        //Task2-5-(1)
        if(line.indexOf("8x8") != 0) {
            br.close();
            fr.close();
            JOptionPane.showMessageDialog(null,"棋盘并非8*8","读取错误",JOptionPane.ERROR_MESSAGE);
            throw new Exception("Chess board is not 8x8!");
        }
        //Turns
        line = br.readLine();
        //Will throw exception if color was invalid
        //Task2-2, Task5-3-(3)
        if(!line.equals("White") && !line.equals("Black")){
            JOptionPane.showMessageDialog(null,"缺少下一步行棋方","读取错误",JOptionPane.ERROR_MESSAGE);
        }
        thisTurnColor = PiecesColor.valueOf(line);
        //Number of pieces
        //Task2-2,
        line = br.readLine();
        int counter = Integer.parseInt(line);
        //Load each pieces
        pieces = new ArrayList<>();
        while(counter > 0) {
            String[] parms = br.readLine().split(" ");
            //Will throw exception if color was invalid
            //Task2-5-(2)
            PiecesColor color = null;
            try{
                color = PiecesColor.valueOf(parms[0]);
            }catch (Exception e){
                JOptionPane.showMessageDialog(null,"存在非黑白棋子","读取错误",JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            //Will throw exception if name was invalid
            //Task2-5-(2)
            PiecesName name = null;
            try{
                name = PiecesName.valueOf(parms[1]);
            }catch (Exception e){
                JOptionPane.showMessageDialog(null,"存在非合法棋子","读取错误",JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            //Will throw exception if the location is out of board
            //Task2-5-(5)
            PiecesLocation loc = null;
            try{
                loc = new PiecesLocation(parms[2]);
            }catch (Exception e){
                JOptionPane.showMessageDialog(null,"存储步骤非法","读取错误",JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            //Will throw exception if the movement status is wrong
            boolean moved = Boolean.parseBoolean(parms[3]);
            //If this is a Pawn, there is another flag
            if(name == PiecesName.Pawn) {
                boolean twoSteps = Boolean.parseBoolean(parms[4]);
                pieces.add(new Pawn(this, color, loc, moved, twoSteps));
            } else {
                pieces.add(newPiece(color, name, loc, moved));
            }
            counter--;
        }
        //Number of logs
        //Task2-4
        line = br.readLine();
        counter = Integer.parseInt(line);
        //Load each logs
        logs = new ArrayList<>();
        while(counter > 0) {
            String[] params = br.readLine().split(" ");
            StringBuilder note = new StringBuilder();
            for(int i=2; i<params.length; i++)
                note.append(params[i]);
            logs.add(new PiecesLog(
                    PiecesColor.valueOf(params[0]), //Will throw exception if color was invalid
                    PiecesAction.valueOf(params[1]),  //Will throw exception if action was error
                    note.toString()
            ));
            counter--;
        }

        //Close
        br.close();
        fr.close();

        if(trace)
            System.out.println("Loaded from " + file);
    }}

