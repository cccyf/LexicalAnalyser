import java.util.List;

public class Main {

    public static void main(String[] args) {
        String inputFile = "input.txt";
        char[] input = FileHelper.readFile(inputFile);
        Analyser analyser = new Analyser(input);
        List<Token> tokens = analyser.analyse();
        String out = "";
        for (Token t : tokens) {
            out += t.toString();
            out += "\n";
        }
        String outFile = "out.txt";
        FileHelper.writeFile(outFile, out);
        System.out.println(out);
    }
}
