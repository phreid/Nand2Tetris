package VMTranslator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

class CodeWriter {
    private PrintWriter writer;

    CodeWriter(String filePath) throws IOException {
        setFileName(filePath);
    }

    void setFileName(String filePath) throws IOException {
        File f = new File(filePath);
        if (f.exists()) f.delete();

        writer = new PrintWriter(f);
    }

    void writeArithmetic(String command) {
        switch (command) {
            case Parser.ADD:
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("D=M");
                writer.println("A=A-1");
                writer.println("M=D+M");
                writer.println("@SP");
                writer.println("M=M-1");
                break;
            case Parser.SUB:
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("D=M");
                writer.println("A=A-1");
                writer.println("M=M-D");
                writer.println("@SP");
                writer.println("M=M-1");
                break;
            case Parser.NEG:
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("M=-M");
                break;
            case Parser.EQ:

        }
    }

    void writePushPop(int command, String segment, int index) {
        if (command == Parser.C_PUSH) {
            switch (segment) {
                case Parser.CONSTANT:
                    writer.println("@" + index);
                    writer.println("D=A");
                    writer.println("@SP");
                    writer.println("A=M");
                    writer.println("M=D");
                    writer.println("@SP");
                    writer.println("M=M+1");
            }
        }
    }

    void close() {
        writer.close();
    }
}
