#### 一、实验目的

构造自定义程序语言的词法分析器，进而更好的理解词法分析原理。

#### 二、内容概述

该程序由编写语言为java，实现平台为OSX。

程序读取根目录下的input.txt文件并对其中代码进行词法分析，识别内容输出到根目录下的out.txt中。

程序可识别保留字、标志符、操作符、分隔符、注释、整数、浮点数、字符串、字符，并能够对浮点数点数超过1、字符长度超过1、注释符号不全的错误进行识别。

程序能够输出格式为<识别单词符号，类型码（，错误信息）>的Token序列。其中类型码为ERROR时显示错误信息。

#### 三、实现思路

1）针对要识别的单词符号写出正则表达式

2）构造出每个正则表达式对应的NFA

3）合并所有NFA，并化简为DFA

4）基于DFA编写代码

5）代码思路：先读入一个字符，根据字符类型得到其所属的类别，并继续进行判断。具体类型根据后面的字符进行判断，到达可识别的位置就添加到token列表中。

#### 四、实验假设

1. 假设保留字的优先级高于标志符
2. 假设+=、-=这类运算符优先级高于+、-
3. 假设输入的数字范围是有效的
4. 假设输入的字符不考虑转义符

#### 五、记号定义

**a.保留字 RESERVEDWORD**

```
"public", "protected", "private",
"class", "interface", "abstract", "implements", "extends", "new",
"import", "package",
"byte", "char", "boolean", "short", "int", "float", "long", "double",
"void", "null", "true", "false",
"if", "else", "while", "for", "switch", "case", "default", "do", "break", "continue", "return",
"static", "final", "super", "this", "synchronized", "transient", "volatile",
"catch", "try", "finally", "throw", "throws", "enum", "assert"
```

输出示例：`< public , RESERVEDWORD >`

**b.操作符 OPERATOR**

```
 "+","-","*","/","=",">","<","!","%",
 "++","--",">=","<=","+=","-=","*=","/=","==","!=","%=",
 "&","&&","|","||",
 "."
```

输出示例：`< = , OPERATOR >`

**c.分隔符 DELIMITER**

```
",",";",":","(",")","[","]","{","}","."
```

输出示例：`< ; , DELIMITER >`

**d.注释 ANNOTATION**

```
/* 
 * annotations 
 */
 
//annotation 
```

输出示例（实际注释不输出）：

`< // annotations , ANNOTATION >`

`< /* annotations */ , ERROR , Error : lacking annotation symbols >`

*注：以上错误为缺少多行注释的结束符*

**e.标示符 ID**

输出示例：`< a , ID >`

**f.整数 NUMBER**

输出示例：`< 2 , NUMBER >`

**g.浮点数 FLOAT**

输出示例：

`< 50.0 , FLOAT >`

`< 2.2. , ERROR , Error : The float has more than 1 point  >`

*注：以上错误为浮点数输入错误*

**h.字符 CHAR**

输出示例：

`< a , CHAR >`

`< char , ERROR , Error : length of char is more than 1 >`

*注1：以上错误为char长度输入超过1*

*注2：字符识别 不考虑需要转义的字符*

**i.字符串 STRING**

输出示例：`< abc , STRING >`

*注：此处字符串识别不考虑多行字符串及需要转义的字符。*

#### 六、正则表达式

1.数字

`NUM  -> digit （digit）*`

`digit -> 0|1|2|3|4|5|6|7|8|9`

2.浮点数

`FLOAT ->digit (digit)*.(digit)*`

注：此处在java中尝试了 “1.”是可以被识别为浮点数的。

3.标志符

`ID -> letter (letter|digit)*`

4.保留字

`ReservedWord -> public | protected | private  | class | interface | abstract | implements | extends | new  | import | package  | byte | char | boolean | short | int | float | long | double  | void | null | true | false  | if | else | while | for | switch | case | default | do | break | continue | return  | static | final | super | this | synchronized | transient | volatile  | catch | try | finally | throw | throws | enum | assert` 

5.操作符

`operator-> + | - | * | / | = | > | < | ! | % |  | ++ | -- | >= | <= | += | -= | *= | /= | == | != | %= | & | && | | | || | . | `

6.分隔符

`delimiter ->  , | ; | : | ( | ) | [ | ] | { | } | . | |` 

7.注释(@表示注释的*)

`annotation->  (/@)(~[/|@])*@(~[/])*/(~[@])*(@/)`

8.字符

`character -> ' ~[symbol] '` 

*注：其中symbol表示所有需要转义的字符*

9.字符串

`string -> "~[symbol]*"`

*注：其中symbol表示所有需要转义的字符*

#### 七、重要的数据结构

![屏幕快照 2017-11-04 上午9.42.35](/Users/chengyunfei/Desktop/屏幕快照 2017-11-04 上午9.42.35.png)

![屏幕快照 2017-11-04 上午9.55.05](/Users/chengyunfei/Desktop/屏幕快照 2017-11-04 上午9.55.05.png)

![屏幕快照 2017-11-04 上午9.54.55](/Users/chengyunfei/Desktop/屏幕快照 2017-11-04 上午9.54.55.png)

#### 八、实现算法

**该实验共有6个类：**

