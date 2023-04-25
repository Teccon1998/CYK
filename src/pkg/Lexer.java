package pkg;
import java.util.ArrayList;
public class Lexer {

    private enum State {RULESET,START};
    private ArrayList<String> UnlexedStrings = new ArrayList<>();
    public Lexer(ArrayList<String> UnlexedStrings)
    {
        this.UnlexedStrings = UnlexedStrings;
    }

    public ArrayList<Token> Lex() throws Exception
    {
        
        ArrayList<Token> TokenList = new ArrayList<>();
        //We need to loop over each string in the list
        for(String str : this.UnlexedStrings)
        {
            /*
             * Need to loop over each character. Cases checking for every style of grammar input.
             * We delimit over pipe characters '|' not caring about anything specific. 
             * That will be taken care of in the parser.
             */
            String StringGrouping = "";
            State state = State.START; 
            for(int i = 0; i< str.length(); i++)
            {
                Character c = str.charAt(i);
                switch(state)
                {
                    case START:
                        if(Character.isUpperCase(c))
                        {
                            StringGrouping += c;
                        }
                        else if(Character.isWhitespace(c))
                        {
                            //Do nothing, we dont want to create a lexeme for nothing.
                        }
                        else if(c == ':')
                        {
                            /*
                             * Adds the nonterminal from the inital rule.
                             */
                            TokenList.add(new Token(Token.TokenType.NONTERMINAL,StringGrouping));
                            TokenList.add(new Token(Token.TokenType.RULERELATION,":"));
                            StringGrouping = "";
                            state = State.RULESET;
                        }
                        else
                        {
                            throw new Exception("No valid rule found. Exiting.");
                        }
                        break;
                    case RULESET:
                        if(Character.isUpperCase(c))
                        {
                            StringGrouping += c;
                        }
                        else if(Character.isLowerCase(c))
                        {
                            StringGrouping += c;
                        }
                        else if(Character.isWhitespace(c))
                        {
                            //Do nothing, dont want to create lexemes 
                        }
                        else if(c == '|')
                        {
                            /*
                             * Rule is detected we want to take whatever grouping is found.
                             * Create a rule based on the values in the stored string.
                             * Determines the token type based on case of the stored String.
                             */
                            if(StringGrouping.length() == 0)
                            {
                                TokenList.add(new Token(Token.TokenType.RULERELATION,"|"));
                            }
                            
                            else if(Character.isLowerCase(StringGrouping.charAt(0)))
                            {
                                TokenList.add(new Token(Token.TokenType.TERMINAL,StringGrouping));
                                StringGrouping = "";
                            }
                            else if(Character.isUpperCase(StringGrouping.charAt(0)))
                            {
                                TokenList.add(new Token(Token.TokenType.NONTERMINAL,StringGrouping));
                                StringGrouping = "";
                            }
                            else
                            {
                                TokenList.add(new Token(Token.TokenType.RULERELATION,"|"));
                            }
                        }
                        break;
                }
                
            }
            if(Character.isLowerCase(StringGrouping.charAt(0)))
            {
                TokenList.add(new Token(Token.TokenType.TERMINAL,StringGrouping));
            }
            else if(Character.isUpperCase(StringGrouping.charAt(0)))
            {
                TokenList.add(new Token(Token.TokenType.NONTERMINAL,StringGrouping));
                StringGrouping = "";
            }
            for(int i = 0; i< TokenList.size(); i++)
            {
                if(TokenList.get(i).getValue().equals("epsilon"))
                {
                    TokenList.set(i, new Token(Token.TokenType.EPSILON, "EPSILON"));
                }
            }
            TokenList.add(new Token(Token.TokenType.ENDOFLINE,"EOL"));
        }
        return TokenList;
    }
}
