package VMTranslator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VMTranslator {
    public static void translate(String filePath) throws IOException {
        String outPath = filePath;
        File in = new File(filePath);
        ArrayList<File> toTranslate = new ArrayList<>();

        if (filePath.endsWith(".vm")) {
            outPath = outPath.replace(".vm", ".asm");
            if (in.exists()) {
                toTranslate.add(in);
            }
        } else {
            outPath = in.getPath() + "\\" + in.getName() + ".asm";
            File[] inFiles = in.listFiles((dir, name) ->
                    name.endsWith(".vm"));
            for (File f : inFiles) {
                toTranslate.add(f);
            }
        }

        if (toTranslate.isEmpty()) {
            throw new IOException("No .vm files found.");
        }

        File outFile = new File(outPath);
        if (outFile.exists()) outFile.delete();
        CodeWriter cw = new CodeWriter(outFile);

        for (File inFile : toTranslate) {
            Parser p = new Parser(inFile);
            cw.setFileName(inFile.getName());

            while (p.hasMoreCommands()) {
                p.advance();

                if (p.commandType() == Parser.C_ARITHMETIC) {
                    cw.writeArithmetic(p.arg1());
                } else if (p.commandType() == Parser.C_PUSH ||
                        p.commandType() == Parser.C_POP) {
                    cw.writePushPop(p.commandType(), p.arg1(), p.arg2());
                } else if (p.commandType() == Parser.C_LABEL) {
                    cw.writeLabel(p.arg1());
                } else if (p.commandType() == Parser.C_GOTO) {
                    cw.writeGoto(p.arg1());
                } else if (p.commandType() == Parser.C_IF) {
                    cw.writeIf(p.arg1());
                } else if (p.commandType() == Parser.C_FUN) {
                    cw.writeFunction(p.arg1(), p.arg2());
                } else if (p.commandType() == Parser.C_RETURN) {
                    cw.writeReturn();
                } else if (p.commandType() == Parser.C_CALL) {
                    cw.writeCall(p.arg1(), p.arg2());
                }
            }
        }

        cw.close();
    }

    public static void main(String[] args) throws IOException {
        String inFile = args[0];
        VMTranslator.translate(inFile);
    }
}
