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
        String StartRule = "C";//TODO:Find the start rule from the tokenList
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
        ArrayList<ArrayList<ArrayList<String>>> CYKMap = createCYKMap(inputString, hashMap, nonTermMap);
        if(CYKMap.get(CYKMap.size()-1).get(0).contains(StartRule))
        {
            System.out.println("THIS STRING IS IN THIS LANGUAGE.");
        }
        else
        {
            System.out.println("THIS STRING IS NOT IN THE LANGUAGE.");
        }
    }
    /*
     * This is super annoying implementation, but it WORKS!
     * You initalize the first row. Then call 
     */
    public static ArrayList<ArrayList<ArrayList<String>>> createCYKMap(String inputString, HashMap<String,ArrayList<Token>> hashMap, HashMap<Character,ArrayList<String>> nonTermMap)
    {
        ArrayList<ArrayList<ArrayList<String>>> CYKMap = new ArrayList<>();

        //size init of triangle table
        for(int i = 0; i < inputString.length(); i++)
        {
            CYKMap.add(new ArrayList<ArrayList<String>>());
            
            for(int j = 0; j <= i; j++)
            {
                CYKMap.get(i).add(new ArrayList<String>());
            }
        }
        logger.info("Length of input String: "+inputString.length());
        logger.info("Length of inital row: "+CYKMap.size());
        //Initalize the first row of the table.
        for(int i = 0; i < inputString.length(); i++)
        {
            for(int k = 0; k < nonTermMap.get(inputString.charAt(i)).size(); k++)
            {
                CYKMap.get(i).get(i).add(nonTermMap.get(inputString.charAt(i)).get(k));
            }
            String loggerString = "Rules for X_"+i+","+i+": ";
            int p=0;
            for(String str : nonTermMap.get(inputString.charAt(i)))
            {
                p++;
                if(nonTermMap.get(inputString.charAt(i)).size()==0)
                {
                    loggerString += "0";
                }
                else if(nonTermMap.get(inputString.charAt(i)).size()==1)
                {
                    loggerString += str;
                }
                else if(p==nonTermMap.get(inputString.charAt(i)).size())
                {
                    loggerString += str;
                }
                else
                {
                    loggerString += str +", ";
                }
            }
            logger.info(loggerString);
        }
        //For each next row these two for loops run every other column and row in a triangle order.
        for(int k = 1; k < inputString.length(); k++)
        {
            for(int i = k, j =0 ; i < inputString.length(); i++, j++)
            {
                CYKMap = CYKSquare(j, i,CYKMap,hashMap);
            }
        }

        // printCYKMAP(CYKMap); //TODO: Need a more efficient way of printing this garbage.
        return CYKMap;
    }

    public static void printCYKMAP(ArrayList<ArrayList<ArrayList<String>>> CYKMap)
    {
        //TODO:Make this not shit.
        System.out.print("| ");
        for(int i = 0; i < CYKMap.size(); i++)
        {
            for(String s : CYKMap.get(i).get(i))
            {
                System.out.print(s + " ");
            }    
            System.out.print(" | ");

        }

        
        System.out.println();

        
        System.out.print("| ");
        for(int k = 1; k < CYKMap.size(); k++)
        {
            for(int i = k, j =0 ; i < CYKMap.size(); i++, j++)
            {
                if(CYKMap.get(i).get(j).size() == 0)
                {
                    System.out.print("0 ");
                }
                for(String s : CYKMap.get(i).get(j))
                {
                    System.out.print(s + " ");
                }
                
                System.out.print(" | ");
            }
            
            System.out.println();
        }

    }

    public static ArrayList<ArrayList<ArrayList<String>>> CYKSquare(int i, int j,ArrayList<ArrayList<ArrayList<String>>> CYKMap,HashMap<String,ArrayList<Token>> Ruleset)
    {
        ArrayList<String> UnionedRules = new ArrayList<>();
        int l = i;
        int log = i+1;
        String cartesianLog = "Cartesian of X_"+(l+1)+","+(i+1) + " and X_"+(j+1)+","+(log+1)+": ";
        for(int m = i+1; m<=j; m++)
        {
            ArrayList<String> CartesianedRules = cartesian(CYKMap.get(l).get(i),CYKMap.get(j).get(m));
            
            for (String string : CartesianedRules) 
            {
                cartesianLog += string+", ";
                UnionedRules.add(string);
            }
            l++;
            log++;
        }
        if(UnionedRules.size() == 0)
        {
            cartesianLog += ": NULL";
        }
        else
        {
            cartesianLog = cartesianLog.substring(0,cartesianLog.length()-2);
        }
        logger.info(cartesianLog);

        String loggerRuleExists = "Rules containing Cartesian: ";
        int logString = loggerRuleExists.length();
       
        for(String str : UnionedRules)
        {
            for(String s : Ruleset.keySet())
            {
                for(Token t : Ruleset.get(s))
                {
                    if(t.getValue().equals(str))
                    {
                        loggerRuleExists += s+", ";
                        if(!CYKMap.get(j).get(i).contains(s))
                        {
                            CYKMap.get(j).get(i).add(s);
                        };
                        break;
                    }
                }
            }
        }
        if(loggerRuleExists.length() == logString)
        {
            loggerRuleExists += "NULL";
        }
        else
        {
            loggerRuleExists = loggerRuleExists.substring(0, loggerRuleExists.length()-2);
        }
        logger.info(loggerRuleExists);
        logger.info("Adding these to location: X_"+(i+1)+","+(j+1));
        return CYKMap;   
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

