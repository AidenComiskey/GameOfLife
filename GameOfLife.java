import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

public class GameOfLife extends JFrame {
    private final int CELL_SIZE = 19; // Size of each cell
    private Board board;
    private Timer timer;
    private JButton startButton;
    private JButton pauseButton;
    private JButton stepButton;
    private JSlider speedSlider;

    private JButton saveButton;
    private JButton loadButton;

    private boolean paused = true;
    private boolean started = false;


    public GameOfLife(int width, int height, Rules rules) {
        setTitle("Conway's Game of Life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create a board with properties that the user has input
        board = new Board(width, height, rules);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }

            private void drawBoard(Graphics g) {
                Cell[][] cells = board.getBoardContents();
                for (int i = 0; i < cells.length; i++) {
                    for (int j = 0; j < cells[0].length; j++) {
                        g.setColor(cells[i][j].isAlive() ? Color.BLACK : Color.WHITE);
                        g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.LIGHT_GRAY);
                        g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        };
        // When a cell in the grid is clicked, change state/colour
        panel.setPreferredSize(new Dimension(width * CELL_SIZE, height * CELL_SIZE));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = e.getY() / CELL_SIZE;
                int col = e.getX() / CELL_SIZE;
                board.invert(col, row);
                panel.repaint();
            }
        });
        add(panel);
        // Creates start button that starts the game when clicked
        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!started){
                    startGame();
                    startButton.setVisible(false);
                }
            }
        });

        // Creates pause button that pauses/unpauses game when clicked
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!paused){
                    pauseGame();
                }
                else{
                    unpauseGame();
                }
            }
        });

        // Creates a step button that executes one move of the game when clicked
        stepButton = new JButton("Step"); // Create and configure the step button
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepGame(); // Call the stepGame method when the button is clicked
            }
        });

        // Creates a save button that when clicked, asks for you to choose a format, then directory, then name, before saving your board.
        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseGame();//Pauses game upon opening load dialogue

                // Allows the user to choose between the two save formats.
                int option = JOptionPane.showOptionDialog(
                    null, 
                    "Choose Format", 
                    "title", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, 
                    new String[]{".GOL",".GOLHEX"}, 
                    ".GOLHEX"
                );

                //By default saves as a normal .gol
                SaveFormat chosenFormat = SaveFormat.GOL;

                // Process the user's choice
                if (option == JOptionPane.YES_OPTION) {
                    chosenFormat = SaveFormat.GOL;
                } else if (option == JOptionPane.NO_OPTION) {
                    chosenFormat = SaveFormat.GOLHEX;
                } else {
                    System.out.println("Dialog closed or canceled");
                }
            
                //Choosing save name
                String saveName = JOptionPane.showInputDialog("Enter desired save name: (If a save of that name already exists, it WILL be overwritten!)");
                //Choose directory to save to
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int returnValue = fileChooser.showDialog(GameOfLife.this, "Select");
                
                if(returnValue == JFileChooser.APPROVE_OPTION){
                    File saveDir = (fileChooser.getSelectedFile());
                    if(!saveDir.exists() || !saveDir.isDirectory() || !SaveManager.saveBoardState(saveDir, saveName, board, chosenFormat)){ //Calls the save method. If the directory isn't real or the save fails, gives an error.
                        JOptionPane.showMessageDialog(panel, "Error: Invalid save location, please select a directory!");
                    }
                }
            }
        });
    // Creates a load button that when clicked allows you to choose a savefile to open, then loads it
        loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                pauseGame(); //Pauses game upon opening the load dialogue.
                
                JFileChooser fileChooser = new JFileChooser();
                
                FileNameExtensionFilter filter = new FileNameExtensionFilter(".gol and .golHEX", "gol", "golHEX"); //Adds a filter for only the valid file types.
                fileChooser.setFileFilter(filter);
                
                int returnValue = fileChooser.showOpenDialog(GameOfLife.this);
                
                if(returnValue == JFileChooser.APPROVE_OPTION){
                    try{
                        if(!SaveManager.loadBoardState(fileChooser.getSelectedFile(), board)){ //Try to load selected file, if it fails, show an error message.
                            JOptionPane.showMessageDialog(panel, "Error: Invalid save format, please select a .gol or .golHEX save file.");
                        }
                    }
                    catch(FileNotFoundException fileNotFoundException){
                        System.out.println("ERROR: File not found");
                        fileNotFoundException.printStackTrace();
                    }   
                }

                repaint(); //Redraws the board to reflect the changes.
            }
        });
        //Creates a panel for the slider and it's label.
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
        JLabel speedSliderLabel = new JLabel("Simulation Speed");
        //Creates a speed slider
        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e ->{
            if(started){
                int delay = 2200 - (speedSlider.getValue() * 200); // Recalculate delay based on slider value
                timer.cancel(); // Cancel the current timer

                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(!paused){
                            board.checkForUpdates();
                            board.updateBoard();
                            repaint();
                        }
                    }
                }, delay, delay); // Start a new timer with the updated delay   
            }
        });

        sliderPanel.add(speedSliderLabel, BorderLayout.NORTH);
        sliderPanel.add(speedSlider, BorderLayout.SOUTH);
    // Adds all the buttons to the interface
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stepButton); // Add the step button to the panel
        buttonPanel.add(sliderPanel);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }
    // Method that starts the game
    private void startGame() {
        started = true;
        paused = false;
        if (timer != null) {
            timer.cancel();
        }
        int delay = 2200 - (speedSlider.getValue()*200); // Calculate delay based on slider value
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!paused){
                    board.checkForUpdates();
                    board.updateBoard();
                    repaint();
                }
            }
        }, 0, delay); // Update every 'delay' milliseconds
    }
    //Method to pause the game
    private void pauseGame() {
        paused = true;
        pauseButton.setText("Unpause");
    }
    //Method to unpause the game
    private void unpauseGame(){
        paused = false;
        pauseButton.setText("Pause");
    }
    //Steps the game forward once.
    private void stepGame() {
        board.checkForUpdates();
        board.updateBoard();
        repaint();
    }

    /*
     * takeInputInteger creates a text pop up asking for a value, and will only procede if the entered value is a positive integer,
     * The program closes if the close or cancel button is used on the pop up window,
     * Returns the value the user enters.
     */
    private static int takeInputInteger(String message){
        String input = "";
        int value = -1;

        while(input != null && (input.trim().isEmpty() || value < 1)){
            input = JOptionPane.showInputDialog(message);
            try{
                value = Integer.parseInt(input);
                if(value < 1){
                    throw new NumberFormatException("Number cannot be negative");
                }
            }
            catch(NumberFormatException e){
                if(input == null){
                    System.exit(0);
                }
                JOptionPane.showMessageDialog(null, "Invalid input, please enter a positive integer");
            }
        }

        return value;
    }

    // Main method that asks the user for the rules, and starts the game based on the rules
    public static void main(String[] args) {

        int width = GameOfLife.takeInputInteger("Enter the width of the grid");
        int height = GameOfLife.takeInputInteger("Enter the height of the grid:");

        int minimumNeighboursToLive = GameOfLife.takeInputInteger("Enter the minimum number of neighbors for a cell to survive:");
        int maxNeighboursToLive = GameOfLife.takeInputInteger("Enter the maximum number of neighbors for a cell to survive:");
        int neighboursToBecomeAlive = GameOfLife.takeInputInteger("Enter the number of neighbors for a dead cell to become alive:");

        Rules rules = new Rules(minimumNeighboursToLive, maxNeighboursToLive, neighboursToBecomeAlive);

        SwingUtilities.invokeLater(() -> new GameOfLife(width, height, rules).setVisible(true));
    }
}









