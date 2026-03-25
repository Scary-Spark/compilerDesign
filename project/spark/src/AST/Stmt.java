package AST;

import java.util.List;
import java.util.Map;

public abstract class Stmt {
    public final int line; // line number in source code

    public Stmt(int line) {
        this.line = line;
    }

    public abstract void execute(Map<String, Object> context);

    public static class Input extends Stmt {
        public final String name;
        public final Expr expr;

        public Input(String name, Expr expr, int line) {
            super(line);
            this.name = name;
            this.expr = expr;
        }

        @Override
        public void execute(Map<String, Object> context) {
            if (expr != null)
                context.put(name, expr.evaluate(context));
            else
                context.put(name, 0); // default value
        }
    }

    public static class Const extends Stmt {
        public final String name;
        public final Expr expr;

        public Const(String name, Expr expr, int line) {
            super(line);
            this.name = name;
            this.expr = expr;
        }

        @Override
        public void execute(Map<String, Object> context) {
            if (context.containsKey(name))
                throw new RuntimeException("Error at line " + line + ": Constant already defined: " + name);
            context.put(name, expr.evaluate(context));
        }
    }

    public static class Print extends Stmt {
        public final Expr expr;
        public final boolean newLine;

        public Print(Expr expr, boolean newLine, int line) {
            super(line);
            this.expr = expr;
            this.newLine = newLine;
        }

        @Override
        public void execute(Map<String, Object> context) {
            Object val = expr.evaluate(context);
            if (newLine) System.out.println(val);
            else System.out.print(val);
        }
    }

    public static class Assignment extends Stmt {
        public final String name;
        public final Expr expr;

        public Assignment(String name, Expr expr, int line) {
            super(line);
            this.name = name;
            this.expr = expr;
        }

        @Override
        public void execute(Map<String, Object> context) {
            if (!context.containsKey(name))
                throw new RuntimeException("Error at line " + line + ": Variable not defined: " + name);
            context.put(name, expr.evaluate(context));
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final List<Stmt> thenBranch;
        public final List<Stmt> elseBranch;

        public If(Expr condition, List<Stmt> thenBranch, List<Stmt> elseBranch, int line) {
            super(line);
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public void execute(Map<String, Object> context) {
            Object cond = condition.evaluate(context);
            if (!(cond instanceof Boolean))
                throw new RuntimeException("Error at line " + line + ": Condition must be boolean");
            if ((Boolean) cond) {
                thenBranch.forEach(stmt -> stmt.execute(context));
            } else if (elseBranch != null) {
                elseBranch.forEach(stmt -> stmt.execute(context));
            }
        }
    }

    public static class While extends Stmt {
        public final Expr condition;
        public final List<Stmt> body;

        public While(Expr condition, List<Stmt> body, int line) {
            super(line);
            this.condition = condition;
            this.body = body;
        }

        @Override
        public void execute(Map<String, Object> context) {
            Object cond = condition.evaluate(context);
            while (cond instanceof Boolean && (Boolean) cond) {
                for (Stmt stmt : body) stmt.execute(context);
                cond = condition.evaluate(context);
            }
        }
    }

    public static class Return extends Stmt {
        public final Expr expr;

        public Return(Expr expr, int line) {
            super(line);
            this.expr = expr;
        }

        @Override
        public void execute(Map<String, Object> context) {
            throw new ReturnValue(expr.evaluate(context), line);
        }
    }

    public static class ReturnValue extends RuntimeException {
        public final Object value;
        public final int line;

        public ReturnValue(Object value, int line) {
            super("Return at line " + line);
            this.value = value;
            this.line = line;
        }
    }
}