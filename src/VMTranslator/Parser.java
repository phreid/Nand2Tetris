package VMTranslator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Parser {
    private int c;
    private ArrayList<String> commands;
    private String[] curCmdArr;

    static final int C_ARITHMETIC = 1;
    static final int C_PUSH = 2;
    static final int C_POP = 3;
    static final int C_LABEL = 4;
    static final int C_GOTO = 5;
    static final int C_IF = 6;
    static final int C_CALL = 7;
    static final int C_RETURN = 8;
    static final int C_FUN = 9;

    static final String ADD = "add";
    static final String SUB = "sub";
    static final String NEG = "neg";
    static final String EQ = "eq";
    static final String GT = "gt";
    static final String LT = "lt";
    static final String AND = "and";
    static final String OR = "or";
    static final String NOT = "not";

    private static final String PUSH = "push";
    private static final String POP = "pop";
    private static final String LABEL = "label";
    private static final String GOTO = "goto";
    private static final String IF  = "if-goto";
    private static final String CALL = "call";
    private static final String RETURN = "return";
    private static final String FUN = "function";

    Parser(File inFile) throws IOException {
        commands = new ArrayList<>();
        c = 0;
        curCmdArr = new String[3];

        Scanner scanner = new Scanner(inFile);

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
        String curCmd = commands.get(c);

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
        } else if (first.equals(LABEL)) {
            return C_LABEL;
        } else if (first.equals(GOTO)) {
            return C_GOTO;
        } else if (first.equals(IF)) {
            return C_IF;
        } else if (first.equals(CALL)) {
            return C_CALL;
        } else if (first.equals(RETURN)) {
            return C_RETURN;
        } else {
            return C_FUN;
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
}
