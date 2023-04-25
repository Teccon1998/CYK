package pkg;
import java.util.logging.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class App {
    static Logger logger = Logger.getGlobal();
    final static boolean LogSwitch = false; //Global logging switch.
    public static void main(String args[]) throws Exception
    {
        /*
         * Logging Handler if logging is enabled
         */
        if(LogSwitch)
        {
            FileHandler fh = new FileHandler("C://Users//alexa//OneDrive//Desktop//CYK Algorithm//src//pkg//Log.txt");  
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter(); 
            fh.setFormatter(formatter);  
        }

        /*
         * Start of CYK program
         */

        /*
         * Collect the strings from the input file.
         */
        ArrayList<String> UnlexedStrings = new ArrayList<>();
        
        Path path = Paths.get("C://Users//alexa//OneDrive//Desktop//CYK Algorithm//src//pkg//grammar.txt");
        UnlexedStrings.addAll(Files.readAllLines(path));
        //Logs unlexed strings
        if(LogSwitch)
        {
            logger.info("UnlexedStrings:");
            for(String s : UnlexedStrings)
            {
                logger.info(s);
            }
        }

        /*
         * Lexes the strings input from the file.
         */
        Lexer lexer = new Lexer(UnlexedStrings,logger);
        ArrayList<Token> TokenList = new ArrayList<>();
        try {
            TokenList.addAll(lexer.Lex());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.severe(sw.toString());
            System.exit(1);
        }
        /*
         * Logs the TokenList 
         */
        if(LogSwitch)
        {
            logger.info("TokenList:");
            for(Token t : TokenList)
            {
                logger.info(t.toString());
            }
        }
        /*
         * Parses the tokens recursively to add them to a hashmap for usage later.
         */
        Parser parser = new Parser(TokenList,logger);
        HashMap<String,ArrayList<Token>> hashMap = parser.parse();
        
        /*
         * Checking that for each ruleset there exists a correpsonding key rule.
         */

        for (String s : hashMap.keySet()) 
        {
            ArrayList<Token> ruleSetForRule = hashMap.get(s);
            for (Token token : ruleSetForRule) {
                if(!token.getTokenType().equals(Token.TokenType.EPSILON))
                {
                    String TokenString = token.getValue();
                    for(int i = 0; i<TokenString.length(); i++)
                    {
                        if(Character.isUpperCase(TokenString.charAt(i)))
                        {
                            if(!hashMap.containsKey(Character.toString(TokenString.charAt(i))))
                            {
                                logger.severe("Rule does not exist for nonterminal in rule: "+ s + ", Error in rule: "+ TokenString);
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                try 
                                {
                                    throw new Exception("Rule does not exist for nonterminal in rule: "+ s + ", Error in rule: "+ TokenString);
                                } catch (Exception e) {
                                    e.printStackTrace(pw);
                                    logger.severe(sw.toString());
                                }
                            }
                        }
                    }
                }
            }
        }
        /*
         * Logs the hashmap of rules.
         */
        
        if(LogSwitch)
        {
            logger.info("HashMap of Rules:");
            for (String str : hashMap.keySet()) {
                logger.info(str+":");
                for(Token t : hashMap.get(str))
                {
                    logger.info(t.getValue());
                }
            }
        }

        //Taking in an input string, peforming CYK
        // Scanner tempScan = new Scanner(System.in);
        // System.out.println("Enter the string you'd like to perform CYK on below: ");
        // String inputStr = tempScan.nextLine();
        String inputString = "aabaa";
        HashMap<Character,ArrayList<String>> nonTermMap = new HashMap<Character, ArrayList<String>>();
        if(inputString.equals("\n"))
        {
            throw new Exception("Input string must not be null.");
        }
        for(char tempChar : inputString.toCharArray())
        {
            ArrayList<String> temp = new ArrayList<>();
            if(nonTermMap.containsKey(tempChar))
            {
                continue;
            }
            for (String s : hashMap.keySet()) 
            {
                //redundancy check
                
                for(Token t : hashMap.get(s))
                {
                    if(t.getTokenType().equals(Token.TokenType.TERMINAL))
                    {
                        if(t.getValue().charAt(0) == tempChar)
                        {
                            temp.add(s);
                            nonTermMap.put(tempChar, temp);
                            break;
                        }
                    }
                }    
                
            }

        }
        System.out.println();
        
    }
}

