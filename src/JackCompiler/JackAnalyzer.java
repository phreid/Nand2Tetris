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

        writer.println("<tokens>");
        while (tk.hasMoreTokens()) {
            tk.advance();

            JackTokenizer.TokenType tokenType = tk.tokenType();
            if (tokenType == JackTokenizer.TokenType.IDENTIFIER) {
                writer.println(
                        "<identifier> " + tk.identifier() + " </identifier>"
                );
            } else if (tokenType == JackTokenizer.TokenType.KEYWORD) {
                writer.println(
                        "<keyword> " + tk.keyWord() + " </keyword>"
                );
            } else if (tokenType == JackTokenizer.TokenType.INT_CONST) {
                writer.println(
                        "<integerConstant> " + tk.intVal() + " </integerConstant>"
                );
            } else if (tokenType == JackTokenizer.TokenType.STRING_CONST) {
                writer.println(
                        "<stringConstant> " + tk.stringVal() + " </stringConstant>"
                );
            } if (tokenType == JackTokenizer.TokenType.SYMBOL) {
                char c = tk.symbol();
                String s = Character.toString(c);

                if (c == '<') s = "&lt;";
                if (c == '"') s = "&quot;";
                if (c == '>') s = "&gt;";
                if (c == '&') s = "&amp;";

                writer.println("<symbol> " + s + " </symbol>");
            }
        }
        writer.println("</tokens>");
        writer.close();
    }
}
