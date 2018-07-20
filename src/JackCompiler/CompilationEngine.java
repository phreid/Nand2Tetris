package JackCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

class CompilationEngine {
    private JackTokenizer tokenizer;
    private PrintWriter writer;

    CompilationEngine(JackTokenizer tokenizer, File outFile)
            throws FileNotFoundException {
        this.tokenizer = tokenizer;

        writer = new PrintWriter(outFile);
    }

    void compileClass() {
        writer.println("<class>");

        tokenizer.advance();
        writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");

        tokenizer.advance();
        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        tokenizer.advance();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");

        tokenizer.advance();

        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyWord().equals("static") ||
                        tokenizer.keyWord().equals("field"))) {
            compileClassVarDec();
        }

        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyWord().equals("constructor") ||
                        tokenizer.keyWord().equals("function") ||
                        tokenizer.keyWord().equals("method"))) {
            compileSubroutine();
        }

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");

        writer.println("</class>");
    }

    private void compileSubroutine() {
        writer.println("<subroutineDec>");
        writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
            writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        tokenizer.advance();
        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        tokenizer.advance();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL)
            compileParameterList();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");

        tokenizer.advance();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");

        tokenizer.advance();
        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                tokenizer.keyWord().equals("var")) {
            compileVarDec();
        }

        compileStatements();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");

        tokenizer.advance();
        writer.println("</subroutineDec>");

    }

    private void compileStatements() {
        writer.println("<statements>");



        writer.println("</statements>");
    }

    private void compileVarDec() {
        writer.println("<varDec>");

        writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
            writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        tokenizer.advance();
        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        tokenizer.advance();
        while (tokenizer.symbol() != ';') {
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();

            if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
                writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
            else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)
                writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            tokenizer.advance();

            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            tokenizer.advance();
        }

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("</varDec>");
    }

    private void compileParameterList() {
        boolean first = true;

        writer.println("<parameterList>");

        while(tokenizer.symbol() != ')') {
            if (! first) {
                writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                tokenizer.advance();
            }

            if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
                writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
            else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)
                writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");

            tokenizer.advance();
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");

            first = false;

            tokenizer.advance();
        }

        writer.println("</parameterList>");
    }

    private void compileClassVarDec() {
        writer.println("<classVarDec>");
        writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
            writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        tokenizer.advance();
        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");

        tokenizer.advance();
        while (tokenizer.symbol() != ';') {
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            tokenizer.advance();
        }

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        writer.println("</classVarDec>");

        tokenizer.advance();
    }

/*    private void advanceAndCheck(JackTokenizer.TokenType type) {
        if (! tokenizer.hasMoreTokens()) {
            throw new IllegalArgumentException("Out of tokens.");
        }

        tokenizer.advance();

        if (tokenizer.tokenType() != type) {
            throw new IllegalArgumentException(
                    "Expected " + type + ", received: " + tokenizer.tokenType()
            );
        }
    }

    private void compileSymbol(char symbol) {
        advanceAndCheck(JackTokenizer.TokenType.SYMBOL);

        if (! (tokenizer.symbol() == symbol)) {
            throw new IllegalArgumentException(
                    "Expected: " + symbol + ", received: " + tokenizer.symbol()
            );
        }

        char c = tokenizer.symbol();
        String s = Character.toString(c);

        if (c == '<') s = "&lt;";
        if (c == '"') s = "&quot;";
        if (c == '>') s = "&gt;";
        if (c == '&') s = "&amp;";

        writer.println("<symbol> " + s + " </symbol>");
    }

    private void compileKeyword(String keyword) {
        advanceAndCheck(JackTokenizer.TokenType.KEYWORD);

        if (! tokenizer.keyWord().equals(keyword)) {
            throw new IllegalArgumentException(
                "Expected: " + keyword + ", received: " + tokenizer.keyWord()
            );
        }

        writer.println("<keyword> " + keyword + " </keyword>");
    }

    private void compileIdentifier() {
        advanceAndCheck(JackTokenizer.TokenType.IDENTIFIER);

        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
    }

    private void handle(String token, JackTokenizer.TokenType type) {
        if (! tokenizer.hasMoreTokens()) {
            throw new IllegalArgumentException("Out of tokens.");
        }

        tokenizer.advance();

        String body = null;
        String tag = null;
        String curToken = null;

        switch (tokenizer.tokenType()) {
            case INT_CONST:
                body = curToken = Integer.toString(tokenizer.intVal());
                tag = "integerConstant";
                break;
            case KEYWORD:
                body = curToken = tokenizer.keyWord();
                tag = "keyword";
                break;
            case SYMBOL:
                body = curToken = Character.toString(tokenizer.symbol());
                tag = "symbol";

                if (curToken.equals("<")) body = "&lt;";
                if (curToken.equals("\"")) body = "&quot;";
                if (curToken.equals(">")) body = "&gt;";
                if (curToken.equals("&")) body = "&amp;";

                break;
            case STRING_CONST:
                body = curToken = tokenizer.stringVal();
                tag = "stringConstant";
                break;
            case IDENTIFIER:
                body = curToken = tokenizer.identifier();
                tag = "identifier";
        }

        if (! token.equals(curToken)) {
            throw new IllegalArgumentException("Token doesn't match.");
        }

        writer.println("<" + tag + "> " + body + " </" + tag + ">");
    }

    void compileExpression() {
        writer.println("<expression>");

        compileTerm();

        writer.println("</expression>");
    }

    void compileTerm() {
        writer.println("<term>");

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST)
            writer.println("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST)
            writer.println("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");

        writer.println("</term>");
    }

    void compileStatements() {
        writer.println("<statements>");

        System.out.println(tokenizer.peek());

        while (tokenizer.hasMoreTokens() &&
                (tokenizer.peek().equals("let") ||
                        tokenizer.peek().equals("while"))) {
            compileStatement();
        }

        writer.println("</statements>");
    }

    void compileStatement() {
        String next = tokenizer.peek();

        if (next.equals("while"))
            compileWhile();
        else if (next.equals("let"))
            compileLet();
    }

    void compileWhile() {
        writer.println("<whileStatement>");
        compileKeyword("while");
        compileSymbol('(');
        compileExpression();
        compileSymbol(')');
        compileSymbol('{');
        compileStatements();
        compileSymbol('}');
        writer.println("</whileStatement");
    }

    void compileLet() {
        writer.println("<letStatement>");
        compileKeyword("let");
        compileIdentifier();
        compileSymbol('=');
        compileExpression();
        compileSymbol(';');
        writer.println("</letStatement>");
    }*/

    void close() {
        writer.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        File inFile = new File("test.txt");
        File outFile = new File("out.txt");
        JackTokenizer tk = new JackTokenizer(inFile);
        CompilationEngine c = new CompilationEngine(tk, outFile);

        c.compileClass();
        c.close();
    }

}
