package priv.ethan.milvus.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlibabaJavaText {

    private String firstHeadline;

    private String secondHeadline;

    private List<String> lines = new ArrayList<>();

    public boolean isNotBlank() {
        return CollectionUtils.isNotEmpty(lines);
    }

    public void clear() {
        this.lines.clear();
    }

    public void append(String line) {
        this.lines.add(line);
    }

    public AlibabaJavaText clone() {
        AlibabaJavaText clone = new AlibabaJavaText();
        clone.setFirstHeadline(firstHeadline);
        clone.setSecondHeadline(secondHeadline);
        clone.setLines(new ArrayList<>(lines));
        return clone;
    }

    public String getText() {
        return String.join("；", lines);
    }

    public String format() {
        return String.join("\n", lines);
    }
}
