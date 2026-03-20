package com.hplusplus.parser.nodes;

import java.util.List;

public sealed interface Node permits
    Node.Program,
    Node.AppDeclaration,
    Node.OpenImport,
    Node.DeclareVar,
    Node.Assign,
    Node.Say,
    Node.Who,
    Node.Ask,
    Node.Shout,
    Node.Remember,
    Node.Really,
    Node.FuncDef,
    Node.Bye,
    Node.StringLiteral,
    Node.IntLiteral,
    Node.FloatLiteral,
    Node.BoolLiteral,
    Node.Identifier,
    Node.Comparison,
    Node.IfBlock,
    Node.Loop,
    Node.Stop,
    Node.MathCall
{
    // Root
    record Program(List<Node> statements) implements Node {}

    // appN com.apple.clock
    record AppDeclaration(String name) implements Node {}

    // open linux.fetch_64bit
    record OpenImport(String module) implements Node {}

    // declare.Int score
    record DeclareVar(String type, String name) implements Node {}

    // score == 10
    record Assign(String name, Node value) implements Node {}

    // say "Hello"
    record Say(Node value) implements Node {}

    // who (Birthday?)
    record Who(String prompt) implements Node {}

    // ask.(Really time.clock) or ask (who [Name]) or ask (shortterm1)
    record Ask(Node target, boolean force) implements Node {}

    // shout greet or shout! greet
    record Shout(String funcName, boolean force) implements Node {}

    // remember (x) as score or remember! (x) as file.md
    record Remember(Node value, String target, boolean force) implements Node {}

    // Really time.clock
    record Really(String target) implements Node {}

    // func greet; ... bye func;
    record FuncDef(String name, List<Node> body) implements Node {}

    // bye or bye func;
    record Bye(String scope) implements Node {}

    // Literals
    record StringLiteral(String value) implements Node {}
    record IntLiteral(int value) implements Node {}
    record FloatLiteral(double value) implements Node {}
    record BoolLiteral(boolean value) implements Node {}
    record Identifier(String name) implements Node {}

    // are condition then; ... otherwise; ... next
    // Each branch is a (condition, body) pair. Null condition = otherwise with no are.
    record IfBranch(Node condition, List<Node> body) {}
    record IfBlock(List<IfBranch> branches) implements Node {}

    // loop; ... next
    record Loop(List<Node> body) implements Node {}

    // stop
    record Stop() implements Node {}

    // math.randomize(0 50)
    record MathCall(String function, List<Node> args) implements Node {}

    // are score == 10 and lives == 3
    record Comparison(Node left, String operator, Node right) implements Node {}
}
