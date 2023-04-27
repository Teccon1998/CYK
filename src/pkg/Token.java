package pkg;

public class Token {
    private final TokenType TokType;
    private final String str;

    public Token(TokenType tokenType, String str) {
        this.TokType = tokenType;
        this.str = str;
    }

    public TokenType getTokenType() {
        return this.TokType;
    }

    public String getValue() {
        return this.str;
    }

    @Override
    public String toString() {
        return "Token(T:" + this.TokType + ", String:" + this.str + ")";
    }

    public enum TokenType {
        TERMINAL, NONTERMINAL, EPSILON, ENDOFLINE, SEPARATOR, RULERELATION, INPUTSTRING
    }
}
