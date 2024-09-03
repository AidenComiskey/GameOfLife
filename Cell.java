import java.util.ArrayList;
import java.util.List;

public class Cell {
    private boolean alive = false;
    private final int row;
    private final int column;
    private List<Cell> neighbours  = new ArrayList<>();
    private Board parentBoard;
    private boolean updateFlag = false;
    public boolean needsUpdate(){
        return updateFlag;
    }

    public void flagForUpdate(){
        updateFlag = true;
    }
    public void update(){
        changeState();
        updateFlag = false;
    }
    public Cell(Board parentBoard, int row, int column){
        this.parentBoard = parentBoard;
        this.row = row;
        this.column = column;
    }

    public void changeState(){
        if(alive){
            alive = false;
        }
        else{
            alive = true;
        }
    }

    public boolean isAlive(){
        return alive;
    }

    public void setAlive(boolean bool){
        alive = bool;
    }

    public void assignNeighbours() {
        for (int i = this.row - 1; i <= this.row + 1; i++) {
            for (int j = this.column - 1; j <= this.column + 1; j++) {
                addNeighbour(i, j);
            }
        }
    }

    private void addNeighbour(int row, int column) {
        int numRows = parentBoard.getBoardContents().length;
        int numCols = parentBoard.getBoardContents()[0].length;

        row = (row + numRows) % numRows;
        column = (column + numCols) % numCols;

        if (row == this.row && column == this.column) {
            return;
        }

        neighbours.add(parentBoard.getBoardContents()[row][column]);
    }

    

    public int countAliveNeighbours(){
        int aliveNeighbours = 0;
        for(Cell cell : neighbours){
            if(cell.isAlive()){
                aliveNeighbours++;
            }
        }
        return  aliveNeighbours;
    }

}
