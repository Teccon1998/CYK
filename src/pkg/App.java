package pkg;
import java.util.logging.*;
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
        Lexer lexer = new Lexer(UnlexedStrings);
        ArrayList<Token> TokenList = new ArrayList<>();
        try {
            TokenList.addAll(lexer.Lex());
        } catch (Exception e) {
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
        Parser parser = new Parser(TokenList);
        HashMap<String,ArrayList<Token>> hashMap = parser.parse();
        
        /*
         * Logs the hashmap of rules.
         */
        
        if(LogSwitch)
        {
            logger.info("HashMap of Rules:");
            for (String s : hashMap.keySet()) {
                logger.info(s+":");
                for(Token t : hashMap.get(s))
                {
                    logger.info(t.getValue());
                }
            }
        }
    }

    
}
