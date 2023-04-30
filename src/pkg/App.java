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
    
    public static void main(String[] args) throws Exception {
        /*
         * Logging Handler if logging is enabled
         */
        LocalDateTime startTime = LocalDateTime.now();
        if (LogSwitch) {
            String projectDir = System.getProperty("user.dir");
            String logFilePath = projectDir + "/tests/Log.txt";
            FileHandler fh = new FileHandler(logFilePath);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        }
       
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
     * This is super annoying implementation, but it WORKS!
     * You initalize the first row. Then call
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


    public static String printCYKMAP(ArrayList<ArrayList<ArrayList<String>>> CYKMap) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Add start time and date stamp
        sb.append("CYKMap printed on: ").append(LocalDateTime.now().format(formatter)).append("\n");

        // Print the top row
        sb.append("|   ");
        for (int i = 1; i < CYKMap.size(); i++) {
            sb.append(String.format("| %d   ", i));
        }
        sb.append("|\n");

        // Print the separator
        sb.append("|---");
        for (int i = 0; i < CYKMap.size(); i++) {
            sb.append("|-----");
        }
        sb.append("|\n");

        // Print the main table
        for (int i = 0; i <= CYKMap.size()-1; i++) {
            sb.append(String.format("| %d ", i));
            for (int j = 0; j < CYKMap.size(); j++) {
                if (i < j) {
                    sb.append("|     ");
                } else {
                    sb.append("|");
                    ArrayList<String> cell = CYKMap.get(i).get(j);
                    if (cell.size() == 0) {
                        sb.append("  0  ");
                    } else {
                        sb.append(String.format("%-5s", String.join(",", cell)));
                    }
                }
            }
            sb.append("|\n");
        }

        // Print the bottom separator
        sb.append("|---");
        for (int i = 0; i < CYKMap.size(); i++) {
            sb.append("|-----");
        }
        sb.append("|\n");

        // Add end time and date stamp
        sb.append("CYKMap print ended on: ").append(LocalDateTime.now().format(formatter)).append("\n");

        // Send the output to the logger
        logger.info(sb.toString());

        return sb.toString();
    } 

    public static ArrayList<ArrayList<ArrayList<String>>> CYKSquare(int i, int j, ArrayList<ArrayList<ArrayList<String>>> CYKMap, HashMap<String, ArrayList<Token>> Ruleset) {
        ArrayList<String> UnionedRules = new ArrayList<>();
        int l = i;
        int log = i + 1;
        StringBuilder cartesianLog = new StringBuilder("Cartesian of X_" + (l + 1) + "," + (i + 1) + " and X_" + (j + 1) + "," + (log + 1) + ": ");
        for (int m = i + 1; m <= j; m++) {
            ArrayList<String> CartesianedRules = cartesian(CYKMap.get(l).get(i), CYKMap.get(j).get(m));

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
        if (loggerRuleExists.length() == logString) {
            loggerRuleExists.append("NULL");
        } else {
            loggerRuleExists = new StringBuilder(loggerRuleExists.substring(0, loggerRuleExists.length() - 2));
        }
        logger.info(loggerRuleExists.toString());
        logger.info("Adding these to location: X_" + (i + 1) + "," + (j + 1));
        return CYKMap;
    }


    public static ArrayList<Token> Lex() throws IOException {
        /*
         * Start of CYK program
         */

        /*
         * Collect the strings from the input file.
         */
        String projectDir = System.getProperty("user.dir");
        String grammarFilePath = "/tests/grammar.txt";
        grammarFilePath = grammarFilePath.replace("\\", "/");
        grammarFilePath = projectDir + grammarFilePath;

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
         * Lexes the strings input from the file.
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

    public static HashMap<String, ArrayList<Token>> parse(ArrayList<Token> TokenList) throws Exception {
        //Parses each line into a hashmap of rules.
        Parser parser = new Parser(TokenList, logger);
        HashMap<String, ArrayList<Token>> hashMap = parser.parse();
        /*
         * Checking that for each ruleset there exists a correpsonding key rule.
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


    public static ArrayList<String> cartesian(ArrayList<String> arr1, ArrayList<String> arr2) {
        // Returns the cartesian product of two arraylists.
        return arr1.stream()
                .flatMap(str1 -> arr2.stream().map(str2 -> str1 + str2))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    public static boolean terminalNotExistsFromInput(String inpuString, HashMap<Character, ArrayList<String>> nonTermMap) {
        // Checks if the input string contains a terminal that is not in the grammar.
        for (char c : inpuString.toCharArray())
            if (!nonTermMap.containsKey(c)) return true;
        return false;
    }
}

