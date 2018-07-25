package JackCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class JackAnalyzer {

    public static void main(String[] args) throws FileNotFoundException {
        File inFile = new File(args[0]);
        String outPath = inFile.getParent() +
                "\\out\\" +
                inFile.getName().replace(".jack", ".xml");
        File outFile = new File(outPath);
        if (outFile.exists()) outFile.delete();
        PrintWriter writer = new PrintWriter(outFile);

        JackTokenizer tk = new JackTokenizer(inFile);
        CompilationEngine ce = new CompilationEngine(tk, outFile);

        ce.compileClass();
        ce.close();
    }
}
