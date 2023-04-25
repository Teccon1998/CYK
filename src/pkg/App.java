package pkg;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class App {
    public static void main(String args[]) throws Exception
    {
        ArrayList<String> UnlexedStrings = new ArrayList<>();
        Path path = Paths.get("C://Users//alexa//OneDrive//Desktop//CYK Algorithm//src//pkg//grammar.txt");
        UnlexedStrings.addAll(Files.readAllLines(path));

        Lexer lexer = new Lexer(UnlexedStrings);

        ArrayList<Token> TokenList = new ArrayList<>();
        try {
            TokenList.addAll(lexer.Lex());
        } catch (Exception e) {
            System.exit(1);
        }
        System.out.println(TokenList);

        Parser parser = new Parser(TokenList);
    }
}
