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

enum class TokenType
{
    IDENTIFIER, // varibale, function names
    NUMBER,     // 123, 3.14
    STRING,     // "hello"
    KEYWORD,    // if, else, printn, etc.
    OPERATOR,   // +, -, *, /, %, =, ==, >=, <=, >, <, !=, ++, --,
    SYMBOL,     // ;, , , (), {}, []
    UNKNOWN
};

class Token
{
public:
    TokenType type;
    string value;

    Token(TokenType tokenType, const string &v) : type(tokenType), value(v) {}

    // --------------
    //      Logs
    // --------------
    void print()
    {
        string typeName;
        switch (type)
        {
        case TokenType::IDENTIFIER:
            typeName = "IDENTIFIER";
            break;
        case TokenType::NUMBER:
            typeName = "NUMBER";
            break;
        case TokenType::STRING:
            typeName = "STRING";
            break;
        case TokenType::KEYWORD:
            typeName = "KEYWORD";
            break;
        case TokenType::OPERATOR:
            typeName = "OPERATOR";
            break;
        case TokenType::SYMBOL:
            typeName = "SYMBOL";
            break;
        default:
            typeName = "UNKNOWN";
        }
        cout << "[" << typeName << ": " << value << "] ";
    }
};

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