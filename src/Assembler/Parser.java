package Assembler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Parser {
    private int c;
    private ArrayList<String> commands;
    private String curCmd;

    static final int A_COMMAND = 0;
    static final int C_COMMAND = 1;
    static final int L_COMMAND = -1;

    Parser(String filepath) throws IOException {
        commands = new ArrayList<>();
        c = 0;

        File file = new File(filepath);
        Scanner scanner;

        scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line = line.replace(" ", "");
            int i = line.indexOf("/");
            if (i != -1) line = line.substring(0, i);
            if (! line.isEmpty()) commands.add(line);
        }

        scanner.close();
    }

    boolean hasMoreCommands() {
        return c < commands.size();
    }

    void advance() {
        curCmd = commands.get(c);
        c++;
    }

    void reset() {
        c = 0;
        curCmd = null;
    }

    int commandType() {
        char firstChar = curCmd.charAt(0);

        if (firstChar == '@') return A_COMMAND;
        else if (firstChar == '(') return L_COMMAND;
        else return C_COMMAND;
    }

    String symbol() {
        if (commandType() == A_COMMAND) {
            return curCmd.substring(1, curCmd.length());
        }
        // never called when C command
        return curCmd.substring(1, curCmd.length() - 1);
    }

    String dest() {
        int i = curCmd.indexOf('=');
        if (i != -1) {
            return curCmd.substring(0, i);
        }

        return null;
    }

    String comp() {
        int i = curCmd.indexOf('=');
        int j = curCmd.indexOf(';');

        if (i != -1 && j != -1) {
            return curCmd.substring(i, j);
        } else if (i == -1 && j != -1) {
            return curCmd.substring(0, j);
        } else if (i != -1 && j == -1) {
            return curCmd.substring(i + 1, curCmd.length());
        } else {
            return curCmd;
        }
    }

    String jump() {
        int i = curCmd.indexOf(';');
        if (i != -1) {
            return curCmd.substring(i + 1);
        }

        return null;
    }
}
