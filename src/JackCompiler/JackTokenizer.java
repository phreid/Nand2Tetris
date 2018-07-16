package JackCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {
    private boolean commentFlag;
    private ArrayList<String> tokens;
    private int c;
    private String curToken;

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

    public JackTokenizer(File inFile) throws FileNotFoundException {
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
                if (! (commentFlag || token.charAt(0) == '/')) tokens.add(token);
                if (token.equals("*/")) commentFlag = false;
            }
        }

        scanner.close();
    }

    public boolean hasMoreTokens() {
        return c < tokens.size();
    }

    public void advance() {
        curToken = tokens.get(c++);
    }

    public TokenType tokenType() {
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

    public String symbol() {
        return curToken;
    }

    public String identifier() {
        return curToken;
    }

    public int intVal() {
        return Integer.parseInt(curToken);
    }

    public String stringVal() {
        return curToken.substring(1, curToken.length());
    }

    public static void main(String[] args) throws FileNotFoundException {
        File f =  new File("test.txt");
        JackTokenizer t = new JackTokenizer(f);

        while (t.hasMoreTokens()) {
            t.advance();
            System.out.println(t.curToken);
            System.out.println(t.tokenType());
        }
    }
}
