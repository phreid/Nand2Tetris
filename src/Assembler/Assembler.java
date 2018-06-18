package Assembler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Assembler {

    private static String translateC(String d, String c, String j) {
        String jt = Code.jump(j);
        String dt = Code.dest(d);
        String ct = Code.comp(c);
        String pre = "111";

        return pre + ct+ dt + jt;
    }

    private static String translateA(String symbol) {
        String pre = "0";

        Integer dec = Integer.parseInt(symbol);
        String bin = Integer.toBinaryString(dec);

        for (int i = 0; i < 15 - bin.length(); i++) {
            pre += "0";
        }

        return pre + bin;
    }

    public static void main(String[] args) throws IOException {
        String inFile = args[0];
        String outFile = inFile.
                substring(0, inFile.indexOf('.')).
                concat(".hack");

        File f = new File(outFile);
        if (f.exists()) f.delete();

        Parser p = new Parser(inFile);
        PrintWriter writer = new PrintWriter(outFile);

        while (p.hasMoreCommands()) {
            p.advance();
            if (p.commandType() == Parser.C_COMMAND) {
                writer.println(translateC(
                        p.dest(),
                        p.comp(),
                        p.jump())
                );
            } else if (p.commandType() == Parser.A_COMMAND) {
                writer.println(translateA(p.symbol()));
            }
        }

        writer.close();

    }
}
