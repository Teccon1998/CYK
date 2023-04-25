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
            Token token = TokenList.remove(0);
            return token;
        }
        else
        {
            return null;
        }
    }
    public Token peek()
    {
        if(TokenList.size() == 0)
            return null;
        return TokenList.get(0);
    }

    public HashMap<String,ArrayList<Token>> parse() throws Exception
    {
        HashMap<String,ArrayList<Token>> hashMap = new HashMap<>();
        while(TokenList.size() != 0)
        {
            hashMap.putAll(RuleRow());
        }
        return hashMap;
    }
    

    public HashMap<String,ArrayList<Token>> RuleRow() throws Exception
    {
        HashMap<String,ArrayList<Token>> RuleSet = new HashMap<>();
        ArrayList<Token> Rules = new ArrayList<>();
        
        String rule = "";
        Token token = matchAndRemove(Token.TokenType.NONTERMINAL);
        if(token == null)
            throw new Exception("No nonterminal detected. Exiting.");
        rule = token.getValue();
        if(matchAndRemove(Token.TokenType.RULERELATION)==null)
            throw new Exception("Improper Formatting. Next token must be a RULE RELATION SYMBOL");
        do 
        {
            if(peek().getTokenType().equals(Token.TokenType.NONTERMINAL))
            {
                Rules.add(matchAndRemove(Token.TokenType.NONTERMINAL));
            }
            else if(peek().getTokenType().equals(Token.TokenType.TERMINAL))
            {
                Rules.add(matchAndRemove(Token.TokenType.TERMINAL));
            }
            else if(peek().getTokenType().equals(Token.TokenType.EPSILON))
            {
                Rules.add(matchAndRemove(Token.TokenType.EPSILON));
            }
            else
            {
                throw new Exception("No valid token detected.");
            }
        } 
        while (matchAndRemove(Token.TokenType.ENDOFLINE)== null);
        RuleSet.put(rule, Rules);
        return RuleSet;
    }
}
