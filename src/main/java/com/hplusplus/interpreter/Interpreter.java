package com.hplusplus.interpreter;

import com.hplusplus.parser.nodes.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Interpreter {

    private final Environment env = new Environment();
    private final Scanner scanner = new Scanner(System.in);

    public void execute(Node.Program program) {
        for (Node stmt : program.statements()) {
            executeNode(stmt);
        }
    }

    private Object executeNode(Node node) {
        return switch (node) {
            case Node.Program p         -> { for (Node s : p.statements()) executeNode(s); yield null; }
            case Node.AppDeclaration a  -> null;
            case Node.OpenImport o      -> { handleOpen(o.module()); yield null; }
            case Node.DeclareVar d      -> { env.set(d.name(), null); yield null; }
            case Node.Assign a          -> { Object val = evaluate(a.value()); env.set(a.name(), val); yield val; }
            case Node.Say s             -> { Object val = evaluate(s.value()); System.out.println(formatValue(val)); yield null; }
            case Node.Who w             -> { Object val = handleWho(w); yield val; }
            case Node.Ask a             -> { Object val = handleAsk(a); env.setLastAnswer(val); yield val; }
            case Node.Shout s           -> handleShout(s);
            case Node.Remember r        -> handleRemember(r);
            case Node.Really r          -> handleReally(r.target());
            case Node.FuncDef f         -> { env.defineFunction(f.name(), f); yield null; }
            case Node.Bye b             -> { handleBye(b); yield null; }
            case Node.IfBlock ib        -> { handleIfBlock(ib); yield null; }
            case Node.Loop l            -> { handleLoop(l); yield null; }
            case Node.Stop s            -> throw new StopException();
            case Node.MathCall mc       -> handleMathCall(mc);
            case Node.StringLiteral sl  -> sl.value();
            case Node.IntLiteral il     -> il.value();
            case Node.FloatLiteral fl   -> fl.value();
            case Node.BoolLiteral bl    -> bl.value();
            case Node.Identifier id     -> env.get(id.name());
            case Node.Comparison c      -> handleComparison(c);
        };
    }

    private Object evaluate(Node node) {
        return executeNode(node);
    }

    // --- Handlers ---

    private void handleOpen(String module) {
        // Module loading — currently logs; real platform binding comes later
        System.out.println("[H++] Loaded module: " + module);
    }

    private Object handleWho(Node.Who w) {
        System.out.print(w.prompt() + ": ");
        String input = scanner.nextLine();
        env.set("__who__" + w.prompt(), input);
        env.setLastAnswer(input);
        return input;
    }

    private Object handleAsk(Node.Ask ask) {
        Node target = ask.target();

        // ask.(Really time.clock)
        if (target instanceof Node.Really r) {
            return handleReally(r.target());
        }

        // ask (who [label]) — retrieve previously stored who input, no re-prompt
        if (target instanceof Node.Who w) {
            Object stored = env.get("__who__" + w.prompt());
            if (stored != null) return stored;
            // fallback: prompt if not yet stored
            return handleWho(w);
        }

        // ask (shortterm1) or ask (varName) or ask (ask.LastAns)
        if (target instanceof Node.Identifier id) {
            return env.get(id.name());
        }

        // ask.(math.randomize(0 50))
        if (target instanceof Node.MathCall mc) {
            return handleMathCall(mc);
        }

        return evaluate(target);
    }

    private Object handleShout(Node.Shout shout) {
        Node.FuncDef func = env.getFunction(shout.funcName());
        if (func == null) {
            System.err.println("[Novel] Unknown function: " + shout.funcName());
            return null;
        }
        return executeBlock(func.body());
    }

    private Object handleRemember(Node.Remember remember) {
        Object value = evaluate(remember.value());
        String target = remember.target();

        if (target == null || target.isEmpty()) {
            // shortterm
            int slot = env.remember(value);
            System.out.println("[Novel] Stored as shortterm" + slot);
            return value;
        }

        if (target.contains(".")) {
            // file write
            String ext = target.substring(target.lastIndexOf('.') + 1);
            try {
                Path path = Path.of(System.getProperty("user.home"), target);
                Files.writeString(path, formatValue(value));
                System.out.println("[Novel] Written to " + path);
            } catch (IOException e) {
                System.err.println("[Novel] Could not write file: " + target + " — " + e.getMessage());
            }
        } else {
            // named variable
            env.set(target, value);
        }
        return value;
    }

    private Object handleReally(String target) {
        return switch (target) {
            case "time.clock" -> {
                LocalTime now = LocalTime.now();
                yield now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
            }
            default -> {
                System.err.println("[Novel] Unknown Really target: " + target);
                yield null;
            }
        };
    }

    private void handleBye(Node.Bye bye) {
        if (bye.scope().equals("script")) {
            env.flushShortterm();
            // Full exit handled by Main
        }
        // bye func; is handled by parseFuncDef, nothing to do at runtime
    }

    // --- Control flow ---

    private static class StopException extends RuntimeException {
        StopException() { super(null, null, true, false); }
    }

    private void handleIfBlock(Node.IfBlock ib) {
        for (Node.IfBranch branch : ib.branches()) {
            if (branch.condition() == null) {
                // plain otherwise
                executeBlock(branch.body());
                return;
            }
            Object result = evaluate(branch.condition());
            if (isTruthy(result)) {
                executeBlock(branch.body());
                return;
            }
        }
    }

    private void handleLoop(Node.Loop loop) {
        try {
            while (true) {
                executeBlock(loop.body());
            }
        } catch (StopException e) {
            // clean exit from loop
        }
    }

    private Object handleMathCall(Node.MathCall mc) {
        return switch (mc.function()) {
            case "math.randomize" -> {
                int lo = toInt(evaluate(mc.args().get(0)));
                int hi = toInt(evaluate(mc.args().get(1)));
                yield lo + (int)(Math.random() * (hi - lo + 1));
            }
            default -> {
                System.err.println("[Novel] Unknown math function: " + mc.function());
                yield null;
            }
        };
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Integer i) return i != 0;
        if (value instanceof String s) return !s.isEmpty();
        return true;
    }

    private int toInt(Object value) {
        if (value instanceof Integer i) return i;
        if (value instanceof Double d) return d.intValue();
        if (value instanceof String s) { try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; } }
        return 0;
    }

    private Object handleComparison(Node.Comparison c) {
        Object left = evaluate(c.left());
        Object right = evaluate(c.right());
        return switch (c.operator()) {
            case "==" -> objectsEqual(left, right);
            case "!=" -> !objectsEqual(left, right);
            case ">"  -> toDouble(left) > toDouble(right);
            case "<"  -> toDouble(left) < toDouble(right);
            default   -> false;
        };
    }

    private double toDouble(Object value) {
        if (value instanceof Integer i) return i.doubleValue();
        if (value instanceof Double d) return d;
        if (value instanceof String s) { try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return 0; } }
        return 0;
    }



    private Object executeBlock(List<Node> body) {
        Object last = null;
        for (Node stmt : body) {
            last = executeNode(stmt);
            if (stmt instanceof Node.Bye b && b.scope().equals("func")) break;
        }
        return last;
    }

    private boolean objectsEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        // numeric coercion: "25" == 25
        try {
            return Double.parseDouble(a.toString().trim()) == Double.parseDouble(b.toString().trim());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String formatValue(Object value) {
        if (value == null) return "";
        if (value instanceof Boolean b) return b ? "yes" : "no";
        return value.toString();
    }
}
