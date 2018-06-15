package Assembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {
    private int c;
    private ArrayList<String> commands;

    public Parser(String filepath) {
        commands = new ArrayList<>();
        c = 0;

        File file = new File(filepath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = line.replace(" ", "");
                int i = line.indexOf("/");
                if (i != -1) line = line.substring(0, i);
                if (! line.isEmpty()) commands.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) scanner.close();
        }
    }

    public boolean hasMoreCommands() {
        return c < commands.size();
    }

    public void advance() {
        c++;
    }

    public static void main(String[] args) {
        Parser p = new Parser("test.asm");

        String c = p.commands.toString();
        System.out.println(c);
    }
}
