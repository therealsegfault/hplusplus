package com.hplusplus.lexer;

public enum TokenType {
    // App identity
    APP_N,

    // Declaration
    DECLARE,

    // Types
    TYPE_INT, TYPE_STR, TYPE_FLOAT, TYPE_BOOL,
    TYPE_LIST, TYPE_MAP, TYPE_FUNC,

    // Assignment & comparison
    EQUALS,         // ==
    GT,             // >
    LT,             // <
    ARE,
    EXCEPT,
    AND,
    WITH,
    OR,

    // Control flow
    THEN,
    OTHERWISE,
    NEXT,
    LOOP,
    STOP,

    // I/O
    SAY,
    WHO,

    // Fetch & memory
    REALLY,
    ASK,
    SHOUT,
    REMEMBER,
    AS,
    FETCH,          // fetch: prefix

    // Control
    OPEN,
    FUNC,
    BYE,
    ON,

    // Draw (Gloves)
    DRAW,
    DRAW_NEW,
    REDRAW,

    // Literals
    STRING,         // "hello"
    INTEGER,        // 42
    FLOAT,          // 3.14
    BOOL,           // yes / no
    IDENTIFIER,     // variable/function names

    // Symbols
    DOT,            // .
    SEMICOLON,      // ;
    LPAREN,         // (
    RPAREN,         // )
    LBRACKET,       // [
    RBRACKET,       // ]
    BANG,           // !
    COLON,          // :

    // Special
    EOF
}
