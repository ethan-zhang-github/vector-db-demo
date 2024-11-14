package priv.ethan.vector.db.demo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseHelper {

    public static List<AlibabaJavaText> parseAlibabaJavaTexts() throws IOException {
        List<String> lines = FileUtils.readLines(new File("docs/alibaba_java.txt"), StandardCharsets.UTF_8);
        List<AlibabaJavaText> texts = new ArrayList<>();
        Iterator<String> iterator = lines.iterator();
        AlibabaJavaText text = new AlibabaJavaText();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (isFirstHeadline(line)) {
                if (text.isNotBlank()) {
                    texts.add(text.clone());
                    text.clear();
                }
                text.setH1(line);
                continue;
            }
            if (isSecondHeadline(line)) {
                if (text.isNotBlank()) {
                    texts.add(text.clone());
                    text.clear();
                }
                text.setH2(line);
                continue;
            }
            if (isThirdHeadline(line)) {
                if (text.isNotBlank()) {
                    texts.add(text.clone());
                    text.clear();
                }
                text.append(line);
                continue;
            }
            text.append(line);
        }
        if (text.isNotBlank()) {
            texts.add(text.clone());
        }
        return texts;
    }

    private static boolean isFirstHeadline(String line) {
        Pattern pattern = Pattern.compile("^[零一二三四五六七八九]+、");
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    private static boolean isSecondHeadline(String line) {
        Pattern pattern = Pattern.compile("^\\([零一二三四五六七八九]+\\)");
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    private static boolean isThirdHeadline(String line) {
        Pattern pattern = Pattern.compile("^\\d+\\.");
        Matcher matcher = pattern.matcher(line);
        return matcher.find() && line.contains("【");
    }

}
