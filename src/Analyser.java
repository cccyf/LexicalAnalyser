import java.util.ArrayList;
import java.util.List;

public class Analyser {
    private char[] inputChars;

    public Analyser(char[] chars) {
        this.inputChars = chars;
    }

    public List<Token> analyse() {
        List<Token> tokenList = new ArrayList<>();
        int pointer = 0;
        char ch;

        while ((ch = inputChars[pointer++]) != '#') {
            /*
             * NUM
             */
            String current = "";
            if (Character.isDigit(ch)) {
                boolean isFloat = false;
                current += ch;
                while ((ch = inputChars[pointer]) != '#') {
                    /*
                      当输入一直是数字 加入current
                     */
                    if (Character.isDigit(ch)) {
                        current += ch;
                    } else if (ch == '.') {
                        //已经存在一个.的异常处理
                        if (isFloat) {
                            tokenList.add(new Token(current+ch, TokenType.ERROR,"Error : The float has more than 1 point "));
                            pointer++;
                            break;
//                       下一位不是数字的异常处理
//                       实际java可以这样写 所以去掉了
//                        } else if (!Character.isDigit(inputChars[pointer + 1])) {
//                            tokenList.add(new Token("Error : A wrong extra point ", TokenType.ERROR));
//                            break;
                        } else {
                            isFloat = true;
                            current += ch;
                        }
                    } else {
                        if (isFloat) {
                            tokenList.add(new Token(current, TokenType.FLOAT));
                        } else {
                            tokenList.add(new Token(current, TokenType.NUMBER));
                        }
                        break;
                    }
                    pointer++;
                }
                // ID 或者 RESERVEDWORD
            } else if (Character.isLetter(ch)) {
                current += ch;
                while ((ch = inputChars[pointer]) != '#') {
                    /*
                    当输入一直是数字/字母 加入current
                     */
                    if (Character.isDigit(ch) || Character.isLetter(ch)) {
                        current += ch;
                        pointer++;
                    } else {
                        //RESERVEDWORD
                        if (Lex.isReservedWord(current)) {
                            tokenList.add(new Token(current, TokenType.RESERVEDWORD));
                        } else {
                            //ID
                            tokenList.add(new Token(current, TokenType.ID));
                        }
                        break;
                    }
                }
            } else {
                //特殊符号情况
                char next;
                switch (ch) {
                    //+ - 都包含+、++、+=或-、++、-=各自三种情况
                    case '+':
                    case '-':
                        next = inputChars[pointer];
                        if (next == ch || next == '=') {
                            tokenList.add(new Token(ch + "" + next, TokenType.OPERATOR));
                            pointer++;
                        } else {
                            tokenList.add(new Token(ch + "", TokenType.OPERATOR));
                        }
                        break;
                    case '/':
                        next = inputChars[pointer];
                        //如果为多行注释
                        if (next == '*') {
                            pointer++;
                            //文件结束前要包含注释结束符号
                            while (!(inputChars[pointer] == '#' && inputChars[pointer + 1] == 0)) {
                                if (inputChars[pointer] == '*') {
                                    if (inputChars[pointer + 1] == '/') {
                                        tokenList.add(new Token("/* annotations */", TokenType.ANNOTATION));
                                        pointer += 2;
                                        break;
                                    }
                                }
                                pointer++;
                            }
                            //缺少注释结束的异常处理
                            if (inputChars[pointer] == '#' && inputChars[pointer + 1] == 0) {
                                tokenList.add(new Token("/* annotations */", TokenType.ERROR ,"Error : lacking annotation symbols"));
                            }
                            //如果是单行注释
                        } else if (next == '/') {
                            pointer++;
                            //找到换行符
                            while (inputChars[pointer] != '\n') {
                                //如果到达文件结束结束 直接退出
                                if (inputChars[pointer] == '#' && inputChars[pointer + 1] == 0) {
                                    break;
                                }
                                pointer++;
                            }
                            tokenList.add(new Token("// annotations", TokenType.ANNOTATION));
                            //如果是／= 运算符情况
                        } else if (next == '=') {
                            tokenList.add(new Token(ch + "" + next, TokenType.OPERATOR));
                            pointer++;
                            //单个／运算符情况
                        } else {
                            tokenList.add(new Token(ch + "", TokenType.OPERATOR));
                        }
                        break;
                    // * % < > = ! 情况一致 都各有两种情况
                    case '*':
                    case '%':
                    case '>':
                    case '<':
                    case '=':
                    case '!':
                        next = inputChars[pointer];
                        if (next == '=') {
                            tokenList.add(new Token(ch + "" + next, TokenType.OPERATOR));
                            pointer++;
                        } else {
                            tokenList.add(new Token(ch + "", TokenType.OPERATOR));
                        }
                        break;
                    // |与&情况一致 亦各有两种情况
                    case '|':
                    case '&':
                        next = inputChars[pointer];
                        if (next == ch) {
                            tokenList.add(new Token(ch + "" + next, TokenType.OPERATOR));
                            pointer++;
                        } else {
                            tokenList.add(new Token(ch + "", TokenType.OPERATOR));
                        }
                        break;
                    //以下几种一致
                    //注意 . 不包含浮点数判断，浮点数判断在number处已经进行判断
                    case '(':
                    case ')':
                    case '[':
                    case ']':
                    case '{':
                    case '}':
                    case '.':
                    case ',':
                    case ';':
                    case ':':
                        tokenList.add(new Token(ch + "", TokenType.DELIMITER));
                        break;
                    //字符串识别 不考虑 多行字符串 及 需要转义的字符
                    case '\"':
                        String str = "";
                        while (inputChars[pointer] != '\"') {
                            str += inputChars[pointer];
                            pointer++;
                        }
                        tokenList.add(new Token(str, TokenType.STRING));
                        pointer++;
                        break;
                    //字符识别 不考虑需要转义的字符
                    case '\'':
                        char c = inputChars[pointer];
                        if (inputChars[pointer + 1] == '\'') {
                            tokenList.add(new Token(c + "", TokenType.CHAR));
                        } else {
                            //异常情况 如果char字符长度大于1
                            //处理方法 如果发生该异常，则忽略该行异常之后的识别，从下一行开始继续识别
                            tokenList.add(new Token("char", TokenType.ERROR,"Error : length of char is more than 1"));
                            pointer++;
                            //寻找换行符 或者 到达文件末尾 则停止识别
                            while (inputChars[pointer] != '\n' && (!(inputChars[pointer] == '#'&&inputChars[pointer]==0))) {
                                pointer++;
                            }
                        }
                        break;
                    case ' ':
                    case '\n':
                        break;
                    default:
                        tokenList.add(new Token("Cannot Identify", TokenType.ERROR,"Error : This symbol "+ch+" cannot be identified "));
                        break;
                }
            }
            continue;
        }

        return tokenList;
    }
}
