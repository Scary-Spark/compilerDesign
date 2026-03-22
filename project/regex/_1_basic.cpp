#include <iostream>
#include <regex>

using namespace std;
int main()
{
    string s = "helloww";
    regex pattern("hello[a-z]");

    if (regex_match(s, pattern))
    {
        cout << "Matched";
    }
    else
    {
        cout << "Not matched";
    }
}