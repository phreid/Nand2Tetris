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

    void compileClass() {
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
    }

    private void compileDo() {
        tokenizer.advance(); // 'do'
        compileSubroutineCall();
        vm.writePop("temp", 0);

        if (tokenizer.hasMoreTokens())
            tokenizer.advance(); // ';'
    }

    private void compileSubroutineCall() {
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
        vm.close();
    }
}
