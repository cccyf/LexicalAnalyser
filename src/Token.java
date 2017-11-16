public class Token {
    String label = "";
    TokenType type = TokenType.ERROR;
    String errorInfo = "";

    public Token(String label, TokenType tokenType) {
        this.label = label;
        this.type = tokenType;
    }

    public Token(String label, TokenType tokenType, String errorInfo) {
        this.label = label;
        this.type = tokenType;
        this.errorInfo = errorInfo;
    }

    public String toString() {
        if (errorInfo == null || errorInfo.length() == 0) {
            return "< " + this.label + " , " + this.type.toString() + " >";
        }
        return "< " + this.label + " , " + this.type.toString() + " , " + errorInfo + " >";
    }

}
