function checkRE(input, regex) {
  // Regex for (ab) repeated 2 or more times

  if (regex.test(input)) {
    return "Accepted";
  } else {
    return "Rejected";
  }
}

// Example usage:
let testStrings = ["ab", "abab", "ababab", "aba", "abba", "abababab"];
const regex = /^(ab){2,}$/;

for (let str of testStrings) {
  console.log(`${str}: ${checkRE(str, regex)}`);
}
