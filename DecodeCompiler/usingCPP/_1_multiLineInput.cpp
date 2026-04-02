#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main()
{
    vector<string> sourceCode; // Store each line of code
    string line;

    cout << "Enter your code (type END to finish):\n";

    int count = 0; // optional: count number of lines
    while (true)
    {
        getline(cin, line); // Read one line from input
        if (line == "END")
            break; // Stop when user, if line only contain "END"
        count++;
        sourceCode.push_back(line); // Store line in vector
    }

    // Display the collected lines (optional)
    cout << "\nSource Code Entered:\n";
    for (auto l : sourceCode)
    {
        cout << l << endl;
    }
    cout << "Total Lines: " << count << endl;

    return 0;
}