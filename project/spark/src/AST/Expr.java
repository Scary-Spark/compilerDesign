package AST;

import java.util.List;
import java.util.Map;

public abstract class Expr {
    public final int line; // line number in source code

    public Expr(int line) {
        this.line = line;
    }

    public abstract Object evaluate(Map<String, Object> context);

    // Binary expression: a + b, x * y
    public static class Binary extends Expr {
        public final Expr left;
        public final String operator;
        public final Expr right;

        public Binary(Expr left, String operator, Expr right, int line) {
            super(line);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            Object l = left.evaluate(context);
            Object r = right.evaluate(context);

            if (l instanceof Number && r instanceof Number) {
                double leftVal = ((Number) l).doubleValue();
                double rightVal = ((Number) r).doubleValue();

                return switch (operator) {
                    case "+" -> leftVal + rightVal;
                    case "-" -> leftVal - rightVal;
                    case "*" -> leftVal * rightVal;
                    case "/" -> leftVal / rightVal;
                    case "%" -> leftVal % rightVal;
                    default -> throw new RuntimeException("Error at line " + line + ": Unknown operator: " + operator);
                };
            }

            if (l instanceof String || r instanceof String) {
                if (operator.equals("+")) return String.valueOf(l) + r;
            }

            throw new RuntimeException("Error at line " + line + ": Invalid operands for " + operator);
        }
    }

    // Unary expression: -x, !flag
    public static class Unary extends Expr {
        public final String operator;
        public final Expr right;

        public Unary(String operator, Expr right, int line) {
            super(line);
            this.operator = operator;
            this.right = right;
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            Object r = right.evaluate(context);
            return switch (operator) {
                case "-" -> -((Number) r).doubleValue();
                case "!" -> !((Boolean) r);
                default ->
                        throw new RuntimeException("Error at line " + line + ": Unknown unary operator: " + operator);
            };
        }
    }

    // Literal: 10, "Hello", true
    public static class Literal extends Expr {
        public final Object value;

        public Literal(Object value, int line) {
            super(line);
            this.value = value;
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            return value;
        }
    }

    // Variable: a, x, k
    public static class Variable extends Expr {
        public final String name;

        public Variable(String name, int line) {
            super(line);
            this.name = name;
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            if (!context.containsKey(name))
                throw new RuntimeException("Error at line " + line + ": Undefined variable: " + name);
            return context.get(name);
        }
    }

    // Function call: add(2,3)
    public static class Call extends Expr {
        public final java.util.function.Function<List<Object>, Object> func;
        public final List<Expr> arguments;

        public Call(java.util.function.Function<List<Object>, Object> func, List<Expr> arguments, int line) {
            super(line);
            this.func = func;
            this.arguments = arguments;
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            List<Object> args = arguments.stream().map(arg -> arg.evaluate(context)).toList();
            return func.apply(args);
        }
    }

    // Array access: arr[0]
    public static class ArrayAccess extends Expr {
        public final List<Expr> array;
        public final Expr index;

        public ArrayAccess(List<Expr> array, Expr index, int line) {
            super(line);
            this.array = array;
            this.index = index;
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            int i = ((Number) index.evaluate(context)).intValue();
            if (i < 0 || i >= array.size())
                throw new RuntimeException("Error at line " + line + ": Array index out of bounds: " + i);
            return array.get(i).evaluate(context);
        }
    }
}