package JackCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

class CompilationEngine {
    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;
    private VMWriter vm;
    private String className;
    private int ifCounter;
    private int whileCounter;
    private int curLocalCount;

    private final List<Character> opsList = Arrays.asList(
            '+', '-', '*', '/', '&', '|', '<', '>', '='
    );

    CompilationEngine(JackTokenizer tokenizer, File outFile)
            throws FileNotFoundException {
        this.tokenizer = tokenizer;

        symbolTable = new SymbolTable();
        vm = new VMWriter(outFile);
    }

    void setDebug(boolean b) {
        vm.setDebug(b);
    }

    /*private void handle(JackTokenizer.TokenType type) {
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
    }*/

    void compileClass() {
        /*writer.println("<class>");
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
        writer.println("</class>");*/
        tokenizer.advance(); // empty token

        tokenizer.advance(); // 'class'
        className = tokenizer.identifier();
        tokenizer.advance(); // class name
        tokenizer.advance(); // '{'

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
    }

    private void compileSubroutine() {
        /*writer.println("<subroutineDec>");
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
        writer.println("</subroutineDec>");*/
        curLocalCount = 0;
        symbolTable.startSubroutine();

        String fnType = tokenizer.keyWord();
        tokenizer.advance(); // constructor, function, or method
        tokenizer.advance(); // void or return type

        if (fnType.equals("method"))
            symbolTable.define("this", className, "argument");

        String fnName = tokenizer.identifier();
        tokenizer.advance(); // fn name
        tokenizer.advance(); // '('
        compileParameterList();
        tokenizer.advance(); // ')'
        tokenizer.advance(); // '{'

        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                tokenizer.keyWord().equals("var")) {
            compileVarDec();
        }

        vm.writeFunction(className + "." + fnName, curLocalCount);

        if (fnType.equals("method")) {
            vm.writePush("argument", 0);
            vm.writePop("pointer", 0);
        }

        if (fnType.equals("constructor")) {
            int fieldCount = symbolTable.varCount("field") +
                    symbolTable.varCount("static");

            vm.writePush("constant", fieldCount);
            vm.writeCall("Memory.alloc", 1);
            vm.writePop("pointer", 0);
        }

        compileStatements();

