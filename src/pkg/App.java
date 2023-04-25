package pkg;
import java.util.logging.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class App {
    static Logger logger = Logger.getGlobal();
    final static boolean LogSwitch = true; //Global logging switch.
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
                                    System.exit(1);
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
        /*
         * Inputting string for 
         */
        String inputString = "aabaa";
        HashMap<Character,ArrayList<String>> nonTermMap = new HashMap<Character, ArrayList<String>>();
        if(inputString.equals("\n"))
        {
            logger.severe("Input string must not be null.");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            try 
            {
                throw new Exception("Input String must not be null.");
            } 
            catch (Exception e) 
            {
                e.printStackTrace(pw);
                logger.severe(sw.toString());
                System.exit(1);
            }
        }

        /*
         * Creates another map for terminals and all the rules that contain that nonterminal. 
         */
        for(char tempChar : inputString.toCharArray())
        {
            ArrayList<String> temp = new ArrayList<>();
            if(nonTermMap.containsKey(tempChar))
            {
                continue;
            }
            for (String s : hashMap.keySet()) 
            {
                for(Token t : hashMap.get(s))
                {
                    if(t.getTokenType().equals(Token.TokenType.TERMINAL))
                    {
                        if(t.getValue().charAt(0) == tempChar)
                        {
                            temp.add(s);
                            //add the rule and it's corresponding relation into a hashmap
                            nonTermMap.put(tempChar, temp);
                            break;
                        }
                    }
                }    

            }
        }

        char a = 'a';
        char b = 'b';
        System.out.println(cartesian(nonTermMap.get(a),nonTermMap.get(b)));
    }

    public static ArrayList<String> cartesian(ArrayList<String> arr1, ArrayList<String> arr2)
    {
        ArrayList<String> result = new ArrayList<>();
        for (String str1 : arr1) {
            for (String str2 : arr2) {
                result.add(str1 + str2);
            }
        }
        return result;
    }
}

