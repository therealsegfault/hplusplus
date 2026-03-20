package com.hplusplus.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
        Map.entry("appN",      TokenType.APP_N),
        Map.entry("declare",   TokenType.DECLARE),
        Map.entry("are",       TokenType.ARE),
        Map.entry("except",    TokenType.EXCEPT),
        Map.entry("and",       TokenType.AND),
        Map.entry("with",      TokenType.WITH),
        Map.entry("or",        TokenType.OR),
        Map.entry("then",      TokenType.THEN),
        Map.entry("otherwise", TokenType.OTHERWISE),
        Map.entry("next",      TokenType.NEXT),
        Map.entry("loop",      TokenType.LOOP),
        Map.entry("stop",      TokenType.STOP),
        Map.entry("say",       TokenType.SAY),
        Map.entry("who",       TokenType.WHO),
        Map.entry("Really",    TokenType.REALLY),
        Map.entry("ask",       TokenType.ASK),
        Map.entry("shout",     TokenType.SHOUT),
        Map.entry("remember",  TokenType.REMEMBER),
        Map.entry("as",        TokenType.AS),
        Map.entry("open",      TokenType.OPEN),
        Map.entry("func",      TokenType.FUNC),
        Map.entry("bye",       TokenType.BYE),
        Map.entry("on",        TokenType.ON),
        Map.entry("draw",      TokenType.DRAW),
        Map.entry("drawNew",   TokenType.DRAW_NEW),
        Map.entry("redraw",    TokenType.REDRAW),
        Map.entry("yes",       TokenType.BOOL),
        Map.entry("no",        TokenType.BOOL),
        Map.entry("Int",       TokenType.TYPE_INT),
        Map.entry("Str",       TokenType.TYPE_STR),
        Map.entry("Float",     TokenType.TYPE_FLOAT),
        Map.entry("Bool",      TokenType.TYPE_BOOL),
        Map.entry("List",      TokenType.TYPE_LIST),
        Map.entry("Map",       TokenType.TYPE_MAP),
        Map.entry("Func",      TokenType.TYPE_FUNC)
    );

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LPAREN);
            case ')' -> addToken(TokenType.RPAREN);
            case '[' -> addToken(TokenType.LBRACKET);
            case ']' -> addToken(TokenType.RBRACKET);
            case '.' -> addToken(TokenType.DOT);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '!' -> addToken(TokenType.BANG);
            case ':' -> addToken(TokenType.COLON);
            case '>' -> addToken(TokenType.GT);
            case '<' -> addToken(TokenType.LT);
            case '=' -> {
                if (match('=')) addToken(TokenType.EQUALS);
            }
            case '#' -> {
                // Comments — skip to end of line
                while (peek() != '\n' && !isAtEnd()) advance();
            }
            case '"' -> string();
            case '\n' -> line++;
            case ' ', '\r', '\t' -> { /* ignore whitespace */ }
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    System.err.println("[Lexer] Unexpected character '" + c + "' at line " + line);
                }
            }
        }
    }

    private void string() {
        StringBuilder sb = new StringBuilder();
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            sb.append(advance());
        }
        if (isAtEnd()) {
            System.err.println("[Lexer] Unterminated string at line " + line);
            return;
        }
        advance(); // closing "
        tokens.add(new Token(TokenType.STRING, sb.toString(), line));
    }

    private void number() {
        while (isDigit(peek())) advance();
        boolean isFloat = false;
        if (peek() == '.' && isDigit(peekNext())) {
            isFloat = true;
            advance(); // consume .
            while (isDigit(peek())) advance();
        }
        String value = source.substring(start, current);
        addToken(isFloat ? TokenType.FLOAT : TokenType.INTEGER, value);
    }

    private void identifier() {
        while (isAlphaNumeric(peek()) || peek() == '_') advance();
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type, text);
    }

    private char advance() { return source.charAt(current++); }
    private char peek() { return isAtEnd() ? '\0' : source.charAt(current); }
    private char peekNext() { return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1); }
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }
    private boolean isAtEnd() { return current >= source.length(); }
    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private boolean isAlpha(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'; }
    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }

    private void addToken(TokenType type) { addToken(type, source.substring(start, current)); }
    private void addToken(TokenType type, String value) { tokens.add(new Token(type, value, line)); }
}
