import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;

public class Board {
    private Cell[][] boardContents;
    private HashSet<Cell> cellsToUpdate = new HashSet<Cell>();
    private int width;
    private int height;

    private Rules rules;
    public Board(int width, int height, Rules rules){
        this.width = width;
        this.height = height;
        this.rules = rules;
        boardContents = new Cell[this.height][this.width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                boardContents[i][j] = new Cell(this, i, j);
            }
        }
        for(Cell[] row : boardContents){
            for(Cell cell : row){
                cell.assignNeighbours();
            }
        }
    }

    public void printBoard(){
        for(Cell[] row : boardContents){
            for(Cell cell : row){
                if(cell.isAlive()){
                    System.out.printf("o ");
                }
                else{
                    System.out.printf(". ");
                }
            }
            System.out.printf("%n");
        }
        for(int i = 0; i < boardContents[0].length; i++){
            System.out.printf("~ ");
        }
        System.out.printf("%n");
    }

    public Cell[][] getBoardContents(){
        return boardContents;
    }
    public void invert(int x, int y){
        boardContents[y][x].changeState();
    }

    public void checkForUpdates() {
        for (Cell[] row : boardContents) {
            for (Cell cell : row) {
                int livingNeighbours = cell.countAliveNeighbours();
                if ((livingNeighbours == rules.getNeighboursToBecomeAlive() && !cell.isAlive()) ||
                    (cell.isAlive() && livingNeighbours < rules.getMinimumNeighboursToLive()) ||
                    (cell.isAlive() && livingNeighbours > rules.getMaxNeighboursToLive())) {
                    cell.flagForUpdate();
                    
                    cellsToUpdate.add(cell);
                }
            }
        }
    }

    public void clear(){
        for(Cell[] row : boardContents){
            for(Cell cell : row){
                cell.setAlive(false);
            }
        }
    }

    public void updateBoard(){
        Iterator<Cell> cellIterator = cellsToUpdate.iterator();
        while(cellIterator.hasNext()){
            Cell cellToUpdate = cellIterator.next();
            if(cellToUpdate.needsUpdate()){ //Redundant check.
                cellToUpdate.update();
            }
        }
        cellsToUpdate.clear();
    }
}
