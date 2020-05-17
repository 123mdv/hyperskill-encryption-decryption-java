package encryptdecrypt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {
    static String algorithm = null;
    static String mode = "enc";
    static int key = 0;
    static String inputDataSource = "";
    static String inputData = "";
    static String inputFile = "";
    static String outputFile = "";

    public static void main(String[] args) {
        interpretArguments(args);
        EncryptionDecryption encryptionDecryption;
        if ("unicode".equals(algorithm)) {
            encryptionDecryption = new UnicodeEncryptionDecryption(mode, key, inputData, outputFile);
        } else {
            encryptionDecryption = new ShiftEncryptionDecryption(mode, key, inputData, outputFile);
        }
        encryptionDecryption.encryptDecrypt(); // this is the template method, which has transformData() and deliverOutput() steps
    }

    private static void interpretArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-mode":
                    mode = args[i + 1];
                    break;
                case "-key":
                    key = Integer.parseInt(args[i + 1]);
                    break;
                case "-data":
                    inputDataSource = "stdin";
                    inputData = args[i + 1];
                    break;
                case "-in":
                    if (!"stdin".equals(inputDataSource)) {
                        inputDataSource = "file";
                        inputFile = args[i + 1];
                        inputData = FileProcessor.readFile(inputFile);
                    }
                    break;
                case "-out":
                    outputFile = args[i + 1];
                    break;
                case "-alg":
                    algorithm = args[i + 1];
                    break;
                default:
            }
        }
    }
}

class FileProcessor {

    static String readFile(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            return scanner.nextLine();
        } catch (FileNotFoundException e) {
            System.out.println("Can't find input file: " + fileName);
            return null;
        }
    }

    static void writeFile(String data, String fileName) {
        File file = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.print(data);
        } catch (FileNotFoundException e) {
            System.out.println("Can't find output file: " + fileName);
        }
    }

}

abstract class EncryptionDecryption {
    String mode;
    int key;
    String inputData;
    String outputFile;
    String outputData = "";

    public EncryptionDecryption(String mode, int key, String inputData, String outputFile) {
        this.mode = mode;
        this.key = key;
        this.inputData = inputData;
        this.outputFile = outputFile;
    }

    public void encryptDecrypt() {
        transformData();
        deliverOutputData();
    }

    public abstract void transformData();

    private void deliverOutputData() {
        // print outputData to stdout or outputFile
        if (outputFile == "") {
            System.out.println(outputData);
        } else {
            FileProcessor.writeFile(outputData, outputFile);
        }
    }
}

class ShiftEncryptionDecryption extends EncryptionDecryption {
    final String alphabetLow = "abcdefghijklmnopqrstuvwxyz";
    final String alphabetHigh = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    char inputChar, outputChar;
    int indexOfInputChar, indexOfOutputChar;

    public ShiftEncryptionDecryption(String mode, int key, String inputData, String outputFile) {
        super(mode, key, inputData, outputFile);
    }

    @Override
    public void transformData() {

        for (int i = 0; i < inputData.length(); i++) {
            inputChar = inputData.charAt(i);
            if (alphabetLow.contains(String.valueOf(inputChar))) {
                indexOfInputChar = alphabetLow.indexOf(inputChar);
                if ("dec".equals(mode)) {
                    indexOfOutputChar = (indexOfInputChar + 26 - key % 26) % 26;
                } else {
                    indexOfOutputChar = (indexOfInputChar + key) % 26;
                }
                outputChar = alphabetLow.charAt(indexOfOutputChar);
            } else if (alphabetHigh.contains(String.valueOf(inputChar))) {
                indexOfInputChar = alphabetHigh.indexOf(inputChar);
                if ("dec".equals(mode)) {
                    indexOfOutputChar = indexOfInputChar + 26 - (key % 26);
                } else {
                    indexOfOutputChar = (indexOfInputChar + key) % 26;
                }
                outputChar = alphabetHigh.charAt(indexOfOutputChar);
            } else {
                outputChar = inputChar;
            }
            outputData += outputChar;
        }
    }
}

class UnicodeEncryptionDecryption extends EncryptionDecryption {

    public UnicodeEncryptionDecryption(String mode, int key, String inputData, String outputFile) {
        super(mode, key, inputData, outputFile);
    }

    @Override
    public void transformData() {
        int key2 = "dec".equals(mode) ? -key : key;
        for (char ch : inputData.toCharArray()) {
            outputData += ((char) (ch + key2));
        }
    }
}

