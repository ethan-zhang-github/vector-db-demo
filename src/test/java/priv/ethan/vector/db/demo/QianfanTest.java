package priv.ethan.vector.db.demo;

import com.baidubce.qianfan.core.StreamIterator;
import com.baidubce.qianfan.model.chat.ChatResponse;
import com.baidubce.qianfan.model.completion.CompletionResponse;
import com.baidubce.qianfan.model.embedding.EmbeddingResponse;
import com.baidubce.qianfan.model.image.Image2TextResponse;
import com.baidubce.qianfan.model.image.Text2ImageResponse;
import com.baidubce.qianfan.model.rerank.RerankResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QianfanTest {

    @Test
    public void chatCompletion() {
        ChatResponse response = QianfanClientHolder.getClient().chatCompletion()
            .model("ERNIE-4.0-8K") // 使用model指定预置模型
            // .endpoint("completions_pro") // 也可以使用endpoint指定任意模型 (二选一)
            .addMessage("user", "你好") // 添加用户消息 (此方法可以调用多次，以实现多轮对话的消息传递)
            .temperature(0.7) // 自定义超参数
            .execute(); // 发起请求
        System.out.println(response.getResult());
    }

    @Test
    public void chatCompletionStream() {
        try (StreamIterator<ChatResponse> response = QianfanClientHolder.getClient().chatCompletion()
            .model("ERNIE-4.0-8K")
            .addMessage("user", "你好")
            .executeStream()) {
            while (response.hasNext()) {
                response.forEachRemaining(chunk -> System.out.print(chunk.getResult()));
            }
        }
    }

    @Test
    public void completion() {
        try (StreamIterator<CompletionResponse> response = QianfanClientHolder.getClient().completion()
            .model("CodeLlama-7b-Instruct")
            .prompt("hello")
            .executeStream()) {
            while (response.hasNext()) {
                System.out.print(response.next().getResult());
            }
        }
    }

    @Test
    public void embedding() {
        List<String> inputs = new ArrayList<>();
        inputs.add("今天的晚饭好吃吗");
        inputs.add("今日晚餐味道咋样");

        EmbeddingResponse response = QianfanClientHolder.getClient().embedding()
            // 指定embedding模型
            .model("Embedding-V1")
            .input(inputs)
            .execute();
        response.getData().forEach(data -> {
            System.out.println(inputs.get(data.getIndex()));
            System.out.println(data.getEmbedding());
        });
    }

    @Test
    public void text2Image() throws IOException {
        Text2ImageResponse response = QianfanClientHolder.getClient().text2Image()
            .prompt("cute dog")
            .execute();
        String base64Image = response.getData().get(0).getB64Image();
        System.out.println(base64Image);

        byte[] decodedBytes = Base64.decodeBase64(base64Image);
        Files.write(Paths.get(String.format("images/%s.jpg", Math.abs(RandomUtils.nextLong()))), decodedBytes);
    }

    @Test
    public void image2Text() throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get("images/632410270040155921.jpg"));
        String base64Image = Base64.encodeBase64String(imageBytes);
        Image2TextResponse response2 = QianfanClientHolder.getClient().image2Text()
            .image(base64Image)
            .prompt("introduce the picture")
            .execute();
        System.out.println(response2.getResult());
    }

    @Test
    public void reRank() {
        List<String> documents = new ArrayList<>();
        documents.add("上海位于中国东部海岸线的中心，长江三角洲最东部。");
        documents.add("上海现在的温度是27度。");
        documents.add("深圳现在的温度是29度。");

        RerankResponse response = QianfanClientHolder.getClient().rerank()
            .query("上海现在气温多少？")
            .documents(documents)
            .execute();
        response.getResults().forEach(data -> {
            System.out.println(data.getDocument());
            System.out.println(data.getRelevanceScore());
        });
    }

}
