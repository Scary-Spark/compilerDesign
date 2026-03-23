#include <iostream>
#include <string>
#include <algorithm>
#include <vector>
#include <fstream>
#include <regex>

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

const vector<char> reservedSymbols = {
    ';',
    ',',
    '(',
    ')',
    '{',
    '}',
    '[',
    ']'};

const vector<string> reservedOperators = {
    "+",
    "-",
    "*",
    "/",
    "%",
    "=",
    "==",
    "!=",
    ">",
    "<",
    ">=",
    "<=",
    "++",
    "--"};

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

    Token(TokenType tokenType, const string &v) : type(tokenType), value(v) {} // called initializer list
    // also can be declared using normal constructor way

    // --------------
    //      Logs
    // --------------
    void print()
    {
        string typeName;
        switch (type)
        {
        case TokenType::IDENTIFIER: // :: called scope resolution operator
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

string keywordPatternCreator()
{
    string finalPattern = "\\b(";
    bool start = true;

    for (string s : reservedKeywords)
    {
        if (!start)
            finalPattern += "|";

        finalPattern += s;
        start = false;
    }

    finalPattern += ")\\b";

    return finalPattern;
}

string symbolPatternCreator()
{
    string finalPattern = "[";
    for (char c : reservedSymbols)
    {
        if (c == '[' || c == ']' || c == '(' || c == ')' || c == '{' || c == '}')
            finalPattern += '\\';
        finalPattern += c;
    }
    finalPattern += "]";
    return finalPattern;
}

vector<Token> lexer(const string &line)
{
    regex keywordPattern(keywordPatternCreator());
    regex numberPattern("[0-9]+(\\.[0-9]+)?");
    regex identifierPattern("[a-zA-Z_][a-zA-Z0-9_]*");
    regex symbolPattern(symbolPatternCreator());

    vector<Token> tokens;

    size_t i = 0;
    while (i < line.size())
    {
        char c = line[i];

        if (isspace(c))
        {
            i++;
            continue;
        }

        if (c == '"')
        {
            size_t start = i + 1;
            size_t end = line.find('"', start);
            if (end == string::npos)
            {
                cout << "Error: unmatched quotes\n";
                break;
            }
            string content = line.substr(start, end - start);
            tokens.push_back(Token(TokenType::STRING, content));
            i = end + 1;
            continue;
        }

        string s(1, c);
        if (regex_match(s, symbolPattern))
        {
            tokens.push_back(Token(TokenType::SYMBOL, s));
            i++;
            continue;
        }

        bool matchedOperator = false;
        for (auto &op : reservedOperators)
        {
            if (line.substr(i, op.size()) == op)
            {
                tokens.push_back(Token(TokenType::OPERATOR, op));
                i += op.size();
                matchedOperator = true;
                break;
            }
        }
        if (matchedOperator)
            continue;

        size_t start = i;
        while (i < line.size() && !isspace(line[i]) &&
               !regex_match(string(1, line[i]), symbolPattern))
            i++;
        string word = line.substr(start, i - start);

        if (regex_match(word, keywordPattern))
            tokens.push_back(Token(TokenType::KEYWORD, word));
        else if (regex_match(word, numberPattern))
            tokens.push_back(Token(TokenType::NUMBER, word));
        else if (regex_match(word, identifierPattern))
            tokens.push_back(Token(TokenType::IDENTIFIER, word));
        else
            tokens.push_back(Token(TokenType::UNKNOWN, word));
    }

    return tokens;
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
        vector<Token> tokens = lexer(line);
        cout << lineNumber << ": ";
        for (auto &t : tokens)
            t.print();
        cout << endl;
    }
}

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
