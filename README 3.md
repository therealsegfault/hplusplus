# H++ / Novel

> The H++ shell and Novel scripting language interpreter.

**Novel** is a beginner-friendly, keyword-driven scripting language that reads like natural conversation.  
**H++** is the shell runtime that runs Novel `.book` scripts.

---

## Quick Start

### Build

```bash
mvn package -q
```

### Run a script

```bash
java -jar target/hplusplus.jar run examples/hello
```

### Hello World (`examples/hello.book`)

```novel
appN com.test.hello

say "Hello, World!"

ask.(Really time.clock)
say ask.LastAns

bye
```

---

## Language Overview

| Keyword | Description |
|---|---|
| `appN` | App identity declaration |
| `declare.Type name` | Declare a variable |
| `name == value` | Assign a value |
| `say` | Print to console |
| `who (prompt)` | Prompt user for input |
| `Really target` | Fetch from system |
| `ask.(expr)` | Query and store result in `ask.LastAns` |
| `shout funcName` | Call a function (fire and forget) |
| `remember (x) as target` | Store a value to variable or file |
| `open module` | Import a module |
| `bye` | End of script |

See [`novel-spec.md`](novel-spec.md) for the full language specification.

---

## Project Structure

```
src/main/java/com/hplusplus/
├── Main.java               CLI entry point
├── lexer/
│   ├── Lexer.java          Tokenizer
│   └── Token.java          Token record
├── parser/
│   ├── Parser.java         AST builder
│   └── nodes/Node.java     AST node definitions
└── interpreter/
    ├── Interpreter.java    Tree-walking executor
    └── Environment.java    Variable and memory scope
```

---

## License

GPL — see LICENSE.
