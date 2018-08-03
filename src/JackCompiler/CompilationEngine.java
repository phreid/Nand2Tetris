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
    private SymbolTable symbolTable;

    private final List<Character> opsList = Arrays.asList(
            '+', '-', '*', '/', '&', '|', '<', '>', '='
    );

    CompilationEngine(JackTokenizer tokenizer, File outFile)
            throws FileNotFoundException {
        this.tokenizer = tokenizer;

        writer = new PrintWriter(outFile);
        symbolTable = new SymbolTable();
    }

    private void handle(JackTokenizer.TokenType type) {
        String tag = "";
        String body = "";

        if (tokenizer.tokenType() != type) {
            throw new IllegalArgumentException(
                    "ERROR: Expected: " + type + " Received: " + tokenizer.tokenType());
        }

        switch (type) {
            case SYMBOL:
                tag = "symbol";
                char c = tokenizer.symbol();
                String s = Character.toString(c);

                if (c == '<')
                    s = "&lt;";
                if (c == '>')
                    s = "&gt;";
                if (c == '"')
                    s = "&quot;";
                if (c == '&')
                    s = "&amp;";

                body = s;
                break;
            case KEYWORD:
                tag = "keyword";
                body = tokenizer.keyWord();
                break;
            case IDENTIFIER:
                tag = "identifier";
                body = tokenizer.identifier();
                break;
            case STRING_CONST:
                tag = "stringConstant";
                body = tokenizer.stringVal();
                break;
            case INT_CONST:
                tag = "integerConstant";
                body = Integer.toString(tokenizer.intVal());
        }
        
        writer.println("<" + tag + "> " + body + " </" + tag + ">");

        if (! tokenizer.hasMoreTokens()) {
            throw new IllegalArgumentException("ERROR: Out of tokens");
        }

        tokenizer.advance();
    }

    void handleIdentifier(String cat, boolean beingDefined, boolean add) {
        String tag = "<identifier> ";
        String endTag = " </identifier>";

        String name = tokenizer.identifier();
        String def = beingDefined ? "defined" : "used";

        String body = name + ";" + cat + ";" + def;
        body += symbolTable.kindOf(name);

        if (symbolTable.kindOf(name) != SymbolTable.Kind.NONE)
            body += ";" + symbolTable.indexOf(name);

        writer.println(tag + body + endTag);
    }

    void compileClass() {
        writer.println("<class>");
        tokenizer.advance();

        handle(JackTokenizer.TokenType.KEYWORD);
        handle(JackTokenizer.TokenType.IDENTIFIER);
        handle(JackTokenizer.TokenType.SYMBOL);

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
        handle(JackTokenizer.TokenType.KEYWORD);

        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
            handle(JackTokenizer.TokenType.KEYWORD);
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)
            handle(JackTokenizer.TokenType.IDENTIFIER);

        handle(JackTokenizer.TokenType.IDENTIFIER);
        handle(JackTokenizer.TokenType.SYMBOL);
        compileParameterList();
        handle(JackTokenizer.TokenType.SYMBOL);

        writer.println("<subroutineBody>");
        handle(JackTokenizer.TokenType.SYMBOL);

        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                tokenizer.keyWord().equals("var")) {
            compileVarDec();
        }

        compileStatements();
        handle(JackTokenizer.TokenType.SYMBOL);

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
        handle(JackTokenizer.TokenType.KEYWORD);

        if (! (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL))
            compileExpression();

        handle(JackTokenizer.TokenType.SYMBOL);
        writer.println("</returnStatement>");
    }

    private void compileIf() {
        writer.println("<ifStatement>");

        handle(JackTokenizer.TokenType.KEYWORD);
        handle(JackTokenizer.TokenType.SYMBOL);
        compileExpression();
        handle(JackTokenizer.TokenType.SYMBOL);
        handle(JackTokenizer.TokenType.SYMBOL);
        compileStatements();
        handle(JackTokenizer.TokenType.SYMBOL);

        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                tokenizer.keyWord().equals("else")) {
            handle(JackTokenizer.TokenType.KEYWORD);
            handle(JackTokenizer.TokenType.SYMBOL);
            compileStatements();
            handle(JackTokenizer.TokenType.SYMBOL);
        }

        writer.println("</ifStatement>");
    }

    private void compileLet() {
        writer.println("<letStatement>");
        handle(JackTokenizer.TokenType.KEYWORD);
        handle(JackTokenizer.TokenType.IDENTIFIER);

        if (tokenizer.symbol() == '[') {
            handle(JackTokenizer.TokenType.SYMBOL);
            compileExpression();
            handle(JackTokenizer.TokenType.SYMBOL);
        }

        handle(JackTokenizer.TokenType.SYMBOL);
        compileExpression();
        handle(JackTokenizer.TokenType.SYMBOL);

        writer.println("</letStatement>");
    }

    private void compileExpression() {
        writer.println("<expression>");

        compileTerm();

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                opsList.contains(tokenizer.symbol())) {
            handle(JackTokenizer.TokenType.SYMBOL);
            compileTerm();
        }

        writer.println("</expression>");
    }

    private void compileTerm() {
        writer.println("<term>");

        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            handle(JackTokenizer.TokenType.INT_CONST);
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            handle(JackTokenizer.TokenType.STRING_CONST);
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                (tokenizer.keyWord().equals("true") || tokenizer.keyWord().equals("false") ||
                        tokenizer.keyWord().equals("null") || tokenizer.keyWord().equals("this"))) {
            handle(JackTokenizer.TokenType.KEYWORD);
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            String next = tokenizer.peek();

            if (next.equals(".")) {
                handleSubCall();
            } else if (next.equals("[")) {
                handle(JackTokenizer.TokenType.IDENTIFIER);
                handle(JackTokenizer.TokenType.SYMBOL);
                compileExpression();
                handle(JackTokenizer.TokenType.SYMBOL);
            } else {
                handle(JackTokenizer.TokenType.IDENTIFIER);
            }
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                tokenizer.symbol() == '(') {
            handle(JackTokenizer.TokenType.SYMBOL);
            compileExpression();
            handle(JackTokenizer.TokenType.SYMBOL);
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
            handle(JackTokenizer.TokenType.SYMBOL);
            compileTerm();
        }

        writer.println("</term>");
    }

    private void compileDo() {
        writer.println("<doStatement>");
        handle(JackTokenizer.TokenType.KEYWORD);
        handleSubCall();
        handle(JackTokenizer.TokenType.SYMBOL);
        writer.println("</doStatement>");
    }

    private void handleSubCall() {
        handle(JackTokenizer.TokenType.IDENTIFIER);

        if (tokenizer.symbol() == '.') {
            handle(JackTokenizer.TokenType.SYMBOL);
            handle(JackTokenizer.TokenType.IDENTIFIER);
        }

        handle(JackTokenizer.TokenType.SYMBOL);
        compileExpressionList();
        handle(JackTokenizer.TokenType.SYMBOL);
    }

    private void compileWhile() {
        writer.println("<whileStatement>");
        handle(JackTokenizer.TokenType.KEYWORD);
        handle(JackTokenizer.TokenType.SYMBOL);
        compileExpression();
        handle(JackTokenizer.TokenType.SYMBOL);
        handle(JackTokenizer.TokenType.SYMBOL);
        compileStatements();
        handle(JackTokenizer.TokenType.SYMBOL);
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
            handle(JackTokenizer.TokenType.SYMBOL);
            compileExpression();
        }

        writer.println("</expressionList>");
    }

    private void compileVarDec() {
        writer.println("<varDec>");
        handle(JackTokenizer.TokenType.KEYWORD);

        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
            handle(JackTokenizer.TokenType.KEYWORD);
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)
            handle(JackTokenizer.TokenType.IDENTIFIER);

        handle(JackTokenizer.TokenType.IDENTIFIER);

        while (tokenizer.symbol() != ';') {
            handle(JackTokenizer.TokenType.SYMBOL);
            handle(JackTokenizer.TokenType.IDENTIFIER);
        }

        handle(JackTokenizer.TokenType.SYMBOL);

        writer.println("</varDec>");
    }

    private void compileParameterList() {
        boolean first = true;

        writer.println("<parameterList>");

        while(tokenizer.symbol() != ')') {
            if (! first) {
                handle(JackTokenizer.TokenType.SYMBOL);
            }

            if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
                handle(JackTokenizer.TokenType.KEYWORD);
            else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)
                handle(JackTokenizer.TokenType.IDENTIFIER);

            handle(JackTokenizer.TokenType.IDENTIFIER);
            first = false;
        }

        writer.println("</parameterList>");
    }

    private void compileClassVarDec() {
        writer.println("<classVarDec>");
        handle(JackTokenizer.TokenType.KEYWORD);

        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
            handle(JackTokenizer.TokenType.KEYWORD);
        else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)
            handle(JackTokenizer.TokenType.IDENTIFIER);

        handle(JackTokenizer.TokenType.IDENTIFIER);

        while (tokenizer.symbol() != ';') {
            handle(JackTokenizer.TokenType.SYMBOL);
            handle(JackTokenizer.TokenType.IDENTIFIER);
        }

        handle(JackTokenizer.TokenType.SYMBOL);
        writer.println("</classVarDec>");
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

        new File("o").mkdirs();
    }

}
