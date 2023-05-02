package pkg;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;


public class App {
    
    final static boolean LogSwitch = true; //Global logging switch.
    static Logger logger = Logger.getGlobal();
    public static String StartRule = "";
    
    /*
     * Longest algorithm is O(n^3) bringing the time complexity to O(n^3)
     */
    public static void main(String[] args) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        /*
         * Logging Handler if logging is enabled
         */

        if (LogSwitch) {
            String projectDir = System.getProperty("user.dir");
            String logFilePath = projectDir + "/tests/Log.txt";
            FileHandler fh = new FileHandler(logFilePath);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        }
        /*
         * Here to line 84 is the entirety of the process. Scroll to each method to see what it does.
         * We lex our tokens, remove the first token to store our input string which is lexed.
         * We store the tokens in a copy of the array for debugging and usage to find the start rule after parsing.
         * We find our start rule and if there is no epsilon in the rule then we collect the first rule found.
         * We create a map of our nonterminals to our terminals. We do this so we can easily collect our rules for each character.
         * When we create our first row of the CYK we no longer need the nonterminal map. 
         */
        ArrayList<Token> TokenList = Lex();
        String inputString = TokenList.remove(0).getValue();
        ArrayList<Token> TokenListStore = new ArrayList<>(TokenList);
        HashMap<String, ArrayList<Token>> hashMap = parse(TokenList);
        StartRule = findStartRule(hashMap);
        if(StartRule == null)
        {
            StartRule = TokenListStore.get(0).getValue();
        }
        
        HashMap<Character, ArrayList<String>> nonTermMap = NonTermMap(inputString, hashMap);
        if (terminalNotExistsFromInput(inputString, nonTermMap)) {
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
        System.out.println("Start Rule: " + StartRule);
        ArrayList<ArrayList<ArrayList<String>>> CYKMap = createCYKMap(inputString, hashMap, nonTermMap);
        if (CYKMap.get(CYKMap.size() - 1).get(0).contains(StartRule)) {
            System.out.println("THIS STRING IS IN THIS LANGUAGE.");
            Logger.getGlobal().info("THIS STRING IS IN THIS LANGUAGE.");
        } else {
            System.out.println("THIS STRING IS NOT IN THE LANGUAGE.");
            Logger.getGlobal().info("THIS STRING IS NOT IN THE LANGUAGE.");
        }

        LocalDateTime endTime = LocalDateTime.now();
        logger.info("End of program.");
        // log start rule
        logger.info("Start rule: " + StartRule);
    // Calculate the time elapsed and log it
        Duration timeElapsed = Duration.between(startTime, endTime);
        logger.info("Time elapsed for the entire program: " + timeElapsed.toMillis() + " milliseconds");
    }

    /*
     * Check the hashmap of the rules by looking at the entry value and if the nonterminal contains an epsilon then we can assume its our start rule
     * If no epsilon is found we return null.
     * O(n)
     */
    public static String findStartRule(HashMap<String, ArrayList<Token>> hashMap) {
        // Start tends to have an epsilon transition, so we look for that first or it'll be the first rule typically
        for (Map.Entry<String, ArrayList<Token>> entry : hashMap.entrySet()) {
            ArrayList<Token> tokens = entry.getValue();
            for (Token token : tokens) {
                if (token.getTokenType().equals(Token.TokenType.EPSILON)) {
                    return entry.getKey();
                }
            }
        }
        // If no rule with an epsilon transition is found, return the first rule in the HashMap as the starting rule
        return null;
    }

