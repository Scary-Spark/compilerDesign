function removeComment(code) {
  let result = "";
  let i = 0;
  let inSingleLine = false;
  let inMultiLine = false;

  while (i < code.length) {
    // Check for start of single-line comment
    if (
      !inSingleLine &&
      !inMultiLine &&
      code[i] === "/" &&
      code[i + 1] === "/"
    ) {
      inSingleLine = true;
      i += 2;
      continue;
    }

    // Check for start of multi-line comment
    if (
      !inSingleLine &&
      !inMultiLine &&
      code[i] === "/" &&
      code[i + 1] === "*"
    ) {
      inMultiLine = true;
      i += 2;
      continue;
    }

    // End of single-line comment
    if (inSingleLine && code[i] === "\n") {
      inSingleLine = false;
      result += code[i];
      i++;
      continue;
    }

    // End of multi-line comment
    if (inMultiLine && code[i] === "*" && code[i + 1] === "/") {
      inMultiLine = false;
      i += 2;
      continue;
    }

    // If not inside a comment, copy character
    if (!inSingleLine && !inMultiLine) {
      result += code[i];
    }

    i++;
  }

  return result;
}

// Example usage:
let code = `
int a = 5; // initialize a
/* a multi
line comment */
int b = 10;
`;

console.log(removeComment(code));
