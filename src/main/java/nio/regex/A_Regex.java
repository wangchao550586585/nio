package nio.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class A_Regex {
    public static void main(String[] args) {
        String input = "str";
        Pattern pattern = Pattern.compile("ABC|XYZ");
        Matcher matcher = pattern.matcher(input);
        String match0 = input.subSequence(matcher.start(), matcher.end()).toString();
        String match2 = input.subSequence(matcher.start(2), matcher.end(2)).toString();
        //上述代码与下列代码等效：
        match0 = matcher.group();
        match2 = matcher.group(2);
        //$1表示匹配的第一个
        //192

    }

}
