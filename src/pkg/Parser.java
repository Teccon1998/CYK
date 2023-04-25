package pkg;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.*;

public class Parser {
    private ArrayList<Token> TokenList;
    Logger logger;

    public Parser(ArrayList<Token> TokenList,Logger logger)
    {
        this.TokenList = TokenList;
        this.logger = logger;
    }

    //Checks for tokens and returns them so we can deal with tokens in the list
    public Token matchAndRemove(Token.TokenType tokenType)
    {
        if(TokenList.get(0).getTokenType().equals(tokenType))
        {
            return TokenList.remove(0);
        }
        else
        {
            return null;
        }
    }
    //Peek to reduce the chance of nullpointer exceptions
    public Token peek()
    {
        if(TokenList.size() == 0)
            return null;
        return TokenList.get(0);
    }

    //Recursively check for rules. Only adds if nonnull.
    public HashMap<String,ArrayList<Token>> parse() throws Exception
    {
        HashMap<String,ArrayList<Token>> hashMap = new HashMap<>();
        while(TokenList.size() != 0)
        {
            hashMap.putAll(RuleRow());
        }
        return hashMap;
    }
    
    //Recursively checks for each token and builds hashmap for later rule lookups.
    public HashMap<String,ArrayList<Token>> RuleRow() throws Exception
    {
        HashMap<String,ArrayList<Token>> RuleSet = new HashMap<>();
        ArrayList<Token> Rules = new ArrayList<>();
        
        String rule = "";
        Token token = matchAndRemove(Token.TokenType.NONTERMINAL);
        if(token == null)
        {
            logger.severe("No nonterminal detected. Exiting.");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            try {
                throw new Exception("No nonterminal detected. Exiting.");
            } catch (Exception e) {
                e.printStackTrace(pw);
                logger.severe(sw.toString());
            }
        }
            // logger.severe
            // throw new Exception("No nonterminal detected. Exiting.");
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
