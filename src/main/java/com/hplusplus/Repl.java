package com.hplusplus;

import com.hplusplus.interpreter.Interpreter;
import com.hplusplus.lexer.Lexer;
import com.hplusplus.lexer.Token;
import com.hplusplus.parser.Parser;
import com.hplusplus.parser.nodes.Node;

import java.util.List;
import java.util.Scanner;

public class Repl {

    private static final String PROMPT = "# app/new> ";
    private static final String VERSION = "0.1.0";

    public static void start() {
        System.out.println("H++ Novel Interactive Shell v" + VERSION);
        System.out.println("Type Novel expressions to evaluate them.");
        System.out.println("Type 'close' to exit.\n");

        Interpreter interpreter = new Interpreter();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(PROMPT);

            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;
            if (line.equals("close")) {
                System.out.println("[H++] Closing shell.");
                break;
            }

            // Wrap bare lines in a minimal program for the parser
            // Lines that don't start with a keyword get echoed as-is
            String source = wrapLine(line);

            try {
                Lexer lexer = new Lexer(source);
                List<Token> tokens = lexer.tokenize();
                Parser parser = new Parser(tokens);
                Node.Program program = parser.parse();
                interpreter.execute(program);
            } catch (Exception e) {
                System.out.println("[H++] Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    // Wrap a bare line into a minimal parseable program
    // The interpreter is reused across lines to keep state (variables, funcs)
    private static String wrapLine(String line) {
        // Already a full statement — pass through
        return line;
    }
}
