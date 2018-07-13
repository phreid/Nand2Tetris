package JackCompiler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {

    public static void main(String[] args) {
        String s = "let x = 10;\nlet 0y = 5;\nlet z = \"abcde\"\nlet q = \"\"";

        String p = "(" +
                "\\b\\d+\\b|" +
                "\\b[A-Za-z_][A-Za-z_\\d]*|" +
                "\"[^\"\\n]*\"|" +
                "[{}()\\[\\].,;\\+\\-\\*\\/\\&\\|<>=~]" +
                ")";

        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(s);

        while(matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}
