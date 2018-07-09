package VMTranslator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

class CodeWriter {
    private PrintWriter writer;
    private int labelCounter;
    private int returnCounter;
    private String staticPrefix;
    private String currentFunName;

    private static final String CONSTANT = "constant";
    private static final String LOCAL = "local";
    private static final String ARGUMENT = "argument";
    private static final String THIS = "this";
    private static final String THAT = "that";
    private static final String POINTER = "pointer";
    private static final String TEMP = "temp";
    private static final String STATIC = "static";

    CodeWriter(File outFile) throws IOException {
        labelCounter = 0;
        writer = new PrintWriter(outFile);
        currentFunName = "";

        writer.println("@$INIT");
        writer.println("0;JMP");
        writeEqSub();
        writeGtSub();
        writeLtSub();
        writer.println("($INIT)");
        writeBootstrap();
    }

    void setFileName(String inFileName) {
        staticPrefix = inFileName.substring(0, inFileName.indexOf('.'));
    }

    void writeArithmetic(String command) {
        switch (command) {
            case Parser.ADD:
                writeTwoArgFn("D+M");
                break;
            case Parser.SUB:
                writeTwoArgFn("M-D");
                break;
            case Parser.NEG:
                writeOneArgFn("-M");
                break;
            case Parser.EQ:
                writer.println("@$RIP" + labelCounter);
                writer.println("D=A");
                writer.println("@$EQ");
                writer.println("0;JMP");
                writer.println("($RIP" + labelCounter + ")");
                labelCounter++;
                break;
            case Parser.GT:
                writer.println("@$RIP" + labelCounter);
                writer.println("D=A");
                writer.println("@$GT");
                writer.println("0;JMP");
                writer.println("($RIP" + labelCounter + ")");
                labelCounter++;
                break;
            case Parser.LT:
                writer.println("@$RIP" + labelCounter);
                writer.println("D=A");
                writer.println("@$LT");
                writer.println("0;JMP");
                writer.println("($RIP" + labelCounter + ")");
                labelCounter++;
                break;
            case Parser.NOT:
                writeOneArgFn("!M");
                break;
            case Parser.AND:
                writeTwoArgFn("D&M");
                break;
            case Parser.OR:
                writeTwoArgFn("D|M");
                break;
        }
    }

    private void writeTwoArgFn(String comp) {
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("A=A-1");
        writer.println("M=" + comp);
    }

    private void writeOneArgFn(String comp) {
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=" + comp);
    }

    private void writeEqSub() {
        writer.println("($EQ)");
        writer.println("@R13");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("A=A-1");
        writer.println("D=D-M");
        writer.println("M=-1");
        writer.println("@$FALSE_EQ");
        writer.println("D;JNE");
        writer.println("@$END_EQ");
        writer.println("0;JMP");
        writer.println("($FALSE_EQ)");
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=0");
        writer.println("($END_EQ)");
        writer.println("@R13");
        writer.println("A=M");
        writer.println("0;JMP");
    }

    private void writeGtSub() {
        writer.println("($GT)");
        writer.println("@R13");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("A=A-1");
        writer.println("D=D-M");
        writer.println("M=-1");
        writer.println("@$FALSE_GT");
        writer.println("D;JGE");
        writer.println("@$END_GT");
        writer.println("0;JMP");
        writer.println("($FALSE_GT)");
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=0");
        writer.println("($END_GT)");
        writer.println("@R13");
        writer.println("A=M");
        writer.println("0;JMP");
    }

    private void writeLtSub() {
        writer.println("($LT)");
        writer.println("@R13");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("A=A-1");
        writer.println("D=D-M");
        writer.println("M=-1");
        writer.println("@$FALSE_LT");
        writer.println("D;JLE");
        writer.println("@$END_LT");
        writer.println("0;JMP");
        writer.println("($FALSE_LT)");
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=0");
        writer.println("($END_LT)");
        writer.println("@R13");
        writer.println("A=M");
        writer.println("0;JMP");
    }

