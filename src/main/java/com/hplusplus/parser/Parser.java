package com.hplusplus.parser;

import com.hplusplus.lexer.Token;
import com.hplusplus.lexer.TokenType;
import com.hplusplus.parser.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Node.Program parse() {
        List<Node> statements = new ArrayList<>();
        while (!isAtEnd()) {
            Node stmt = parseStatement();
            if (stmt != null) statements.add(stmt);
        }
        return new Node.Program(statements);
    }

    private Node parseStatement() {
        Token t = peek();
        return switch (t.type()) {
            case APP_N      -> parseAppN();
            case OPEN       -> parseOpen();
            case DECLARE    -> parseDeclare();
            case SAY        -> parseSay();
            case WHO        -> parseWho();
            case ASK        -> parseAsk();
            case SHOUT      -> parseShout();
            case REMEMBER   -> parseRemember();
            case REALLY     -> parseReally();
            case FUNC       -> parseFuncDef();
            case BYE        -> parseBye();
            case ARE        -> parseIfBlock();
            case LOOP       -> parseLoop();
            case STOP       -> { advance(); yield new Node.Stop(); }
            case IDENTIFIER -> parseAssignOrIdentifierOrMath();
            default -> { advance(); yield null; }
        };
    }

    // appN com.apple.clock
    private Node parseAppN() {
        consume(TokenType.APP_N);
        StringBuilder name = new StringBuilder();
        name.append(consume(TokenType.IDENTIFIER).value());
        while (check(TokenType.DOT)) {
            advance();
            name.append(".").append(advance().value());
        }
        return new Node.AppDeclaration(name.toString());
    }

    // open linux.fetch_64bit
    private Node parseOpen() {
        consume(TokenType.OPEN);
        StringBuilder module = new StringBuilder();
        module.append(advance().value());
        while (check(TokenType.DOT)) {
            advance();
            module.append(".").append(advance().value());
        }
        return new Node.OpenImport(module.toString());
    }

    // declare.Int score
    private Node parseDeclare() {
        consume(TokenType.DECLARE);
        consume(TokenType.DOT);
        String type = advance().value();
        String name = consume(TokenType.IDENTIFIER).value();
        return new Node.DeclareVar(type, name);
    }

    // say "Hello" or say ask.LastAns
    private Node parseSay() {
        consume(TokenType.SAY);
        Node value = parseExpression();
        return new Node.Say(value);
    }

    // who (Birthday?)
    private Node parseWho() {
        consume(TokenType.WHO);
        consume(TokenType.LPAREN);
        StringBuilder prompt = new StringBuilder();
        while (!check(TokenType.RPAREN) && !isAtEnd()) {
            prompt.append(advance().value()).append(" ");
        }
        consume(TokenType.RPAREN);
        return new Node.Who(prompt.toString().trim());
    }

    // ask.(Really time.clock) or ask (who [Name]) or ask (shortterm1)
    private Node parseAsk() {
        consume(TokenType.ASK);
        boolean force = false;
        if (check(TokenType.DOT)) advance();
        if (check(TokenType.LPAREN)) {
            consume(TokenType.LPAREN);
            if (check(TokenType.BANG)) { advance(); force = true; }
            // ask (who [label]) — retrieve stored who input
            if (check(TokenType.WHO)) {
                advance();
                if (check(TokenType.LBRACKET)) {
                    advance();
                    StringBuilder label = new StringBuilder();
                    while (!check(TokenType.RBRACKET) && !isAtEnd()) {
                        label.append(advance().value()).append(" ");
                    }
                    if (check(TokenType.RBRACKET)) advance();
                    consume(TokenType.RPAREN);
                    return new Node.Ask(new Node.Who(label.toString().trim()), force);
                }
            }
            Node target = parseExpression();
            consume(TokenType.RPAREN);
            return new Node.Ask(target, force);
        }
        // ask (shortterm1) without parens
        Node target = parseExpression();
        return new Node.Ask(target, false);
    }

    // shout greet or shout! greet
    private Node parseShout() {
        consume(TokenType.SHOUT);
        boolean force = false;
        if (check(TokenType.BANG)) { advance(); force = true; }
        String name = consume(TokenType.IDENTIFIER).value();
        return new Node.Shout(name, force);
    }

    // remember (x) as score or remember! (x) as file.md
    private Node parseRemember() {
        consume(TokenType.REMEMBER);
        boolean force = false;
        if (check(TokenType.BANG)) { advance(); force = true; }
        consume(TokenType.LPAREN);
        Node value = parseExpression();
        consume(TokenType.RPAREN);
        consume(TokenType.AS);
        String target = advance().value();
        return new Node.Remember(value, target, force);
    }

    // Really time.clock
    private Node parseReally() {
        consume(TokenType.REALLY);
        StringBuilder target = new StringBuilder();
        target.append(advance().value());
        while (check(TokenType.DOT)) {
            advance();
            target.append(".").append(advance().value());
        }
        return new Node.Really(target.toString());
    }

    // func greet; ... bye func;
    private Node parseFuncDef() {
        consume(TokenType.FUNC);
        String name = consume(TokenType.IDENTIFIER).value();
        consume(TokenType.SEMICOLON);
        List<Node> body = new ArrayList<>();
        while (!isAtEnd()) {
            if (check(TokenType.BYE)) {
                advance();
                if (check(TokenType.FUNC)) { advance(); consume(TokenType.SEMICOLON); break; }
            }
            Node stmt = parseStatement();
            if (stmt != null) body.add(stmt);
        }
        return new Node.FuncDef(name, body);
    }

    // bye or bye func;
    private Node parseBye() {
        consume(TokenType.BYE);
        if (check(TokenType.FUNC)) {
            advance();
            consume(TokenType.SEMICOLON);
            return new Node.Bye("func");
        }
        return new Node.Bye("script");
    }

    // score == 10  or  math.randomize(0 50)
    private Node parseAssignOrIdentifierOrMath() {
        String name = consume(TokenType.IDENTIFIER).value();
        // math.randomize(0 50)
        if (check(TokenType.DOT)) {
            advance();
            String func = advance().value();
            if (check(TokenType.LPAREN)) {
                advance();
                List<Node> args = new ArrayList<>();
                while (!check(TokenType.RPAREN) && !isAtEnd()) {
                    args.add(parseExpression());
                }
                consume(TokenType.RPAREN);
                return new Node.MathCall(name + "." + func, args);
            }
            return new Node.Identifier(name + "." + func);
        }
        if (check(TokenType.EQUALS)) {
            consume(TokenType.EQUALS);
            Node value = parseExpression();
            return new Node.Assign(name, value);
        }
        return new Node.Identifier(name);
    }

    // are score == 10
    // then;
    //     ...
    // otherwise are score > 5
    //     ...
    // otherwise;
    //     ...
    // next
    private Node parseIfBlock() {
        List<Node.IfBranch> branches = new ArrayList<>();

        // First branch — are <condition> then;
        consume(TokenType.ARE);
        Node condition = parseComparison();
        consume(TokenType.THEN);
        if (check(TokenType.SEMICOLON)) advance();
        List<Node> thenBody = parseBlock();
        branches.add(new Node.IfBranch(condition, thenBody));

        // otherwise branches
        while (check(TokenType.OTHERWISE)) {
            advance();
            if (check(TokenType.ARE)) {
                advance();
                Node elseifCond = parseComparison();
                if (check(TokenType.SEMICOLON)) advance();
                List<Node> elseifBody = parseBlock();
                branches.add(new Node.IfBranch(elseifCond, elseifBody));
            } else {
                if (check(TokenType.SEMICOLON)) advance();
                List<Node> elseBody = parseBlock();
                branches.add(new Node.IfBranch(null, elseBody));
                break;
            }
        }

        if (check(TokenType.NEXT)) advance();
        return new Node.IfBlock(branches);
    }

    // loop; ... next
    private Node parseLoop() {
        consume(TokenType.LOOP);
        consume(TokenType.SEMICOLON);
        List<Node> body = parseBlock();
        if (check(TokenType.NEXT)) advance();
        return new Node.Loop(body);
    }

    // Parse statements until next/otherwise/bye func reached
    private List<Node> parseBlock() {
        List<Node> body = new ArrayList<>();
        while (!isAtEnd()
                && !check(TokenType.NEXT)
                && !check(TokenType.OTHERWISE)) {
            if (check(TokenType.BYE)) break;
            Node stmt = parseStatement();
            if (stmt != null) body.add(stmt);
        }
        return body;
    }

    // Parse a comparison expression: score == 10, score > 5, etc.
    private Node parseComparison() {
        Node left = parseExpression();
        if (check(TokenType.EQUALS)) {
            advance();
            Node right = parseExpression();
            return new Node.Comparison(left, "==", right);
        }
        if (check(TokenType.GT)) {
            advance();
            Node right = parseExpression();
            return new Node.Comparison(left, ">", right);
        }
        if (check(TokenType.LT)) {
            advance();
            Node right = parseExpression();
            return new Node.Comparison(left, "<", right);
        }
        return left;
    }



    private Node parseExpression() {
        Token t = peek();
        return switch (t.type()) {
            case STRING  -> { advance(); yield new Node.StringLiteral(t.value()); }
            case INTEGER -> { advance(); yield new Node.IntLiteral(Integer.parseInt(t.value())); }
            case FLOAT   -> { advance(); yield new Node.FloatLiteral(Double.parseDouble(t.value())); }
            case BOOL    -> { advance(); yield new Node.BoolLiteral(t.value().equals("yes")); }
            case REALLY  -> parseReally();
            case ASK -> {
                advance();
                if (check(TokenType.DOT)) {
                    advance();
                    String field = advance().value();
                    yield new Node.Identifier("ask." + field);
                }
                yield new Node.Identifier("ask.LastAns");
            }
            case IDENTIFIER -> {
                advance();
                if (check(TokenType.DOT)) {
                    advance();
                    String func = advance().value();
                    // math.randomize(0 50) as expression
                    if (check(TokenType.LPAREN)) {
                        advance();
                        List<Node> args = new ArrayList<>();
                        while (!check(TokenType.RPAREN) && !isAtEnd()) {
                            args.add(parseExpression());
                        }
                        consume(TokenType.RPAREN);
                        yield new Node.MathCall(t.value() + "." + func, args);
                    }
                    yield new Node.Identifier(t.value() + "." + func);
                }
                yield new Node.Identifier(t.value());
            }
            default -> { advance(); yield new Node.Identifier(t.value()); }
        };
    }

    private Token consume(TokenType type) {
        if (check(type)) return advance();
        throw new RuntimeException("[Parser] Expected " + type + " but got " + peek() + " at line " + peek().line());
    }

    private boolean check(TokenType type) { return !isAtEnd() && peek().type() == type; }
    private Token advance() { if (!isAtEnd()) current++; return previous(); }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }
    private boolean isAtEnd() { return peek().type() == TokenType.EOF; }
}
