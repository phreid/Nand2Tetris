package JackCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class JackCompiler {
    public static void compile(File inFile, boolean debug) throws FileNotFoundException {
        ArrayList<File> toTranslate = new ArrayList<>();
        String outPath = inFile.getAbsolutePath() + "\\o\\";

        if (inFile.getName().endsWith(".jack")) {
            if (inFile.exists()) {
                toTranslate.add(inFile);
            }

            outPath = inFile.getAbsoluteFile().getParent() + "\\o\\";
        } else {
            File[] inFiles = inFile.listFiles((dir, name) ->
                    name.endsWith(".jack"));
            for (File f : inFiles) {
                toTranslate.add(f);
            }
        }

        if (toTranslate.isEmpty()) {
            throw new FileNotFoundException("No .jack files found.");
        }

        new File(outPath).mkdirs();
        for (File f : toTranslate) {
            File outFile = new File(outPath +
                    f.getName().replace(".jack", ".vm"));
            if (outFile.exists()) outFile.delete();

            JackTokenizer tk = new JackTokenizer(f);
            CompilationEngine ce = new CompilationEngine(tk, outFile);

            ce.setDebug(debug);
            ce.compileClass();
            ce.close();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        File inFile = new File(args[0]);

        if (args.length == 2 && args[1].equals("debug"))
            compile(inFile, true);
        else
            compile(inFile, false);
    }
}
