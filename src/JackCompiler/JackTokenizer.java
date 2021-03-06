package JackCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JackTokenizer {
    private boolean commentFlag;
    private ArrayList<String> tokens;
    private int c;
    String curToken;

    private static final String digitRegex = "\\b\\d+\\b";
    private static final String labelRegex = "\\b[A-Za-z_][A-Za-z_\\d]*";
    private static final String stringRegex = "\"[^\"\\n]*\"";
    private static final String commentRegex = "\\/\\/.*";
    private static final String openCommentRegex = "\\/\\*";
    private static final String closeCommentRegex = "\\*\\/";
    private static final String symbolRegex = "[{}()\\[\\].,;\\+\\-\\*\\/\\&\\|<>=~]";

    private static final Pattern digitPattern = Pattern.compile(digitRegex);
    private static final Pattern labelPattern = Pattern.compile(labelRegex);
    private static final Pattern stringPattern = Pattern.compile(stringRegex);
    private static final Pattern symbolPattern = Pattern.compile(symbolRegex);
    private static final Pattern allPattern = Pattern.compile(
            digitRegex + "|" + labelRegex + "|" + stringRegex + "|" + commentRegex + "|" +
                      openCommentRegex + "|" + closeCommentRegex + "|" + symbolRegex
    );

    enum TokenType {
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST, BAD_TOKEN
    }

    private static final List<String> keyList = Arrays.asList(
            "class", "constructor", "function", "method", "field", "static",
            "var", "int", "char", "boolean", "void", "true", "false", "null",
            "this", "let", "do", "if", "else", "while", "return");

    JackTokenizer(File inFile) throws FileNotFoundException {
        tokens = new ArrayList<>();
        c = 0;
        commentFlag = false;

        Scanner scanner = new Scanner(inFile);
        Matcher tokenMatcher = allPattern.matcher("");

        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.isEmpty()) continue;

            tokenMatcher.reset(line);
            while (tokenMatcher.find()) {
                String token = tokenMatcher.group();
                if (token.equals("/*")) commentFlag = true;
                if (! (commentFlag || token.startsWith("//")))
                    tokens.add(token);
                if (token.equals("*/")) commentFlag = false;
            }
        }

        scanner.close();
    }

    JackTokenizer(String line) {
        tokens = new ArrayList<>();
        c = 0;
        commentFlag = false;

        Matcher tokenMatcher = allPattern.matcher("");

        tokenMatcher.reset(line);
        while (tokenMatcher.find()) {
            String token = tokenMatcher.group();
            if (token.equals("/*")) commentFlag = true;
            if (! (commentFlag || token.startsWith("//")))
                tokens.add(token);
            if (token.equals("*/")) commentFlag = false;
        }
    }

    boolean hasMoreTokens() {
        return c < tokens.size();
    }

    void advance() {
        curToken = tokens.get(c++);
    }

    String peek() {
        return tokens.get(c);
    }

    TokenType tokenType() {
        Matcher matcher;

        matcher = labelPattern.matcher(curToken);
        if (matcher.matches()) {
            if (keyList.contains(curToken)) return TokenType.KEYWORD;

            return TokenType.IDENTIFIER;
        }

        matcher = symbolPattern.matcher(curToken);
        if (matcher.matches()) return TokenType.SYMBOL;

        matcher = digitPattern.matcher(curToken);
        if (matcher.matches()) return TokenType.INT_CONST;

        matcher = stringPattern.matcher(curToken);
        if (matcher.matches()) return TokenType.STRING_CONST;

        return TokenType.BAD_TOKEN;
    }

    char symbol() {
        return curToken.charAt(0);
    }

    String identifier() {
        return curToken;
    }

    int intVal() {
        return Integer.parseInt(curToken);
    }

    String stringVal() {
        return curToken.substring(1, curToken.length() - 1);
    }

    String keyWord() {
        return curToken;
    }
}
