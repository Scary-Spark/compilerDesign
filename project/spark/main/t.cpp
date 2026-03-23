#include <iostream>
#include <regex>
#include <vector>
#include <cstring>

using namespace std;

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

string keywordPatternCreator()
{
    string finalPattern = "";
    bool start = true;

    for (string s : reservedKeywords)
    {
        if (!start)
        {
            finalPattern += "|";
            finalPattern += s;
            continue;
        }
        finalPattern += s;
        start = false;
    }

    return finalPattern;
}

const vector<char> reservedSymbols = {
    ';',
    ',',
    '(',
    ')',
    '{',
    '}',
    '[',
    ']'};

string symbolPatternCreator()
{
    string finalPattern = "[";
    bool start = true;

    for (char c : reservedSymbols)
    {
        if (!start)
            finalPattern += "|";
        finalPattern += c;
        start = false;
    }

    finalPattern += "]";

    return finalPattern;
}

int main()
{
    string keyPattern = keywordPatternCreator();
    cout << keyPattern << endl;
    regex keywordPattern(keyPattern);

    keyPattern = symbolPatternCreator();
    cout << keyPattern << endl;
    regex symbonPattern(keyPattern);
    char c = ']';
    if (find(reservedSymbols.begin(), reservedSymbols.end(), c) != reservedSymbols.end())
    {
        cout << "Exists" << endl;
    }
}