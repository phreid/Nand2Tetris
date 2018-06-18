package Assembler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {
    private int c;
    private ArrayList<String> commands;
    private String curCmd;

    static final int A_COMMAND = 0;
    static final int C_COMMAND = 1;
    static final int L_COMMAND = -1;

    public Parser(String filepath) {
        commands = new ArrayList<>();
        c = 0;

        File file = new File(filepath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = line.replace(" ", "");
                int i = line.indexOf("/");
                if (i != -1) line = line.substring(0, i);
                if (! line.isEmpty()) commands.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) scanner.close();
        }
    }

    public boolean hasMoreCommands() {
        return c < commands.size();
    }

    public void advance() {
        curCmd = commands.get(c);
        c++;
    }

    public void reset() {
        c = 0;
        curCmd = null;
    }

    public int commandType() {
        char firstChar = curCmd.charAt(0);

        if (firstChar == '@') return A_COMMAND;
        else if (firstChar == '(') return L_COMMAND;
        else return C_COMMAND;
    }

    public String symbol() {
        if (commandType() == A_COMMAND) {
            return curCmd.substring(1, curCmd.length());
        }
        // never called when C command
        return curCmd.substring(1, curCmd.length() - 1);
    }

    public String dest() {
        int i = curCmd.indexOf('=');
        if (i != -1) {
            return curCmd.substring(0, i);
        }

        return null;
    }

    public String comp() {
        int i = curCmd.indexOf('=');
        int j = curCmd.indexOf(';');

        if (i != -1 & j != -1) {
            return curCmd.substring(i, j);
        } else if (i == -1 & j != -1) {
            return curCmd.substring(0, j);
        } else if (i != -1 & j == -1) {
            return curCmd.substring(i + 1, curCmd.length());
        } else {
            return curCmd;
        }
    }

    public String jump() {
        int i = curCmd.indexOf(';');
        if (i != -1) {
            return curCmd.substring(i + 1);
        }

        return null;
    }

    public static void main(String[] args) {
        Parser p = new Parser("test.asm");

        String c = p.commands.toString();
        System.out.println(c);

        while (p.hasMoreCommands()) {
            p.advance();
            if (p.commandType() == C_COMMAND) {
                System.out.println(p.jump());
            }
        }
    }
}
