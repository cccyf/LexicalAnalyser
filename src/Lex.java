import java.util.Arrays;

public class Lex {
    private static String[] reservedWords = {
            "public", "protected", "private",
            "class", "interface", "abstract", "implements", "extends", "new",
            "import", "package",
            "byte", "char", "boolean", "short", "int", "float", "long", "double",
            "void", "null", "true", "false",
            "if", "else", "while", "for", "switch", "case", "default", "do", "break", "continue", "return",
            "static", "final", "super", "this", "synchronized", "transient", "volatile",
            "catch", "try", "finally", "throw", "throws", "enum", "assert"
    };

    private static String[] annotations = {"/*","//","*/"};

    private static String[] operator = {
            "+","++","-","--","*","/","=",">","<",">=","<=","+=","-=","*=","/=","==","!","!=","%","%=","&","&&","|","||","."
    };

    private static String[] delimiter = { ",",";",":","(",")","[","]","{","}","."};

    public static boolean isReservedWord(String s){
        return Arrays.asList(reservedWords).contains(s);
    }

}
