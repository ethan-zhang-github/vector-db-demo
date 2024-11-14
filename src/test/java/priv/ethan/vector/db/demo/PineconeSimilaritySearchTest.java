package priv.ethan.vector.db.demo;

import com.google.gson.Gson;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.junit.Test;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.inference.client.ApiException;
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
    public void upsertVectors() throws ApiException, IOException {
        List<AlibabaJavaText> texts = ParseHelper.parseAlibabaJavaTexts();
        List<String> inputs = texts.stream().map(AlibabaJavaText::getText).collect(Collectors.toList());
        // embedding
        List<Embedding> embeddings = PineconeHelper.embedding(inputs, "passage");
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
            PineconeHelper.getIndex(indexName)
                .upsert(String.format("vector-%s", i), vector, null, null, metaData, null);
        }
    }
}