    /*
     * To create our CYK map we need to create a 3 dimensional arrayList. 
     * We create a coordinate map where each location on the map has an arraylist to store the resulting rules from our
     * cartesian product, and in that array store the list of rules that correspond to that cell's particular cartesian product.
     * O(n log n)
     */
    public static ArrayList<ArrayList<ArrayList<String>>> createCYKMap(String inputString, HashMap<String, ArrayList<Token>> hashMap, HashMap<Character, ArrayList<String>> nonTermMap) {
        ArrayList<ArrayList<ArrayList<String>>> CYKMap = new ArrayList<>();

        //size init of triangle table
        for (int i = 0; i < inputString.length(); i++) {
            CYKMap.add(new ArrayList<ArrayList<String>>());

            for (int j = 0; j <= i; j++) {
                CYKMap.get(i).add(new ArrayList<String>());
            }
        }
        logger.info("Length of input String: " + inputString.length());
        logger.info("Length of inital row: " + CYKMap.size());
        //Initalize the first row of the table.
        for (int i = 0; i < inputString.length(); i++) {
            for (int k = 0; k < nonTermMap.get(inputString.charAt(i)).size(); k++) {
                CYKMap.get(i).get(i).add(nonTermMap.get(inputString.charAt(i)).get(k));
            }
            StringBuilder loggerString = new StringBuilder("Rules for X_" + i + "," + i + ": ");
            int p = 0;
            for (String str : nonTermMap.get(inputString.charAt(i))) {
                p++;
                // If there is only one rule for the non-terminal, don't add a comma
                if (nonTermMap.get(inputString.charAt(i)).size() == 0) {
                    loggerString.append("0");
                } else if (nonTermMap.get(inputString.charAt(i)).size() == 1) {
                    loggerString.append(str);
                } else if (p == nonTermMap.get(inputString.charAt(i)).size()) {
                    loggerString.append(str);
                } else {
                    loggerString.append(str).append(", ");
                }
            }
            logger.info(loggerString.toString());
        }
        //For each next row these two for loops run every other column and row in a triangle order.
        for (int k = 1; k < inputString.length(); k++) {
            for (int i = k, j = 0; i < inputString.length(); i++, j++) {
                // CYKMap get i,j is the current cell we are looking at.
                CYKSquare(j, i, CYKMap, hashMap);
            }
        }

        System.out.println("CYK MAP: ");
        printCYKMAP(CYKMap);
        return CYKMap;
    }


    public static void printCYKMAP(ArrayList<ArrayList<ArrayList<String>>> CYKMap) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Add start time and date stamp
        sb.append("CYKMap printed on: ").append(LocalDateTime.now().format(formatter)).append("\n");

        // Print the top row w/ column numbers
        sb.append(" |   ");
        for (int i = 1; i < CYKMap.size()+1; i++) {
            sb.append(String.format(" | %d   ", i));
        }
        sb.append("|i\n");

        // Print the separator
        sb.append(" |---");
        for (int i = 0; i < CYKMap.size(); i++) {
            sb.append(" |-----"); // 5 dashes
        }
        sb.append("|\n");

        // Print the main table
        for (int i = 0; i <= CYKMap.size()-1; i++) {
            // Print the leftmost column
            /* This if else ensures that the "j" is placed to the left of the first column, ensuring that you can see 
             * how the coordinate system works.
            */
            if(i==0)
            {
                sb.append(String.format("j| %d ",i+1));
            }
            else
            {
                sb.append(String.format(" | %d ", i+1));
            }
            
            // Print the contents of the row
            for (int j = 0; j < CYKMap.size(); j++) {
                // if i < j, then we are in the upper triangle, so print empty cells
                if (i < j) { // Print empty cells
                    sb.append(" |     ");
                } else { // Print the contents of the cell
                    sb.append(" |"); // Add the left separator
                    ArrayList<String> cell = CYKMap.get(i).get(j); // Get the contents of the cell
                    if (cell.size() == 0) {
                        sb.append("  0  "); // Print a 0 if the cell is empty
                    } else { // Print the contents of the cell
                        sb.append(String.format("%-5s", String.join(",", cell)));
                    }
                }
            }
            sb.append("|\n"); // Add the right separator
        }

        // Print the bottom separator
        sb.append(" |---");
        for (int i = 0; i < CYKMap.size(); i++) {
            sb.append(" |-----");
        }
        sb.append("|\n");

        // Add end time and date stamp
        sb.append("CYKMap print ended on: ").append(LocalDateTime.now().format(formatter)).append("\n");