    void writePushPop(int command, String segment, int index) {
        if (command == Parser.C_PUSH) {
            switch (segment) {
                case CONSTANT:
                    writer.println("@" + index);
                    writer.println("D=A");
                    writer.println("@SP");
                    writer.println("AM=M+1");
                    writer.println("A=A-1");
                    writer.println("M=D");
                    break;
                case LOCAL:
                    writePush("LCL", index);
                    break;
                case ARGUMENT:
                    writePush("ARG", index);
                    break;
                case THIS:
                    writePush("THIS", index);
                    break;
                case THAT:
                    writePush("THAT", index);
                    break;
                case POINTER:
                    writer.println("@" + (3 + index));
                    writer.println("D=M");
                    writer.println("@SP");
                    writer.println("AM=M+1");
                    writer.println("A=A-1");
                    writer.println("M=D");
                    break;
                case TEMP:
                    writer.println("@" + (5 + index));
                    writer.println("D=M");
                    writer.println("@SP");
                    writer.println("AM=M+1");
                    writer.println("A=A-1");
                    writer.println("M=D");
                    break;
                case STATIC:
                    writer.println("@" + staticPrefix + "." + index);
                    writer.println("D=M");
                    writer.println("@SP");
                    writer.println("AM=M+1");
                    writer.println("A=A-1");
                    writer.println("M=D");
                    break;
            }
        } else if (command == Parser.C_POP) {
            switch (segment) {
                case LOCAL:
                    writePop("LCL", index);
                    break;
                case ARGUMENT:
                    writePop("ARG", index);
                    break;
                case THIS:
                    writePop("THIS", index);
                    break;
                case THAT:
                    writePop("THAT", index);
                    break;
                case POINTER:
                    writer.println("@SP");
                    writer.println("AM=M-1");
                    writer.println("D=M");
                    writer.println("@" + (3 + index));
                    writer.println("M=D");
                    break;
                case TEMP:
                    writer.println("@SP");
                    writer.println("AM=M-1");
                    writer.println("D=M");
                    writer.println("@" + (5 + index));
                    writer.println("M=D");
                    break;
                case STATIC:
                    writer.println("@SP");
                    writer.println("AM=M-1");
                    writer.println("D=M");
                    writer.println("@" + staticPrefix + "." + index);
                    writer.println("M=D");
                    break;
            }

        }
    }

    private void writePush(String segment, int index) {
        writer.println("@" + index);
        writer.println("D=A");
        writer.println("@" + segment);
        writer.println("A=D+M");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("AM=M+1");
        writer.println("A=A-1");
        writer.println("M=D");
    }

    private void writePop(String segment, int index) {
        writer.println("@" + index);
        writer.println("D=A");
        writer.println("@" + segment);
        writer.println("D=D+M");
        writer.println("@R13");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@R13");
        writer.println("A=M");
        writer.println("M=D");
    }

    void writeLabel(String label) {
        label = currentFunName + "$" + label;
        writer.println("(" + label + ")");
    }

    void writeGoto(String label) {
        label = currentFunName + "$" + label;
        writer.println("@" + label);
        writer.println("0;JMP");
    }

    void writeIf(String label) {
        label = currentFunName + "$" + label;
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@" + label);
        writer.println("D;JNE");
    }

    void writeCall(String funName, int nArgs) {
        String label = funName + "$RET$" + returnCounter;
        int argOffset = Math.max(nArgs - 5, 0);
        returnCounter++;

        writer.println("@" + label);
        writer.println("D=A");
        writer.println("@SP");
        writer.println("AM=M+1");
        writer.println("A=A-1");
        writer.println("M=D");
        writer.println("@LCL");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("AM=M+1");
        writer.println("A=A-1");
        writer.println("M=D");
        writer.println("@ARG");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("AM=M+1");
        writer.println("A=A-1");
        writer.println("M=D");
        writer.println("@THIS");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("AM=M+1");
        writer.println("A=A-1");
        writer.println("M=D");
        writer.println("@THAT");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("AM=M+1");
        writer.println("A=A-1");
        writer.println("M=D");
        writer.println("@" + nArgs);
        writer.println("D=A");
        writer.println("@SP");
        writer.println("D=M-D");
        writer.println("@ARG");
        writer.println("M=D");
        writer.println("@5");
        writer.println("D=A");
        writer.println("@ARG");
        writer.println("M=M-D");
        writer.println("@SP");
        writer.println("D=M");
        writer.println("@LCL");
        writer.println("M=D");
        writer.println("@" + funName);
        writer.println("0;JMP");
        writer.println("(" + label + ")");
    }

    void writeReturn() {
        writer.println("@LCL");
        writer.println("D=M");
        writer.println("@R13");
        writer.println("M=D");
        writer.println("@5");
        writer.println("D=A");
        writer.println("@R13");
        writer.println("A=M-D");
        writer.println("D=M");
        writer.println("@R14");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@ARG");
        writer.println("A=M");
        writer.println("M=D");
        writer.println("D=A+1");
        writer.println("@SP");
        writer.println("M=D");
        writer.println("@R13");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@THAT");
        writer.println("M=D");
        writer.println("@R13");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@THIS");
        writer.println("M=D");
        writer.println("@R13");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@ARG");
        writer.println("M=D");
        writer.println("@R13");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@LCL");
        writer.println("M=D");
        writer.println("@R14");
        writer.println("A=M");
        writer.println("0;JMP");
    }

    void writeFunction(String funName, int nLocals) {
        currentFunName = funName;
        writer.println("(" + funName + ")");
        for (int i = 0; i < nLocals; i++) {
            writePushPop(Parser.C_PUSH, CONSTANT, 0);
        }
    }

    private void writeBootstrap() {
        writer.println("@256");
        writer.println("D=A");
        writer.println("@SP");
        writer.println("M=D");
        writeCall("Sys.init", 0);
    }

    void close() {
        writer.println("(END)");
        writer.println("@END");
        writer.println("0;JMP");
        writer.close();
    }
}
