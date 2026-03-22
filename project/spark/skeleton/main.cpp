#include <iostream>
#include <string>
#include <algorithm>
#include <vector>
#include <fstream>
#include <regex>

using namespace std;
void execute(const string &fileName);

const vector<string> reservedKeywords = {
    "input",
    "read",
    "const",
    "printn",
    "print",
    "if",
    "elif",
    "else",
    "switch",
    "case",
    "break",
    "continue",
    "function",
    "for",
    "while"};

int main(int argCount, char *argVector[])
{
    if (argCount < 2)
    {
        cout << "Usage: spark <fileName.spark>" << endl;
        return 1;
    }

    string fileName = argVector[1];
    regex fileNamePattern("[a-zA-Z0-9_]+\\.spark");

    if (!regex_match(fileName, fileNamePattern))
    {
        cout << "Invalid File Name" << endl;
        return 1;
    }

    execute(fileName);
}

void execute(const string &fileName)
{
    ifstream file(fileName);

    if (!file)
    {
        cout << "Cannot open file: " << fileName << endl;
        return;
    }

    string line;
    int lineNumber = 0;

    while (getline(file, line))
    {
        lineNumber++;
        // For now, just show the line
        cout << lineNumber << ": " << line << endl;

        // TODO: Add your language logic here in the future
    }
}