package Assembler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Assembler {
    private SymbolTable symbolTable;

    public Assembler() {
        symbolTable = new SymbolTable();
    }

    private String translateC(String d, String c, String j) {
        String jt = Code.jump(j);
        String dt = Code.dest(d);
        String ct = Code.comp(c);
        String pre = "111";

        return pre + ct+ dt + jt;
    }

    private String translateA(String symbol) {
        String pre = "0";
        Integer dec;

        if (isNumeric(symbol)) {
            dec = Integer.parseInt(symbol);
        } else {
            if (! symbolTable.contains(symbol)) {
                symbolTable.addEntry(symbol);
            }

            dec = symbolTable.getAddress(symbol);
        }


        String bin = Integer.toBinaryString(dec);

        for (int i = 0; i < 15 - bin.length(); i++) {
            pre += "0";
        }

        return pre + bin;
    }

    private boolean isNumeric(String s) {
        if (s == null) return false;

        for (char c : s.toCharArray()) {
            if (c < '0' || c > '9') return false;
        }

        return true;
    }

    public void assemble(String inFile) throws IOException {
        String outFile = inFile.
                substring(0, inFile.indexOf('.')).
                concat(".hack");

        File f = new File(outFile);
        if (f.exists()) f.delete();

        Parser p = new Parser(inFile);
        PrintWriter writer = new PrintWriter(outFile);

        int romCounter = 0;
        while (p.hasMoreCommands()) {
            p.advance();
            if (p.commandType() == Parser.L_COMMAND) {
                symbolTable.addEntry(p.symbol(), romCounter);
            } else {
                romCounter++;
            }
        }

        p.reset();
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

    public static void main(String[] args) throws IOException {
        Assembler assembler = new Assembler();

        String inFile = args[0];
        assembler.assemble(inFile);
    }
}
