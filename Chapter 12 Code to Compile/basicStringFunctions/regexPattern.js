function checkRE(input, regex) {
  if (regex.test(input)) {
    return "Accepted";
  } else {
    return "Rejected";
  }
}

// Example usage:
let testStrings = ["ab", "abab", "ababab", "aba", "abba", "abababab"];
const regex = /^(ab){2,}$/; // Regex for (ab) repeated 2 or more times

for (let str of testStrings) {
  console.log(`${str}: ${checkRE(str, regex)}`);
}