        // Send the output to the logger
        logger.info(sb.toString());

    }

    /*
     * For loops create indicies of appropriate squares for cartesianed rules. 
     * O(n^3)
     */
    public static ArrayList<ArrayList<ArrayList<String>>> CYKSquare(int i, int j, ArrayList<ArrayList<ArrayList<String>>> CYKMap, HashMap<String, ArrayList<Token>> Ruleset) {
        ArrayList<String> UnionedRules = new ArrayList<>();
        int l = i;
        int log = i + 1;
        StringBuilder cartesianLog = new StringBuilder("Cartesian of X_" + (l + 1) + "," + (i + 1) + " and X_" + (j + 1) + "," + (log + 1) + ": ");
        for (int m = i + 1; m <= j; m++) {
            //Takes the appropriate locations of the two squares to be unioned and simply appends the strings together.
            ArrayList<String> CartesianedRules = cartesian(CYKMap.get(l).get(i), CYKMap.get(j).get(m));
            //For each string in the Cartesianed Rules we appened it to the unioned rules,
            //Once we are done with our unioned rules we continue on with logging.
            for (String string : CartesianedRules) {
                cartesianLog.append(string).append(", ");
                UnionedRules.add(string);
            }
            l++;
            log++;
        }
        if (UnionedRules.size() == 0) {
            cartesianLog.append(": NULL");
        } else {
            cartesianLog = new StringBuilder(cartesianLog.substring(0, cartesianLog.length() - 2));
        }
        logger.info(cartesianLog.toString());

        StringBuilder loggerRuleExists = new StringBuilder("Rules containing Cartesian: ");
        int logString = loggerRuleExists.length();
        /*
         * For each string in our unioned rules we check each key, we take that key, 
         * and for each value from that key we get a token.
         * 
         * We take that token and if our token's string value is equal to our unioned rule's value
         * We check and see if that value is already in the keymap. If so we can skip it saving us a bit of time
         * on our get and add. If not we add it to our cyk map at the appropriate location.
         * 
         * Once we find our rule regardless of if its in the CYK or not we break because we dont need
         * to check the rest of that token's rules. Saving us more time.
         */
        for (String str : UnionedRules) {
            for (String s : Ruleset.keySet()) {
                for (Token t : Ruleset.get(s)) {
                    if (t.getValue().equals(str)) {
                        loggerRuleExists.append(s).append(", ");
                        if (!CYKMap.get(j).get(i).contains(s)) {
                            CYKMap.get(j).get(i).add(s);
                        }
                        break;
                    }
                }
            }
        }
        //Once we reach here the CYK map should be finished.
        //below is entirely for logging.
        if (loggerRuleExists.length() == logString) {
            loggerRuleExists.append("NULL");
        } else {
            loggerRuleExists = new StringBuilder(loggerRuleExists.substring(0, loggerRuleExists.length() - 2));
        }

        logger.info(loggerRuleExists.toString());
        logger.info("Adding these to location: X_" + (i + 1) + "," + (j + 1));
        return CYKMap;
    }

    /*
     * O(n)
     */
    public static ArrayList<Token> Lex() throws IOException {


        /*
         * Collect the strings from the input file.
         * Collection from the grammar file is done using the compilation and testing subfolder.
         */
        String projectDir = System.getProperty("user.dir");
        String grammarFilePath = "/tests/grammar.txt";
        grammarFilePath = grammarFilePath.replace("\\", "/");
        grammarFilePath = projectDir + grammarFilePath;

        //Get an arraylist of strings from the grammar file. \n delimits a new element in the arraylist.
        Path path = Paths.get(grammarFilePath);
        ArrayList<String> UnlexedStrings = new ArrayList<>(Files.readAllLines(path));

        //Logs unlexed strings
        if (LogSwitch) {
            logger.info("UnlexedStrings:");
            for (String s : UnlexedStrings) {
                logger.info(s);
            }
        }
        /*
         * Lexes the strings input from the file using Lexer.java
         * and the LexerObj.Lex(); method.
         */
        Lexer lexer = new Lexer(UnlexedStrings, logger);
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
        if (LogSwitch) {
            logger.info("TokenList:");
            for (Token t : TokenList) {
                logger.info(t.toString());
            }
        }
        return TokenList;
    }
    /*
     * Creates a parse object and calls parse on that object. 
     * Everything else is error checking
     * O(n^3)
     */
    public static HashMap<String, ArrayList<Token>> parse(ArrayList<Token> TokenList) throws Exception {
        //Parses each line into a hashmap of rules.
        Parser parser = new Parser(TokenList, logger);
        HashMap<String, ArrayList<Token>> hashMap = parser.parse();
        /*
         * Checking that for each ruleset there exists a correpsonding key rule.
         * We do this so there are no null values and no rules with only epsilon moves.
         * This is primarily for errorchecking.
         */
        for (String s : hashMap.keySet()) {
            ArrayList<Token> ruleSetForRule = hashMap.get(s);
            for (Token token : ruleSetForRule) {
                if (!token.getTokenType().equals(Token.TokenType.EPSILON)) {
                    String TokenString = token.getValue();
                    for (int i = 0; i < TokenString.length(); i++) {
                        if (Character.isUpperCase(TokenString.charAt(i))) {
                            if (!hashMap.containsKey(Character.toString(TokenString.charAt(i)))) {
                                logger.severe("Rule does not exist for nonterminal in rule: " + s + ", Error in rule: " + TokenString);
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                try {
                                    throw new Exception("Rule does not exist for nonterminal in rule: " + s + ", Error in rule: " + TokenString);
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
        if (LogSwitch) {
            logger.info("HashMap of Rules:");
            for (String str : hashMap.keySet()) {
                logger.info(str + ":");
                for (Token t : hashMap.get(str)) {
                    logger.info(t.getValue());
                }
            }
        }
        return hashMap;
    }
    /*
     * Creates a map relating where each nonterminal is and the rules they exist in.
     * This is a helper function that for each terminal we can do a quick
     * O(1) lookup for its rules.
     * O(n log n)
     */
    public static HashMap<Character, ArrayList<String>> NonTermMap(String inputString, HashMap<String, ArrayList<Token>> hashMap) {
        /*
         * Input string for CYK algorithm.
         */
        HashMap<Character, ArrayList<String>> nonTermMap = new HashMap<Character, ArrayList<String>>();
        if (inputString.equals("\n")) {
            Lexer.LogOut(logger, "Input string must not be null.", "Input String must not be null.");
        }

        /*
         * Creates another map for terminals and all the rules that contain that non-terminal.
         */
        inputString.chars()
                .mapToObj(c -> (char) c)
                .distinct()
                .forEach(tempChar -> {
                    if (nonTermMap.containsKey(tempChar)) {
                        return;
                    }
                    ArrayList<String> temp = hashMap.entrySet().stream()
                            .flatMap(entry -> entry.getValue().stream()
                                    .filter(token -> token.getTokenType().equals(Token.TokenType.TERMINAL) &&
                                            token.getValue().charAt(0) == tempChar)
                                    .map(token -> entry.getKey()))
                            .collect(Collectors.toCollection(ArrayList::new));
                    nonTermMap.put(tempChar, temp);
                });

        return nonTermMap;
    }

    //Lambda function to collect and create a map of all the strings. 
    public static ArrayList<String> cartesian(ArrayList<String> arr1, ArrayList<String> arr2) {
        // Returns the cartesian product of two arraylists.
        return arr1.stream()
                .flatMap(str1 -> arr2.stream().map(str2 -> str1 + str2))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    //Ensures that every terminal in the input string is contained within the map. 
    //if there's no terminal from the inputstring corresponding to the terminals in the nonTermMap then we have an invalid inputString.
    public static boolean terminalNotExistsFromInput(String inpuString, HashMap<Character, ArrayList<String>> nonTermMap) {
        // Checks if the input string contains a terminal that is not in the grammar.
        for (char c : inpuString.toCharArray())
            if (!nonTermMap.containsKey(c)) return true;
        return false;
    }
}

