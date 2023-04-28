package pkg;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

import static pkg.App.StartRule;
import static pkg.App.getStartRule;
import static pkg.Token.TokenType.*;

public class Lexer {

    Logger logger;
    private ArrayList<String> UnlexedStrings = new ArrayList<>();

    public Lexer(ArrayList<String> UnlexedStrings, Logger logger) {
        this.UnlexedStrings = UnlexedStrings;
        this.logger = logger;
    }

    static void LogOut(Logger logger, String message, String exception) {
        logger.severe(message);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            throw new Exception(exception);
        } catch (Exception e) {
            e.printStackTrace(pw);
            logger.severe(sw.toString());
            System.exit(1);
        }
    }

    public ArrayList<Token> Lex() throws Exception {

        ArrayList<Token> TokenList = new ArrayList<>();
        //We need to loop over each string in the list
        // log start char
        logger.info("Starting Lexing");
        // start state
        logger.info( "Starting Rule: " + getStartRule());
        for (String str : this.UnlexedStrings) {
            /*
             * Need to loop over each character. Cases checking for every style of grammar input.
             * We delimit over pipe characters '|' not caring about anything specific.
             * That will be taken care of in the parser.
             */
            StringBuilder StringGrouping = new StringBuilder();
            State state = State.INPUTSTRING;
            if (TokenList.size() != 0 && TokenList.get(0).getTokenType().equals(INPUTSTRING)) {
                state = State.START;
            }
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                switch (state) {
                    case INPUTSTRING -> {
                        if (Character.isLowerCase(c) || c == ':') {
                            StringGrouping.append(c);
                        }
                        if (i + 1 == str.length()) {
                            TokenList.add(new Token(INPUTSTRING, StringGrouping.toString().split(":")[1]));
                            StringGrouping = new StringBuilder();
                            state = State.START;
                        }
                    }
                    case START -> {
                        if (Character.isUpperCase(c) || Character.isDigit(c)) {
                            StringGrouping.append(c);
                        } else if (Character.isWhitespace(c)) {
                            //Do nothing, we dont want to create a lexeme for nothing.
                        } else if (c == ':') {
                            /*
                             * Adds the nonterminal from the inital rule.
                             */
                            TokenList.add(new Token(NONTERMINAL, StringGrouping.toString()));
                            TokenList.add(new Token(RULERELATION, ":"));
                            StringGrouping = new StringBuilder();
                            state = State.RULESET;
                        } else {
                            LogOut(logger, "NO VALID RULE FOUND!!! EXITING!!!", "No valid rule found. Exiting. Start char: " +  App.getStartRule());
                        }
                    }
                    case RULESET -> {
                        if (Character.isUpperCase(c) || Character.isDigit(c)) {
                            StringGrouping.append(c);
                        } else if (Character.isLowerCase(c)) {
                            StringGrouping.append(c);
                        } else if (Character.isWhitespace(c)) {
                            //Do nothing, dont want to create lexemes
                        } else if (c == '|') {
                            /*
                             * Rule is detected we want to take whatever grouping is found.
                             * Create a rule based on the values in the stored string.
                             * Determines the token type based on case of the stored String.
                             */
                            if (StringGrouping.length() == 0) {
                                TokenList.add(new Token(RULERELATION, "|"));
                            } else if (Character.isLowerCase(StringGrouping.charAt(0))) {
                                TokenList.add(new Token(TERMINAL, StringGrouping.toString()));
                                StringGrouping = new StringBuilder();
                            } else if (Character.isUpperCase(StringGrouping.charAt(0)) || Character.isDigit(StringGrouping.charAt(0))) {
                                TokenList.add(new Token(NONTERMINAL, StringGrouping.toString()));
                                StringGrouping = new StringBuilder();
                            } else {
                                TokenList.add(new Token(RULERELATION, "|"));
                            }
                        }
                    }
                }

            }
            if (StringGrouping.length() != 0) {
                if (Character.isLowerCase(StringGrouping.charAt(0))) {
                    TokenList.add(new Token(TERMINAL, StringGrouping.toString()));
                    StringGrouping = new StringBuilder();
                } else if (Character.isUpperCase(StringGrouping.charAt(0)) || Character.isDigit(StringGrouping.charAt(0))) {
                    TokenList.add(new Token(NONTERMINAL, StringGrouping.toString()));
                    StringGrouping = new StringBuilder();
                }
                TokenList.add(new Token(ENDOFLINE, "EOL"));
            }
            for (int i = 0; i < TokenList.size(); i++) {
                if (TokenList.get(i).getValue().equalsIgnoreCase("epsilon")) {
                    TokenList.set(i, new Token(EPSILON, "EPSILON"));
                }
            }
        }
        return TokenList;
    }

    private enum State {RULESET, START, INPUTSTRING}
}
