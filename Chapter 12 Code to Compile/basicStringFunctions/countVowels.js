function countVowels(str) {
  let count = 0;
  let i = 0;
  const vowels = ["a", "e", "i", "o", "u"];

  while (str[i] !== undefined) {
    if (vowels.includes(str[i].toLowerCase())) {
      // you can use use hard coded if statmetns here also
      // such as
      /*
            if(str[i]==='a' || (and so on...) ) 
      */

      count++;
    }
    i++;
  }

  return count;
}

let str = "Hena lives in Tokyo!";
console.log(`Number of vowels: ${countVowels(str)}`);