Main：主要控制类，流程为从FileHelper中读取文件，调用Analyser进行分析，输出再调用FileHelper。

FileHelper：文件的输入输出。

Lex：词法的定义。

Token：输出序列的定义。

TokenType：输出序列类型的定义。

Analyser：负责分析的核心算法。每次读取一个字符，如果是数字则读取之后的字符并根据后续数字判断浮点数还是数字，当读到非.和数字时结束读取新建token放入list；如果是字母则读取后续字符，如果是数字或字母则一直读取，直到出现非数字且非字母时读取结束新建token放入list；如果是符号则分情况判断，对+和-都包含+、++、+=或-、++、-=各自三种情况，对/则需要判断是除号还是注释，* % < > = ! 情况一致都各有两种情况即“@”或“@=”（@代指以上各个符号），对（）[]{}.,;:则各只有一种一种情况，对“和‘识别判断字符串／字符，另外对除空格和回车之外字符进行异常处理。

#### 九、异常处理

1. 在识别浮点数时，对识别多于1位的「.」进行错误提示。返回结果如下：

   `< 2.2. , ERROR , Error : The float has more than 1 point  >`

   此处的异常处理为继续读取第二个「.」之后的文件输入。

2. 在识别注释时，对多行注释(/* annotation*/)判断是否存在结束符(\*/)，若不存在返回错误信息，结果如下：

   `< /* annotations */ , ERROR , Error : lacking annotation symbols >` 

   此处的异常处理为在注释开始符之后一直到读取到结束符之前都不进行类型识别，一直读取到文件末尾。如果仍未发现结束符则读取结束。

3. 在识别单字符时，如果char字符长度大于1(不包含转义字符的识别)，则返回错误信息，结果如下：

   `< char , ERROR , Error : length of char is more than 1 >`

   此处的异常处理为读取完该行。即在第一个‘之后发现第2个字符非结束符时则对该行后续代码不进行识别，直接从下一行开始处理。如果该行为文件末尾则结束读取直接返回。

4. 对于代码中注释之外的地方如果存在无法识别的字符，譬如‘@’等则返回错误信息，结果如下：

   `< Cannot Identify , ERROR , Error : This symbol @ cannot be identified  >`

   此处的异常处理为从下一个字符继续处理。

#### 十、测试代码及输出

输入：

```java
public class Test {
    /*
     * main method
     */
    public static void main() {
        Test t = new Test();
        double a = 50.0;
        int b = 2;
        //should discover an error
        char c = 'aa';
        double result = t.power(a, b);
    }

    //calculate a ^ b
    private double power(double a, int b) {
        //should discover an error
        double result = 2.2.;
        for (int i = 0; i < b; i++) {
            result *= a;
        }
        return result;
    }

//should discover an error
@@

}
//should discover an error
/*
```

输出：

![屏幕快照 2017-11-04 上午9.49.48](/Users/chengyunfei/Desktop/屏幕快照 2017-11-04 上午9.49.48.png)

![屏幕快照 2017-11-04 上午9.49.48](/Users/chengyunfei/Desktop/屏幕快照 2017-11-04 上午9.49.59.png)

![屏幕快照 2017-11-04 上午9.49.48](/Users/chengyunfei/Desktop/屏幕快照 2017-11-04 上午9.50.08.png)

![屏幕快照 2017-11-04 上午9.49.48](/Users/chengyunfei/Desktop/屏幕快照 2017-11-04 上午9.50.17.png)



#### 十一、遇到的问题

实验最开始我写了两个方法判断运算符。第一个方法是判断是否属于长度为一的运算符，譬如“+，-，*，\，=，&，|”，第二个方法是判断是否属于长度为二的运算符，如“+=，-=，&&，||”等。然后最开始的思路是读到一个字符，就判断一下是否属于长度为一的运算符，如果属于则再读取下一位字符，判断是否属于长度为二的运算符；如果是则存长度为二的运算符，否则就退一格只存长度为一的运算符。这样在判断运算符的时候就十分简单 。但是后来思考了一下觉得似乎不是很合理。因为按照词法分析的过程来讲，对+、-这一类需要读取下一位判断是否为=，但对于=这一类运算符实际上不需要读取下一位进行判断，不是很符合词法分析的过程；而且按我这样的分析方法来讲，把对+之后的=判断和对&之后的&判断混在一起，也不是很符合词法分析的过程。因此最后觉得不是很合理，就改回了switch分析的方法。

另一点就是进行异常处理。我觉得我最开始在考虑异常处理的时候总是把词法分析和语法分析弄混... 譬如else之前要有if这点要不要加进去，譬如一些固定句型。后来想着想着突然发现其实这些应该算是语法分析...不过后来想通了感觉思路就清晰很多。

#### 十二、感受

我觉得通过这样一个实验对词法分析的过程有了更深层次的理解。尤其在优先级方面，在写代码时有了更深的理解。其实整个写代码的过程慢慢思路清晰，感觉还是很好的。另外就是在考虑异常处理的时候感觉还是蛮有趣的。还有一个有趣的地方就是发现了java好多之前没注意到的语法...譬如原来浮点数可以写「  `2.`  」这样的格式...还有+=、-=一定要连写不能空格，感觉这大概也是java的词法分析机制吧。