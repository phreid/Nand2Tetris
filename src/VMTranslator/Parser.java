package VMTranslator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Parser {
    private int c;
    private ArrayList<String> commands;
    private String curCmd;
    private String[] curCmdArr;

    static final int C_ARITHMETIC = 1;
    static final int C_PUSH = 2;
    static final int C_POP = 3;

    static final String ADD = "add";
    static final String SUB = "sub";
    static final String NEG = "neg";
    static final String EQ = "eq";
    static final String GT = "gt";
    static final String LT = "lt";
    static final String AND = "and";
    static final String OR = "or";
    static final String NOT = "not";

    static final String PUSH = "push";
    static final String POP = "pop";

    static final String CONSTANT = "constant";


    Parser(String filepath) throws IOException {
        commands = new ArrayList<>();
        c = 0;
        curCmdArr = new String[3];

        File file = new File(filepath);
        Scanner scanner;

        scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int i = line.indexOf("/");
            if (i != -1) line = line.substring(0, i);
            line = line.trim();
            if (! line.isEmpty()) commands.add(line);
        }

        scanner.close();
    }

    boolean hasMoreCommands() {
        return c < commands.size();
    }

    void advance() {
        curCmd = commands.get(c);

        String[] args = curCmd.split(" ");
        for (int i = 0; i < args.length; i++) {
            curCmdArr[i] = args[i];
        }

        c++;
    }

    int commandType() {
        String first = curCmdArr[0];

        if (first.equals(ADD) ||
                first.equals(SUB) ||
                first.equals(NEG) ||
                first.equals(EQ) ||
                first.equals(GT) ||
                first.equals(LT) ||
                first.equals(AND) ||
                first.equals(OR) ||
                first.equals(NOT)) {
            return C_ARITHMETIC;
        } else if (first.equals(PUSH)) {
            return C_PUSH;
        } else if (first.equals(POP)) {
            return C_POP;
        } else {
            return -1;
        }
    }

    String arg1() {
        if (commandType() == C_ARITHMETIC) {
            return curCmdArr[0];
        }

        return curCmdArr[1];
    }

    int arg2() {
        return Integer.parseInt(curCmdArr[2]);
    }

    public static void main(String args[]) throws IOException {
        Parser p = new Parser("test.vm");
        System.out.println(p.commands.size());
        while (p.hasMoreCommands()) {
            p.advance();
            System.out.println(p.curCmd);
            System.out.println(p.commandType());
            System.out.println(p.arg1());

        }
    }
}
