// Evaluator: Executes the AST and produces output
function evaluate(node, env = {}) {
  switch (node.type) {
    case "Program":
      // Execute each statement in order
      node.body.forEach((stmt) => evaluate(stmt, env));
      break;

    case "InputStatement":
      // Evaluate the expression and store it in the environment
      const value = evaluate(node.expression, env);
      env[node.id.name] = value;
      return value;

    case "PrintStatement":
      // Evaluate the expression and log the result
      const result = evaluate(node.expression, env);
      console.log(`${result}`);
      return result;

    case "BinaryExpression":
      // Recursively evaluate left and right operands
      const left = evaluate(node.left, env);
      const right = evaluate(node.right, env);

      // Perform the actual math
      switch (node.operator) {
        case "+":
          return left + right;
        case "-":
          return left - right;
        case "*":
          return left * right;
        case "/":
          return left / right;
        default:
          throw new Error(`Unknown operator: ${node.operator}`);
      }

    case "Identifier":
      // Look up the variable value in the environment
      if (node.name in env) {
        return env[node.name];
      }
      throw new Error(`Runtime Error: Variable "${node.name}" is not defined.`);

    case "NumberLiteral":
      return node.value;

    default:
      throw new Error(`Evaluation Error: Unsupported node type "${node.type}"`);
  }
}

// Analyzer: Performs semantic analysis (Context-sensitive checks)
function analyzer(ast) {
  // A Set to keep track of defined variable names
  const symbolTable = new Set();

  function visit(node) {
    switch (node.type) {
      case "Program":
        node.body.forEach(visit);
        break;

      case "InputStatement":
        // 1. Check the expression being assigned first
        visit(node.expression);
        // 2. Register the variable in the symbol table
        symbolTable.add(node.id.name);
        break;

      case "PrintStatement":
        // Check the expression to ensure all variables used exist
        visit(node.expression);
        break;

      case "BinaryExpression":
        // Recursively check left and right sides
        visit(node.left);
        visit(node.right);
        break;

      case "Identifier":
        // CRITICAL CHECK: Is the variable defined?
        if (!symbolTable.has(node.name)) {
          throw new Error(
            `Semantic Error: Variable "${node.name}" is used before it is defined.`,
          );
        }
        break;

      case "NumberLiteral":
        // Numbers are always valid
        break;

      default:
        throw new Error(`Unknown AST node type: ${node.type}`);
    }
  }

  // Start the analysis
  visit(ast);
  // console.log("Semantic Analysis: Success (All variables resolved).");
}

