import java.io.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Scanner;

public class GolConverter {

    public static void convertRawStringToGolHEXSave(String inputString, String saveName, File saveDirectory) throws IOException{
        
        File outputFile;
        FileWriter fw;
                
        outputFile = new File(saveDirectory.getAbsolutePath() + "/" + saveName + ".golHEX");
        fw = new FileWriter(outputFile);
        fw.write(encodeValue(inputString));
        fw.close();       
    }

    private static String encodeValue(String input){ //Takes in a raw string format of a .gol without any whitespace, returns raw golHEX string.
        CharacterIterator characterIterator = new StringCharacterIterator(input);

        StringBuilder sb = new StringBuilder();

        int count = 1;
        sb.append(characterIterator.first()); //adding relevant metadata

        while(characterIterator.current() != characterIterator.DONE){
            if(characterIterator.current() == characterIterator.next()){
                count++;
            }
            else{
                if(count == 1){ //Prevents bloating filesize by adding the additional character separating a 1.
                    sb.append('#');
                }
                else{
                    sb.append(Integer.toHexString(count));
                    sb.append("/");
                    count = 1;
                }
            }
        }

        return sb.toString();
    }

    public String decodeGolHEXFile(File inputFile){
        try{
            File fileToDecode = inputFile;
            Scanner scanner = new Scanner(fileToDecode);

            String stringToDecode = scanner.nextLine();

            CharacterIterator streamToDecode = new StringCharacterIterator(stringToDecode);
            char currentCellState = streamToDecode.current();

            StringBuilder currentValue = new StringBuilder();
            StringBuilder result = new StringBuilder();

            while(streamToDecode.current() != CharacterIterator.DONE){
                char currentChar = streamToDecode.next();

                if(currentChar == '/' || currentChar == '#'){
                    if(currentChar == '/'){
                        for(int i = 0; i < Integer.decode("0x" + currentValue); i++){ //0x required so that Integer.decode knows it's hexadecimal.
                            result.append(currentCellState);
                        }

                        currentValue.setLength(0);
                    }
                    else{
                        result.append(currentCellState);
                    }

                    if(currentCellState == '.'){ //Invert the cell state being recorded after either a # or /
                        currentCellState = 'o';
                    }
                    else{
                        currentCellState = '.';
                    }

                }
                else{
                    currentValue.append(currentChar);
                }
            }
            scanner.close();
            return result.toString();
        }
        catch (FileNotFoundException fileNotFoundException){
            System.out.println("ERROR, FILE NOT FOUND");
            fileNotFoundException.printStackTrace();
        }
        return "ERROR";
    }
}
