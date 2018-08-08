package JackCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class VMWriter {
    private PrintWriter writer;

    VMWriter(File outFile) throws FileNotFoundException {
        writer = new PrintWriter(outFile);
    }

    void writePush(String segment, int index) {
        writer.println("push " + segment + " " + index);
    }

    void writePop(String segment, int index) {
        writer.println("pop " + segment + " " + index);
    }

    void writeArithmetic(String command) {
        writer.println(command);
    }

    void writeLabel(String label) {
        writer.println("label " + label);
    }

    void writeGoto(String label) {
        writer.println("goto " + label);
    }

    void writeIf(String label) {
        writer.println("if-goto " + label);
    }

    void writeCall(String name, int nArgs) {
        writer.println("call " + name + " " + nArgs);
    }

    void writeFunction(String name, int nLocals) {
        writer.println("function " + name + " " + nLocals);
    }

    void writeReturn() {
        writer.println("return");
    }

    void close() {
        writer.close();
    }
}
