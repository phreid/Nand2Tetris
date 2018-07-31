package JackCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class JackAnalyzer {
    public static void analyze(File inFile) throws FileNotFoundException {
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
                    f.getName().replace(".jack", ".xml"));
            if (outFile.exists()) outFile.delete();

            JackTokenizer tk = new JackTokenizer(f);
            CompilationEngine ce = new CompilationEngine(tk, outFile);

            ce.compileClass();
            ce.close();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        File inFile = new File(args[0]);

        //File inFile = new File("test.jack");
        analyze(inFile);
    }
}
