function removeWhiteSpace(str) {
  let result = "";
  let i = 0;

  while (str[i] !== undefined) {
    if (str[i] !== " ") {
      // skip spaces

      result += str[i]; // build new string
    }
    i++;
  }

  return result;
}

let str = "   int   a = 5;   ";
console.log(`New string: "${removeWhiteSpace(str)}"`);
