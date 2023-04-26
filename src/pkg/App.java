package pkg;
import java.util.logging.*;
import java.io.IOException;
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
        ArrayList<Token> TokenList = Lex();    
        String inputString = TokenList.remove(0).getValue();  
        HashMap<String,ArrayList<Token>> hashMap = parse(TokenList);  
        HashMap<Character,ArrayList<String>> nonTermMap = NonTermMap(inputString,hashMap);
        if(terminalNotExistsFromInput(inputString,nonTermMap))
        {
            try {
                throw new Exception();
            } catch (Exception e) {
                logger.severe("InputString contains a terminal that is not part of the language.");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.severe(sw.toString());
                System.exit(1);
            }
        }
        ArrayList<String> List = new ArrayList<String>(Collections.fill(10, 10););

        System.out.println("Content of ArrayList:"+List);
        // for(int i = 0; i < inputString.length(); i++)
        // {
        //     Character c = inputString.charAt(i);
        //     aStrings.se
        // }
        System.out.println(List);
    }

    public static ArrayList<Token>Lex() throws IOException
    {
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
        return TokenList;
    }
    
    public static HashMap<String,ArrayList<Token>> parse(ArrayList<Token> TokenList) throws Exception
    {
        //Parses each line into a hashmap of rules.
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
        return hashMap;
    }    
    
    public static HashMap<Character,ArrayList<String>> NonTermMap(String inputString, HashMap<String,ArrayList<Token>> hashMap)
    {
        /*
         * Inputting string for 
         */
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
        return nonTermMap;
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
    
    public static boolean terminalNotExistsFromInput(String inpuString,HashMap<Character,ArrayList<String>> nonTermMap)
    {
        for(char c : inpuString.toCharArray())
        {
            if(nonTermMap.containsKey(c))
            {
                continue;
            }
            else
            {
                return true;
            }

        }
        return false;
    }
}

