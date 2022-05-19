import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

public class ChessGame {
    static Chess chess;
    static JFrame mainFrame;
    static JMenu mainMenu;
    static JLabel mainLabel;
    static Board board;
    static int cellSize;

    private static String chooseFileName(boolean save) {
        JFileChooser file_chooser = new JFileChooser();
        file_chooser.setFileFilter(new FileNameExtensionFilter("文本文件","txt"));
        file_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int res;
        if(save)
            res = file_chooser.showSaveDialog(mainFrame.getContentPane());
        else
            res = file_chooser.showOpenDialog(mainFrame.getContentPane());
        if(res != JFileChooser.APPROVE_OPTION)
            return null;
        String file_name = file_chooser.getSelectedFile().getAbsolutePath();
        return file_name;
    }

    private static void saveChess() {
        String file_name = chooseFileName(true);
        if(file_name == null)
            return;
        try {
            chess.save(file_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadChess() {
        String file_name = chooseFileName(false);
        if(file_name == null)
            return;
        try {
            chess.load(file_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Paint board
        SwingUtilities.invokeLater(() -> board.paintBoard());
    }

    private static JMenu createFileMenu() {
        JMenu menu = new JMenu("文件");
        //Save
        JMenuItem s_item = new JMenuItem("保存棋局...");
        s_item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChess();
            }
        });
        menu.add(s_item);
        //Load
        JMenuItem r_item = new JMenuItem("读取棋局...");
        r_item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadChess();
                //TODO! Refresh board
            }
        });
        menu.add(r_item);
        //Line
        menu.addSeparator();
        //Reset
        JMenuItem clr_item = new JMenuItem("重新开始");
        clr_item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Reset
                chess.init();
                //Paint board
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        board.paintBoard();
                    }
                });
            }
        });
        menu.add(clr_item);
        //Line
        menu.addSeparator();
        //Debug
        JMenuItem t_item = new JMenuItem("打开/关闭调试信息");
        t_item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(chess.trace)
                    chess.trace = false;
                else
                    chess.trace = true;
            }
        });
        menu.add(t_item);
        //Line
        menu.addSeparator();
        //Quit
        JMenuItem c_item = new JMenuItem("关闭");
        c_item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(c_item);
        //Skin1
        JMenuItem a1_item = new JMenuItem("切换皮肤1");
        a1_item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.paintBoard();
            }
        });
        menu.add(a1_item);
        //Skin2
        JMenuItem a2_item = new JMenuItem("切换皮肤2");
        a2_item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.paintBoard2();
            }
        });
        menu.add(a2_item);
        //Stop bgm
        JMenuItem v_item = new JMenuItem("静音");
        v_item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chess.bgm.stop();
            }
        });
        menu.add(v_item);
        return menu;
    }

    private static void layoutFrame(Board board) {
        mainFrame = new JFrame("Chess of Group Castling 欢迎使用”王车易位组“的国际象棋!");
        //Piece icon size 150x150 -> 75x75
        int width = board.getPieceSize() * 8 + 40;
        mainFrame.setSize(width, width+70);
        mainFrame.setResizable(true);
        //Add close button
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Layout
        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(board, BorderLayout.CENTER);
        //Add MenuBar
        JMenuBar jmb = new JMenuBar();
        mainMenu = createFileMenu();
        jmb.add(mainMenu);
        jmb.setVisible(true);
        mainFrame.setJMenuBar(jmb);
        //Add Label
        mainLabel = new JLabel("欢迎使用”王车易位组“的国际象棋");
        mainFrame.add(mainLabel, BorderLayout.NORTH);
        //Display frame
        mainFrame.setVisible(true);
        //Set Window image
        mainFrame.setIconImage(new ImageIcon("window_image.jpg").getImage());
    }

    public static void main(String[] args) throws Exception {
        //Create a chess (Model)
        chess = new Chess();
        //Create a board (Visual)
        cellSize = 60;
        board = new Board(chess, cellSize);
        //Create a listener (Controller)
        BoardListener listener = new BoardListener(board);
        board.setupListener(listener);
        //Create main window
        layoutFrame(board);
        board.setLabel(mainLabel);
        //Paint board
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                board.paintBoard();
            }
        });
        //Process resize
        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //Find the size
                if(mainFrame.getWidth() < mainFrame.getHeight()-70)
                    cellSize = (mainFrame.getWidth()-40) / 8;
                else
                    cellSize = (mainFrame.getHeight()-70-40) / 8;
                board.setPieceSize(cellSize);
                mainFrame.setSize(cellSize*8+40, cellSize*8+70+40);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        board.paintBoard();
                    }
                });
            }
        });
        //Waiting for the end of game
        while(true) {
            try {
                Thread.sleep(1000);
            } catch(Exception e) {

            }
        }
    }
}
