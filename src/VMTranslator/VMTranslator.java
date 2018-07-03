package VMTranslator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VMTranslator {
    public static void translate(String filePath) throws IOException {
        String outPath = filePath;
        File inFile = new File(filePath);
        ArrayList<String> toTranslate = new ArrayList<>();

        if (filePath.endsWith(".vm")) {
            outPath = outPath.replace(".vm", ".asm");
            if (inFile.exists()) {
                toTranslate.add(filePath);
            }
        } else {
            outPath += ".asm";
            File[] inFiles = inFile.listFiles((dir, name) ->
                    name.endsWith(".vm"));
            for (File f : inFiles) {
                toTranslate.add(f.getPath());
            }
        }

        if (toTranslate.isEmpty()) {
            throw new IOException("No .vm files found.");
        }

        File outFile = new File(outPath);
        if (outFile.exists()) outFile.delete();
        CodeWriter cw = new CodeWriter(outFile);

        for (String inFilePath : toTranslate) {
            Parser p = new Parser(inFilePath);
            cw.setFileName(inFilePath);

            while (p.hasMoreCommands()) {
                p.advance();

                if (p.commandType() == Parser.C_ARITHMETIC) {
                    cw.writeArithmetic(p.arg1());
                } else if (p.commandType() == Parser.C_PUSH ||
                        p.commandType() == Parser.C_POP) {
                    cw.writePushPop(p.commandType(), p.arg1(), p.arg2());
                }
            }
        }

        cw.close();
    }

    public static void main(String[] args) throws IOException {
        //String inFile = args[0];
        String inFile = "07";

        VMTranslator.translate(inFile);
    }
}
