package priv.ethan.vector.db.demo;

import com.google.gson.Gson;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.junit.Test;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.inference.client.model.Embedding;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class PineconeSimilaritySearchTest {

    private final Gson gson = new Gson();
    private final String indexName = "example-index-2";

    @Test
    public void createIndex() {
        IndexModel indexModel = PineconeHelper.createIndex(indexName);
        System.out.println(gson.toJson(indexModel));
    }

    @Test
    public void upsertVectors() throws IOException {
        List<AlibabaJavaText> texts = ParseHelper.parseAlibabaJavaTexts();
        List<String> inputs = texts.stream().map(AlibabaJavaText::getText).collect(Collectors.toList());
        // embedding
        List<Embedding> embeddings = RetryHelper.retryInMemory(100, 5,
            () -> PineconeHelper.embedding(inputs, "passage"));
        // insert vectors
        for (int i = 0; i < texts.size(); i++) {
            AlibabaJavaText text = texts.get(i);
            Embedding embedding = embeddings.get(i);
            List<Float> vector = embedding.getValues().stream().map(BigDecimal::floatValue)
                .collect(Collectors.toList());
            Struct metaData = Struct.newBuilder()
                .putFields("h1", Value.newBuilder().setStringValue(text.getH1()).build())
                .putFields("h2", Value.newBuilder().setStringValue(text.getH2()).build())
                .putFields("text", Value.newBuilder().setStringValue(text.format()).build())
                .build();
            String vectorId = String.format("vector-%s", i);
            RetryHelper.retryInMemory(100, 5, () -> PineconeHelper.getIndex(indexName)
                .upsert(vectorId, vector, null, null, metaData, null));
        }
    }

    @Test
    public void similaritySearch() {
        // embedding
        Embedding embedding = RetryHelper.retryInMemory(100, 5,
            () -> PineconeHelper.embedding("创建线程池应该注意哪些问题？", "query"));
        List<Float> vector = embedding.getValues().stream().map(BigDecimal::floatValue).collect(Collectors.toList());
        QueryResponseWithUnsignedIndices response = RetryHelper.retryInMemory(100, 5,
            () -> PineconeHelper.similaritySearch(indexName, 5, vector));
        response.getMatchesList().forEach(
            match -> System.out.printf(
                "===================================================\nid: %s\nscore: %s\nh1:%s\nh2:%s\ntext: %s\nvector: %s\n",
                match.getId(), match.getScore(),
                match.getMetadata().getFieldsOrThrow("h1").getStringValue(),
                match.getMetadata().getFieldsOrThrow("h2").getStringValue(),
                match.getMetadata().getFieldsOrThrow("text").getStringValue(),
                match.getValuesList()));
    }

    @Test
    public void deleteAll() {
        PineconeHelper.getIndex(indexName).deleteAll(null);
    }
}
