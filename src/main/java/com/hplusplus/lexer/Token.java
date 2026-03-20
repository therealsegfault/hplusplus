package com.hplusplus.lexer;

public record Token(TokenType type, String value, int line) {
    @Override
    public String toString() {
        return "Token[" + type + ", \"" + value + "\", line=" + line + "]";
    }
}
