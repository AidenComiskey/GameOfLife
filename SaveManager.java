import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Scanner;

public class SaveManager {

    public static boolean saveBoardState(File saveDirectory, String saveName, Board board, SaveFormat sFormat){

        if(!saveDirectory.isDirectory()){
            return false;
        }
        else{
            StringBuilder boardRaw = new StringBuilder();

            for(Cell[] row : board.getBoardContents()){
                for(Cell cell : row){
                    if(cell.isAlive()){
                        boardRaw.append('o');
                    }
                    else{
                        boardRaw.append('.');
                    }
                }
                boardRaw.append("\n");
            }

            try{
                switch(sFormat){
                    case GOL:
                        FileWriter fw = new FileWriter(new File(saveDirectory.getAbsolutePath() + "/" + saveName + ".gol"));
                        fw.write(boardRaw.toString());
                        fw.close();
                        break;
                    case GOLHEX:
                        GolConverter.convertRawStringToGolHEXSave(boardRaw.toString().replace("\n",""), saveName, saveDirectory); //Removing the newlines to make the format work for convertRawStringToSave
                        break;
                }
            }
            catch (IOException ioException){
                ioException.printStackTrace();
                return false;
            }

            return true;
        }
    }

    public static boolean loadBoardState(File saveFile, Board board) throws FileNotFoundException { //Include file extension
        String saveName = saveFile.getName();

        int extensionPos = saveName.lastIndexOf('.');
        String extension = saveName.substring(extensionPos + 1);

        String rawState = "";

        Boolean fileLoaded = false;

        if(extension.equals("golHEX")){
            GolConverter golConverter = new GolConverter();
            rawState = golConverter.decodeGolHEXFile(saveFile);
            fileLoaded = true;
        }
        else if(extension.equals("gol")){
            Scanner scanner = new Scanner(saveFile);
            while(scanner.hasNextLine()){
                rawState += scanner.nextLine();
            }
            scanner.close();
            fileLoaded = true;
        }

        board.clear();

        CharacterIterator characterIterator = new StringCharacterIterator(rawState);


        for(Cell[] row : board.getBoardContents()){
            for(Cell cell : row){
                if(characterIterator.current() == 'o'){
                    cell.setAlive(true);
                }
                else{
                    cell.setAlive(false);
                }
                characterIterator.next();
            }
        }

        return fileLoaded;
    }
}
