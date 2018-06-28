package VMTranslator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

class CodeWriter {
    private PrintWriter writer;
    private int labelCounter;
    private String staticPrefix;

    private static final String CONSTANT = "constant";
    private static final String LOCAL = "local";
    private static final String ARGUMENT = "argument";
    private static final String THIS = "this";
    private static final String THAT = "that";
    private static final String POINTER = "pointer";
    private static final String TEMP = "temp";
    private static final String STATIC = "static";

    CodeWriter(String filePath) throws IOException {
        labelCounter = 0;
        setFileName(filePath);
    }

    void setFileName(String filePath) throws IOException {
        File f = new File(filePath);
        if (f.exists()) f.delete();

        int i = filePath.indexOf('.');
        int j = filePath.lastIndexOf('\\');
        if (i == -1) {
            staticPrefix = filePath;
        } else {
            staticPrefix = filePath.substring(0, i);
        } if (j != -1) {
            staticPrefix = staticPrefix.substring(j + 1, staticPrefix.length());
        }

        writer = new PrintWriter(f);
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
                writeCompare("JNE");
                break;
            case Parser.GT:
                writeCompare("JGE");
                break;
            case Parser.LT:
                writeCompare("JLE");
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
        writer.println("A=M-1");
        writer.println("D=M");
        writer.println("A=A-1");
        writer.println("M=" + comp);
        writer.println("@SP");
        writer.println("M=M-1");
    }

    private void writeOneArgFn(String comp) {
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=" + comp);
    }

    private void writeCompare(String condition) {
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("A=A-1");
        writer.println("D=D-M");
        writer.println("M=-1");
        writer.println("@FALSE$" + labelCounter);
        writer.println("D;" + condition);
        writer.println("@END$" + labelCounter);
        writer.println("0;JMP");
        writer.println("(FALSE$" + labelCounter + ")");
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=0");
        writer.println("(END$" + labelCounter + ")");
        labelCounter++;
    }

    void writePushPop(int command, String segment, int index) {
        if (command == Parser.C_PUSH) {
            switch (segment) {
                case CONSTANT:
                    writer.println("@" + index);
                    writer.println("D=A");
                    writer.println("@SP");
                    writer.println("A=M");
                    writer.println("M=D");
                    writer.println("@SP");
                    writer.println("M=M+1");
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
                    writer.println("A=M");
                    writer.println("M=D");
                    writer.println("@SP");
                    writer.println("M=M+1");
                    break;
                case TEMP:
                    writer.println("@" + (5 + index));
                    writer.println("D=M");
                    writer.println("@SP");
                    writer.println("A=M");
                    writer.println("M=D");
                    writer.println("@SP");
                    writer.println("M=M+1");
                    break;
                case STATIC:
                    writer.println("@" + staticPrefix + "." + index);
                    writer.println("D=M");
                    writer.println("@SP");
                    writer.println("A=M");
                    writer.println("M=D");
                    writer.println("@SP");
                    writer.println("M=M+1");
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
        writer.println("A=M");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("M=M+1");
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

    void close() {
        writer.println("(END)");
        writer.println("@END");
        writer.println("0;JMP");
        writer.close();
    }
}
