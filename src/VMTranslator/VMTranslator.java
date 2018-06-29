package VMTranslator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VMTranslator {
    public static void translate(String filePath) throws IOException {
        String outPath = filePath;
        ArrayList<String> toTranslate = new ArrayList<>();

        if (filePath.endsWith(".vm")) {
            outPath = outPath.replace(".vm", ".asm");
            toTranslate.add(filePath);
        } else {
            outPath = outPath.concat(".asm");

            File inFolder = new File(filePath);
            File[] inFiles = inFolder.listFiles((dir, name) ->
                    name.endsWith(".vm"));
            for (File f : inFiles) {
                toTranslate.add(f.getAbsolutePath());
            }
        }

        File outFile = new File(outPath);
        if (outFile.exists()) outFile.delete();
        CodeWriter cw = new CodeWriter(outFile);

        for (String inFile : toTranslate) {
            Parser p = new Parser(inFile);
            cw.setFileName(inFile);

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
        String inFile = "xyz.vm";

        VMTranslator.translate(inFile);
    }
}
