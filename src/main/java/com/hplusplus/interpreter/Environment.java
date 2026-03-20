package com.hplusplus.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {

    private final Map<String, Object> variables = new HashMap<>();
    private final List<Object> shortterm = new ArrayList<>();
    private Object lastAnswer = null;

    // Variables

    public void set(String name, Object value) {
        variables.put(name, value);
    }

    public Object get(String name) {
        if (name.equals("ask.LastAns") || name.equals("ask.lastAnswer")) {
            return lastAnswer;
        }
        if (name.startsWith("shortterm")) {
            int index = parseShortterm(name);
            if (index >= 0 && index < shortterm.size()) return shortterm.get(index);
            return null;
        }
        return variables.getOrDefault(name, null);
    }

    public boolean isDeclared(String name) {
        return variables.containsKey(name);
    }

    // LastAns

    public void setLastAnswer(Object value) {
        this.lastAnswer = value;
    }

    public Object getLastAnswer() {
        return lastAnswer;
    }

    // Shortterm slots

    public int remember(Object value) {
        shortterm.add(value);
        return shortterm.size(); // slot number (1-indexed)
    }

    public void flushShortterm() {
        shortterm.clear();
    }

    // Functions

    private final Map<String, com.hplusplus.parser.nodes.Node.FuncDef> functions = new HashMap<>();

    public void defineFunction(String name, com.hplusplus.parser.nodes.Node.FuncDef def) {
        functions.put(name, def);
    }

    public com.hplusplus.parser.nodes.Node.FuncDef getFunction(String name) {
        return functions.get(name);
    }

    // Helpers

    private int parseShortterm(String name) {
        try {
            return Integer.parseInt(name.replace("shortterm", "")) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void dump() {
        System.out.println("[Env] Variables: " + variables);
        System.out.println("[Env] Shortterm: " + shortterm);
        System.out.println("[Env] LastAns: " + lastAnswer);
    }
}