        if (tokenizer.hasMoreTokens())
            tokenizer.advance();
    }

    private void compileStatements() {
 /*       writer.println("<statements>");

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

        writer.println("</statements>");*/

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
    }

    private void compileReturn() {
        /*writer.println("<returnStatement>");
        handle(JackTokenizer.TokenType.KEYWORD);

        if (! (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL))
            compileExpression();

        handle(JackTokenizer.TokenType.SYMBOL);
        writer.println("</returnStatement>");*/

        tokenizer.advance(); // 'return'

        if (! (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL))
            compileExpression();
        else
            vm.writePush("constant", 0);

        vm.writeReturn();

        if (tokenizer.hasMoreTokens())
            tokenizer.advance(); // ';'
    }

    private void compileIf() {
/*        writer.println("<ifStatement>");

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

        writer.println("</ifStatement>");*/
        String labelTrue = "IF_TRUE" + ifCounter;
        String labelFalse = "IF_FALSE" + ifCounter;
        String labelEnd = "IF_END" + ifCounter++;

        tokenizer.advance(); // 'if'
        tokenizer.advance(); // '('

        compileExpression();

        tokenizer.advance(); // ')'
        tokenizer.advance(); // '{'

        //vm.writeArithmetic("not");
        vm.writeIf(labelTrue);
        vm.writeGoto(labelFalse);
        vm.writeLabel(labelTrue);
        compileStatements();

        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance(); // '}'
        }

        //vm.writeLabel(labelFalse);

        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                tokenizer.keyWord().equals("else")) {
            vm.writeGoto(labelEnd);
            vm.writeLabel(labelFalse);
            tokenizer.advance(); // 'else'
            tokenizer.advance(); // '{'
            compileStatements();
            vm.writeLabel(labelEnd);

            if (tokenizer.hasMoreTokens()) {
                tokenizer.advance(); // '}'
            }
        } else {
            vm.writeLabel(labelFalse);
        }

    }

    private void compileLet() {
       /* writer.println("<letStatement>");
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

        writer.println("</letStatement>");*/
        boolean isArray = false;

        tokenizer.advance(); // 'let'

        String identifier = tokenizer.identifier();
        String kind = symbolTable.kindOf(identifier);

        if (kind.equals("var"))
            kind = "local";

        if (kind.equals("field"))
            kind = "this";

        int index = symbolTable.indexOf(identifier);
        if (index == SymbolTable.INDEX_NOT_FOUND) {
            throw new IllegalArgumentException("Identifier not found: " + identifier);
        }

        tokenizer.advance(); // identifier

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                tokenizer.symbol() == '[') {
            tokenizer.advance(); // '['
            compileExpression();
            vm.writePush(kind, index);
            vm.writeArithmetic("add");
            tokenizer.advance(); // ']'
            isArray = true;
        }

        tokenizer.advance(); // '='

        compileExpression();

        if (isArray) {
            vm.writePop("temp", 0);
            vm.writePop("pointer", 1);
            vm.writePush("temp", 0);
            vm.writePop("that", 0);
        } else {
            vm.writePop(kind, index);
        }

        if (tokenizer.hasMoreTokens())
            tokenizer.advance(); // ';'
    }

    private void compileExpression() {
/*        writer.println("<expression>");

        compileTerm();

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                opsList.contains(tokenizer.symbol())) {
            handle(JackTokenizer.TokenType.SYMBOL);
            compileTerm();
        }

        writer.println("</expression>");*/

        compileTerm();

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                opsList.contains(tokenizer.symbol())) {
            char s = tokenizer.symbol();
            tokenizer.advance();

            compileTerm();

            switch (s) {
                case '+':
                    vm.writeArithmetic("add");
                    break;
                case '-':
                    vm.writeArithmetic("sub");
                    break;
                case '=':
                    vm.writeArithmetic("eq");
                    break;
                case '>':
                    vm.writeArithmetic("gt");
                    break;
                case '<':
                    vm.writeArithmetic("lt");
                    break;
                case '&':
                    vm.writeArithmetic("and");
                    break;
                case '|':
                    vm.writeArithmetic("or");
                    break;
                case '*':
                    vm.writeCall("Math.multiply", 2);
                    break;
                case '/':
                    vm.writeCall("Math.divide", 2);
            }
        }
    }

    private void compileTerm() {
/*        writer.println("<term>");

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
                compileSubroutineCall();
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

        writer.println("</term>");*/

        switch (tokenizer.tokenType()) {
            case INT_CONST:
                vm.writePush("constant", tokenizer.intVal());
                tokenizer.advance(); // term
                break;
            case STRING_CONST:
                char[] chars = tokenizer.stringVal().toCharArray();

                vm.writePush("constant", chars.length);
                vm.writeCall("String.new", 1);

                for (char c : chars) {
                    vm.writePush("constant", c);
                    vm.writeCall("String.appendChar", 2);
                }

                tokenizer.advance(); // term
                break;
            case KEYWORD:
                if (tokenizer.keyWord().equals("null") ||
                        tokenizer.keyWord().equals("false")) {
                    vm.writePush("constant", 0);
                } else if (tokenizer.keyWord().equals("true")) {
                    vm.writePush("constant", 0);
                    vm.writeArithmetic("not");
                } else if (tokenizer.keyWord().equals("this")) {
                    vm.writePush("pointer", 0);
                }
                tokenizer.advance(); // term
                break;
            case IDENTIFIER:
                String identifier = tokenizer.identifier();
                String kind = symbolTable.kindOf(identifier);
                int index = symbolTable.indexOf(identifier);

                if (kind.equals("var"))
                    kind = "local";

                if (kind.equals("field"))
                    kind = "this";

                String next = tokenizer.peek();

                if (next.equals(".")) {
                    compileSubroutineCall();
                } else if (next.equals("[")) {
                    tokenizer.advance(); // identifier
                    tokenizer.advance(); // '['
                    compileExpression();
                    vm.writePush(kind, index);
                    vm.writeArithmetic("add");
                    vm.writePop("pointer", 1);
                    vm.writePush("that", 0);
                    tokenizer.advance(); // ']'
                } else {
                    vm.writePush(kind, index);
                    tokenizer.advance(); // identifier
                }

                /*if (! kind.equals("none") && index != SymbolTable.INDEX_NOT_FOUND) {
                    if (kind.equals("var"))
                        kind = "local";

                    if (kind.equals("field"))
                        kind = "this";

                    vm.writePush(kind, index);
                    tokenizer.advance(); // identifier
                } else {
                    compileSubroutineCall();
                }*/
                break;
            case SYMBOL:
                if (tokenizer.symbol() == '(') {
                    tokenizer.advance(); // '('
                    compileExpression();
                    tokenizer.advance(); // ')'
                } else if (tokenizer.symbol() == '-') {
                    tokenizer.advance(); // '-'
                    compileTerm();
                    vm.writeArithmetic("neg");
                } else if (tokenizer.symbol() == '~') {
                    tokenizer.advance(); // '~'
                    compileTerm();
                    vm.writeArithmetic("not");
                }
                break;
        }

        /*if (tokenizer.hasMoreTokens())
            tokenizer.advance();*/
    }

    private void compileDo() {
        /*writer.println("<doStatement>");
        handle(JackTokenizer.TokenType.KEYWORD);
        compileSubroutineCall();
        handle(JackTokenizer.TokenType.SYMBOL);
        writer.println("</doStatement>");*/

        tokenizer.advance(); // 'do'
        compileSubroutineCall();
        vm.writePop("temp", 0);

        if (tokenizer.hasMoreTokens())
            tokenizer.advance(); // ';'
    }

    private void compileSubroutineCall() {
        /*handle(JackTokenizer.TokenType.IDENTIFIER);

        if (tokenizer.symbol() == '.') {
            handle(JackTokenizer.TokenType.SYMBOL);
            handle(JackTokenizer.TokenType.IDENTIFIER);
        }

        handle(JackTokenizer.TokenType.SYMBOL);
        compileExpressionList();
        handle(JackTokenizer.TokenType.SYMBOL);*/
        int nArgs = 0;

        String subName = tokenizer.identifier();
        tokenizer.advance(); // class or function name

        if (tokenizer.symbol() == '.') {
            tokenizer.advance(); // '.'
            if (symbolTable.indexOf(subName) == SymbolTable.INDEX_NOT_FOUND) {
                subName += "." + tokenizer.identifier();
            } else {
                String type = symbolTable.typeOf(subName);
                String kind = symbolTable.kindOf(subName);
                int index = symbolTable.indexOf(subName);

                subName = type + "." + tokenizer.identifier();

                if (kind.equals("var"))
                    kind = "local";

                if (kind.equals("field"))
                    kind = "this";

                vm.writePush(kind, index);
                nArgs++;
            }

            tokenizer.advance(); // method name
        } else {
            vm.writePush("pointer", 0);
            subName = className + "." + subName;
            nArgs++;
        }

        tokenizer.advance(); // '('
        nArgs += compileExpressionList();
        tokenizer.advance(); // ')'

        vm.writeCall(subName, nArgs);
    }

    private void compileWhile() {
        /*writer.println("<whileStatement>");
        handle(JackTokenizer.TokenType.KEYWORD);
        handle(JackTokenizer.TokenType.SYMBOL);
        compileExpression();
        handle(JackTokenizer.TokenType.SYMBOL);
        handle(JackTokenizer.TokenType.SYMBOL);
        compileStatements();
        handle(JackTokenizer.TokenType.SYMBOL);
        writer.println("</whileStatement>");*/

        String labelCont = "WHILE_EXP" + whileCounter;
        String labelEnd = "WHILE_END" + whileCounter++;

        tokenizer.advance(); // 'while'
        tokenizer.advance(); // '('
        vm.writeLabel(labelCont);
        compileExpression();
        tokenizer.advance(); // ')
        tokenizer.advance(); // '{'
        vm.writeArithmetic("not");
        vm.writeIf(labelEnd);
        compileStatements();
        vm.writeGoto(labelCont);
        vm.writeLabel(labelEnd);

        if (tokenizer.hasMoreTokens())
            tokenizer.advance(); // '}'
    }

    private int compileExpressionList() {
        /*writer.println("<expressionList>");

        if ( ! (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                tokenizer.symbol() != '(')) {
            compileExpression();
        }

        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                tokenizer.symbol() == ',') {
            handle(JackTokenizer.TokenType.SYMBOL);
            compileExpression();
        }

        writer.println("</expressionList>");*/
        int argCounter = 0;

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                tokenizer.symbol() == ')') {
            return argCounter;
        }

        compileExpression();
        argCounter++;

        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                tokenizer.symbol() == ',') {
            tokenizer.advance(); // ','
            compileExpression();
            argCounter++;
        }

        return argCounter;
    }

    private void compileVarDec() {
        /*writer.println("<varDec>");
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

        writer.println("</varDec>");*/
        String type;
        String varName;

        tokenizer.advance(); // kind declaration

        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD)
            type = tokenizer.keyWord();
        else // identifier
            type = tokenizer.identifier();

        do {
            tokenizer.advance();
            varName = tokenizer.identifier();
            symbolTable.define(varName, type, "var");
            tokenizer.advance();
            curLocalCount++;
        } while (tokenizer.symbol() != ';');

        if (tokenizer.hasMoreTokens())
            tokenizer.advance();
    }

    private void compileParameterList() {
        /*boolean first = true;

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

        writer.println("</parameterList>");*/

        boolean first = true;

        while (tokenizer.symbol() != ')') {
            if (! first)
                tokenizer.advance(); // ',' when not the first parameter

            String type = tokenizer.keyWord();
            tokenizer.advance(); // type
            String name = tokenizer.identifier();
            tokenizer.advance(); // name
            symbolTable.define(name, type, "argument");
            first = false;
        }
    }

    private void compileClassVarDec() {
        /*writer.println("<classVarDec>");
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
        writer.println("</classVarDec>");*/

        String kind = tokenizer.keyWord();
        tokenizer.advance(); // static or field

        String type = tokenizer.keyWord();
        tokenizer.advance(); // type

        String name = tokenizer.identifier();
        tokenizer.advance(); // name

        symbolTable.define(name, type, kind);

        while (tokenizer.symbol() != ';') {
            tokenizer.advance(); // ','
            symbolTable.define(tokenizer.identifier(), type, kind);
            tokenizer.advance(); // name
        }

        tokenizer.advance(); // ';'
    }

    void close() {
        //writer.close();
        vm.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        String line = "class Test {field int a, b, c; static String name; method void add(int i, int j) {var int x; var int y; let x = i; let b = i + j; return;}}";

        File in = new File("SquareGame.jack");

        JackTokenizer tk = new JackTokenizer(in);
        tk.advance();

        File out = new File("out.vm");
        CompilationEngine ce = new CompilationEngine(tk, out);
        ce.setDebug(true);

        ce.compileClass();
        ce.close();
    }

}
