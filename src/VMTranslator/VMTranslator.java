package VMTranslator;

import java.io.IOException;

public class VMTranslator {
    public static void translate(String filePath) throws IOException {
        String outPath;
        int i = filePath.indexOf('.');
        if (i == -1) {
            outPath = filePath;
        } else {
            outPath = filePath.substring(0, i);
        }
        outPath = outPath.concat(".asm");

        Parser p = new Parser(filePath);
        CodeWriter cw = new CodeWriter(outPath);

        while (p.hasMoreCommands()) {
            p.advance();

            if (p.commandType() == Parser.C_ARITHMETIC) {
                cw.writeArithmetic(p.arg1());
            } else if (p.commandType() == Parser.C_PUSH ||
                    p.commandType() == Parser.C_POP) {
                cw.writePushPop(p.commandType(), p.arg1(), p.arg2());
            }
        }

        cw.close();
    }

    public static void main(String[] args) throws IOException {
        String file = "test.vm";

        VMTranslator.translate(file);
    }
}
