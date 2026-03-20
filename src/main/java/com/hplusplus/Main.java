package com.hplusplus;

import com.hplusplus.interpreter.Interpreter;
import com.hplusplus.lexer.Lexer;
import com.hplusplus.lexer.Token;
import com.hplusplus.parser.Parser;
import com.hplusplus.parser.nodes.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    static final String VERSION = "0.1.0";

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(0);
        }

        String command = args[0];

        switch (command) {
            case "new"     -> Repl.start();
            case "run"     -> {
                if (args.length < 2) { System.err.println("[H++] Usage: h++ run <app/script>"); System.exit(1); }
                runFile(args[1]);
            }
            case "release" -> {
                if (args.length < 2) { System.err.println("[H++] Usage: h++ release <app/script>"); System.exit(1); }
                release(args[1]);
            }
            case "version", "--version", "-v" -> System.out.println("H++ v" + VERSION);
            default -> { System.err.println("[H++] Unknown command: " + command); printUsage(); System.exit(1); }
        }
    }

    static void runFile(String target) {
        Path filePath = resolveBookPath(target);
        if (!Files.exists(filePath)) { System.err.println("[H++] Cannot find script: " + filePath); System.exit(1); }
        String source;
        try { source = Files.readString(filePath); }
        catch (IOException e) { System.err.println("[H++] Could not read: " + filePath); System.exit(1); return; }
        runSource(source);
    }

    static void release(String target) {
        Path filePath = resolveBookPath(target);
        if (!Files.exists(filePath)) { System.err.println("[H++] Cannot find script: " + filePath); System.exit(1); }
        String source;
        try { source = Files.readString(filePath); }
        catch (IOException e) { System.err.println("[H++] Could not read: " + filePath); System.exit(1); return; }
        try {
            new Parser(new Lexer(source).tokenize()).parse();
        } catch (Exception e) {
            System.err.println("[H++] Release failed — parse error: " + e.getMessage()); System.exit(1); return;
        }
        String name = filePath.getFileName().toString().replace(".book", "");
        Path releaseDir = filePath.getParent().resolve(".release");
        try {
            Files.createDirectories(releaseDir);
            Path dest = releaseDir.resolve(name + ".book");
            Files.copy(filePath, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[H++] Released: " + dest);
        } catch (IOException e) { System.err.println("[H++] Release failed: " + e.getMessage()); System.exit(1); }
    }

    public static void runSource(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        Node.Program program = new Parser(tokens).parse();
        new Interpreter().execute(program);
    }

    static Path resolveBookPath(String target) {
        if (target.endsWith(".book")) return Path.of(target);
        return Path.of(target + ".book");
    }

    private static void printUsage() {
        System.out.println("H++ Novel Interpreter v" + VERSION);
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  h++ new              Start an interactive Novel shell");
        System.out.println("  h++ run <app/script> Run a .book script");
        System.out.println("  h++ release <app>    Validate and package a script");
        System.out.println("  h++ version          Show version");
    }
}
