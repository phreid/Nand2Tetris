package JackCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CompilationEngine {
    private JackTokenizer tokenizer;
    private PrintWriter writer;

    private final List<Character> opsList = Arrays.asList(
            '+', '-', '*', '/', '&', '|', '<', '>', '='
    );

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

        //if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL)
        compileParameterList();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("<subroutineBody>");

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                tokenizer.keyWord().equals("var")) {
            compileVarDec();
        }

        compileStatements();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("</subroutineBody>");

        writer.println("</subroutineDec>");

    }

    private void compileStatements() {
        writer.println("<statements>");

        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyWord().equals("do") ||
                        tokenizer.keyWord().equals("let") ||
                        tokenizer.keyWord().equals("while") ||
                        tokenizer.keyWord().equals("if")) ||
                        tokenizer.keyWord().equals("return")) {
            if (tokenizer.keyWord().equals("do"))
                compileDo();
            else if (tokenizer.keyWord().equals("let"))
                compileLet();
            else if (tokenizer.keyWord().equals("while"))
                compileWhile();
            else if (tokenizer.keyWord().equals("if"))
                compileIf();
            else if (tokenizer.keyWord().equals("return"))
                compileReturn();
        }

        writer.println("</statements>");
    }

    private void compileReturn() {
        writer.println("<returnStatement>");

        writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
        tokenizer.advance();

        if (! (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL))
            compileExpression();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("</returnStatement>");
    }

    private void compileIf() {
        writer.println("<ifStatement>");

        writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
        tokenizer.advance();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        compileExpression();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        compileStatements();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                tokenizer.keyWord().equals("else")) {
            writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
            tokenizer.advance();

            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();

            compileStatements();

            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
        }

        writer.println("</ifStatement>");
    }

    private void compileLet() {
        writer.println("<letStatement>");

        writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
        tokenizer.advance();

        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
        tokenizer.advance();

        if (tokenizer.symbol() == '[') {
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();

            compileExpression();

            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
        }

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        compileExpression();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("</letStatement>");
    }

    private void compileExpression() {
        writer.println("<expression>");

        compileTerm();

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                opsList.contains(tokenizer.symbol())) {
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            compileTerm();
        }

        writer.println("</expression>");
    }

    private void compileTerm() {
        writer.println("<term>");

        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            writer.println("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            writer.println("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyWord().equals("true") || tokenizer.keyWord().equals("false") ||
                        tokenizer.keyWord().equals("null") || tokenizer.keyWord().equals("this"))) {
            writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
            writer.println("<unaryOp> " + tokenizer.keyWord() + " </unaryOp>");
            tokenizer.advance();
            compileTerm();
        }

        writer.println("</term>");
    }

    private void compileDo() {
        writer.println("<doStatement>");

        writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
        tokenizer.advance();

        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
        tokenizer.advance();

        if (tokenizer.symbol() == '.') {
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();

            writer.println("<identifier> " + tokenizer.identifier() + "</identifier>");
            tokenizer.advance();
        }

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        compileExpressionList();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("</doStatement>");
    }

    private void compileWhile() {
        writer.println("<whileStatement>");

        writer.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
        tokenizer.advance();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        compileExpression();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        compileStatements();

        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();

        writer.println("</whileStatement>");
    }

    private void compileExpressionList() {
        writer.println("<expressionList>");

        if ( ! (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                tokenizer.symbol() != '(')) {
            compileExpression();
        }

        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                tokenizer.symbol() == ',') {
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            compileExpression();
        }

        writer.println("</expressionList>");
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
