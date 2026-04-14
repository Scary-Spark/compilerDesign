const ignoreWords = [
  "a",
  "an",
  "the",
  "and",
  "or",
  "but",
  "so",
  "yet",
  "for",
  "nor",
  "on",
  "in",
  "at",
  "by",
  "with",
  "about",
  "against",
  "between",
  "into",
  "through",
  "during",
  "before",
  "after",
  "above",
  "below",
  "to",
  "from",
  "up",
  "down",
  "over",
  "under",
  "again",
  "further",
  "then",
  "once",
];

function maxFrequencyWord(text) {
  const cleanedText = text
    .toLowerCase()
    .replace(/[.,\/#!$%\^&\*;:{}=\-_`~()]/g, "");

  const words = cleanedText.split(/\s+/);

  const frequency = {};
  for (const word of words) {
    if (!ignoreWords.includes(word) && word !== "") {
      frequency[word] = (frequency[word] || 0) + 1;
    }
  }

  let maxWord = "";
  let maxCount = 0;
  for (const word in frequency) {
    if (frequency[word] > maxCount) {
      maxWord = word;
      maxCount = frequency[word];
    }
  }

  return { word: maxWord, count: maxCount };
}

const inputString =
  "The cat and the dog played in the garden. The dog was very happy.";
const result = maxFrequencyWord(inputString);
console.log(
  `The most frequent word is "${result.word}" with ${result.count} occurrences.`,
);
