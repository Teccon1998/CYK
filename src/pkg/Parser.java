package pkg;

import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    private ArrayList<Token> TokenList;

    public Parser(ArrayList<Token> TokenList)
    {
        this.TokenList = TokenList;
    }

    public Token matchAndRemove(Token.TokenType tokenType)
    {
        if(TokenList.get(0).getTokenType().equals(tokenType))
        {
            return TokenList.get(0);
        }
        else
        {
            return null;
        }
    }

    public HashMap<String,ArrayList<Token>> parse()
    {
        HashMap<String,ArrayList<Token>> RuleSet = new HashMap<>();

        for(int i = 0; i<TokenList.size(); i++)
        {
            Token Token = TokenList.get(i);

        }

        return RuleSet;
    }
}