// Parser: Converts tokens into an Abstract Syntax Tree (AST)
function parser(tokens) {
  let cursor = 0;

  // return current token without
  // moving cursor
  function peek() {
    return tokens[cursor];
  }

  // consume a token if it matches the expected type
  // (and optional value)
  function consume(expectedType, expectedValue = null) {
    const token = tokens[cursor];
    if (
      !token ||
      token.type !== expectedType ||
      (expectedValue && token.value !== expectedValue)
    ) {
      throw new Error(
        `Expected ${expectedType}${expectedValue ? " " + expectedValue : ""}, got ${token?.type} ${token?.value}`,
      );
    }
    cursor++;
    return token;
  }

  // parse the entire program
  function parseProgram() {
    const body = [];
    while (cursor < tokens.length) {
      body.push(parseStatement());
    }
    return { type: "Program", body };
  }

  // Parse a single statement
  function parseStatement() {
    const token = peek();
    if (token.type === "KEYWORD" && token.value === "input") {
      return parseInputStatement();
    } else if (token.type === "KEYWORD" && token.value === "print") {
      return parsePrintStatement();
    } else {
      throw new Error(`Unexpected statement at ${cursor}: ${token.value}`);
    }
  }

  // parse an input statement: input <id> = <expr>
  function parseInputStatement() {
    consume("KEYWORD", "input");
    const id = consume("IDENTIFIER");
    consume("OPERATOR", "=");
    const expr = parseExpression();
    return {
      type: "InputStatement",
      id: { type: "Identifier", name: id.value },
      expression: expr,
    };
  }

  // parse a print statement: print <expr>
  function parsePrintStatement() {
    consume("KEYWORD", "print");
    const expr = parseExpression();
    return {
      type: "PrintStatement",
      expression: expr,
    };
  }

  // parse expressions with precedence (lowest level starts here)
  function parseExpression() {
    return parseAdditive();
  }

  // handle lower precedence (+, -)
  function parseAdditive() {
    let node = parseMultiplicative();
    while (
      peek() &&
      peek().type === "OPERATOR" &&
      (peek().value === "+" || peek().value === "-")
    ) {
      const operator = consume("OPERATOR").value;
      const right = parseMultiplicative();
      node = { type: "BinaryExpression", operator, left: node, right };
    }
    return node;
  }

  // handle * and / operators (higher precedence)
  function parseMultiplicative() {
    let node = parsePrimary();
    while (
      peek() &&
      peek().type === "OPERATOR" &&
      (peek().value === "*" || peek().value === "/")
    ) {
      const operator = consume("OPERATOR").value;
      const right = parsePrimary();
      node = { type: "BinaryExpression", operator, left: node, right };
    }
    return node;
  }

  // parse numbers or identifiers
  function parsePrimary() {
    const token = peek();
    if (token.type === "NUMBER") {
      consume("NUMBER");
      return { type: "NumberLiteral", value: Number(token.value) };
    } else if (token.type === "IDENTIFIER") {
      consume("IDENTIFIER");
      return { type: "Identifier", name: token.value };
    } else {
      throw new Error(`Unexpected token in expression: ${token.value}`);
    }
  }

  return parseProgram();
}

function lexer(code) {
  const tokens = [];
  let cursor = 0;

  const tokenSpecs = [
    { type: "COMMENT", regex: /^\/\/.*|^\/\*[\s\S]*?\*\// },
    { type: "WHITESPACE", regex: /^\s+/ },
    { type: "KEYWORD", regex: /^(input|print)\b/ },
    // This Regex ensures identifiers DON'T start with a digit
    { type: "IDENTIFIER", regex: /^[a-zA-Z_][a-zA-Z0-9_]*/ },
    { type: "NUMBER", regex: /^[0-9]+(\.[0-9]+)?/ },
    { type: "OPERATOR", regex: /^[=+*/-]/ },
  ];

  while (cursor < code.length) {
    const remainingCode = code.slice(cursor);
    let matched = false;

    for (const { type, regex } of tokenSpecs) {
      const match = remainingCode.match(regex);

      if (match) {
        const value = match[0];

        // LOGIC CHECK: If we just matched a NUMBER,
        // check if the very next character is a letter.
        if (type === "NUMBER") {
          const nextChar = remainingCode[value.length];
          if (nextChar && /[a-zA-Z_]/.test(nextChar)) {
            throw new Error(
              `Lexical Error: Variable names cannot start with a number ("${value}${nextChar}...")`,
            );
          }
        }

        if (type !== "WHITESPACE" && type !== "COMMENT") {
          tokens.push({ type, value });
        }

        cursor += value.length;
        matched = true;
        break;
      }
    }

    if (!matched) {
      throw new Error(
        `Unexpected character at position ${cursor}: "${code[cursor]}"`,
      );
    }
  }

  return tokens;
}

function main(code) {
  try {
    // 1. Lexing
    const tokens = lexer(code);

    // 2. Parsing
    const ast = parser(tokens);

    // 3. Analyzing
    analyzer(ast);

    // 4. Evaluation (Execution)
    const environment = {}; // This holds our variables during runtime
    evaluate(ast, environment);

  } catch (err) {
    console.error(`COMPILER ERROR: ${err.message}`);
  }
}


const code = `
input a = 5  // initiate
input b = 2.3
/* multiline comment */
input sum = a + b * 4 / 3 //precedence order followed

print sum
`;

main(code);
