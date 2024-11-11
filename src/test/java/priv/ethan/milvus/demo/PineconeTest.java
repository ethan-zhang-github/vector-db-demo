package priv.ethan.milvus.demo;

import com.google.gson.Gson;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.Embedding;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PineconeTest {

    private final Gson gson = new Gson();
    private final String indexName = "example-index-1";

    @Test
    public void embedding() throws ApiException {
        List<DataObject> data = getData();
        List<Embedding> embeddings = PineconeHelper.embedding(
            data.stream().map(DataObject::getText).collect(Collectors.toList()), "passage");
        System.out.println(gson.toJson(embeddings));
    }

    @Test
    public void createIndex() {
        IndexModel indexModel = PineconeClientHolder.getPc()
            .createServerlessIndex(indexName, "cosine", 1024, "aws", "us-east-1", DeletionProtection.DISABLED);
        System.out.println(gson.toJson(indexModel));
    }

    @Test
    public void upsertVectors() throws ApiException {
        List<DataObject> data = getData();
        List<Embedding> embeddings = PineconeHelper.embedding(
            data.stream().map(DataObject::getText).collect(Collectors.toList()), "passage");

        // Prepare and upsert the records into the index
        // Each contains an 'id', the embedding 'values', and the original text as 'metadata'
        for (int i = 0; i < data.size(); i++) {
            String id = data.get(i).getId();
            List<Float> vector = convert(embeddings.get(i).getValues());
            Struct metaData = Struct.newBuilder()
                .putFields("text", Value.newBuilder().setStringValue(data.get(i).getText()).build())
                .build();
            getIndex().upsert(id, vector, null, null, metaData, null);
        }
    }

    @Test
    public void similaritySearch() throws ApiException {
        Embedding embedding = PineconeHelper.embedding("Tell me about the tech company known as Apple.", "query");
        List<Float> vector = convert(embedding.getValues());
        // Search the index for the three most similar vectors
        QueryResponseWithUnsignedIndices queryResponse = getIndex().query(3,
            vector, null, null, null, null, null, true,
            true);
        queryResponse.getMatchesList().forEach(
            match -> System.out.printf("id: %s, score: %s, metadata: %s, vector: %s\n", match.getId(), match.getScore(),
                match.getMetadata().getFieldsOrThrow("text").getStringValue(), match.getValuesList()));
    }

    @Test
    public void deleteAll() {
        getIndex().deleteAll(null);
    }

    private Index getIndex() {
        // Target the index where you'll store the vector embeddings
        return PineconeClientHolder.getPc().getIndexConnection(indexName);
    }

    private List<DataObject> getData() {
        return Arrays.asList(
            new DataObject("vec1", "Apple is a popular fruit known for its sweetness and crisp texture."),
            new DataObject("vec2", "The tech company Apple is known for its innovative products like the iPhone."),
            new DataObject("vec3", "Many people enjoy eating apples as a healthy snack."),
            new DataObject("vec4",
                "Apple Inc. has revolutionized the tech industry with its sleek designs and user-friendly interfaces."),
            new DataObject("vec5", "An apple a day keeps the doctor away, as the saying goes."),
            new DataObject("vec6",
                "Apple Computer Company was founded on April 1, 1976, by Steve Jobs, Steve Wozniak, and Ronald Wayne as a partnership.")
        );
    }

    private List<Float> convert(List<BigDecimal> bigDecimalValues) {
        return bigDecimalValues.stream()
            .map(BigDecimal::floatValue)
            .collect(Collectors.toList());
    }

    @AllArgsConstructor
    @Data
    static class DataObject {

        private String id;
        private String text;
    }

}
