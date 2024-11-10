package priv.ethan.milvus.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlibabaJavaText {

    private String firstHeadline;

    private String secondHeadline;

    private String text = "";

    public boolean isNotBlank() {
        return StringUtils.isNotBlank(text);
    }

    public void clear() {
        this.text = "";
    }

    public void append(String line) {
        this.text += line;
    }

    public AlibabaJavaText clone() {
        AlibabaJavaText clone = new AlibabaJavaText();
        clone.setFirstHeadline(firstHeadline);
        clone.setSecondHeadline(secondHeadline);
        clone.setText(text);
        return clone;
    }

    @Override
    public String toString() {
        return String.format("%s-%s-%s", firstHeadline, secondHeadline, text);
    }
}
