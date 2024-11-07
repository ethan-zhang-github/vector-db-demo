package priv.ethan.milvus.demo;

import com.baidubce.qianfan.model.embedding.EmbeddingResponse;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class SimilaritySearchTest {

    private final Gson gson = new Gson();
    private final String collectionName = "collection_test_2";

    @Test
    public void createCollection() {
        // 2. Create a collection in quick setup mode
        CreateCollectionReq quickSetupReq = CreateCollectionReq.builder()
            .autoID(true)
            .collectionName(collectionName)
            .dimension(384) // The dimensionality should be an integer greater than 1.
            .build();
        MilvusClientHolder.getClient().createCollection(quickSetupReq);
    }

    @Test
    public void dropCollection() {
        DropCollectionReq dropQuickSetupParam = DropCollectionReq.builder()
            .collectionName(collectionName)
            .build();
        MilvusClientHolder.getClient().dropCollection(dropQuickSetupParam);
    }

    @Test
    public void embedding() throws IOException {
        List<String> texts = FileUtils.readLines(new File("docs/texts.txt"), StandardCharsets.UTF_8);
        Lists.partition(texts, 16).forEach(partition -> {
            // vector embedding
            EmbeddingResponse response = QianfanClientHolder.getClient().embedding()
                // embedding model
                .model("Embedding-V1")
                .input(partition)
                .execute();
            List<JsonObject> insertData = response.getData().stream().map(embedding -> {
                String text = partition.get(embedding.getIndex());
                List<BigDecimal> vector = embedding.getEmbedding();
                System.out.printf("%s => %s\n", text, vector);
                JsonObject row = new JsonObject();
                JsonArray vectorArray = new JsonArray();
                vector.forEach(vectorArray::add);
                row.add("vector", vectorArray);
                row.addProperty("text", text);
                return row;
            }).collect(Collectors.toList());
            // insert data
            InsertReq insertReq = InsertReq.builder()
                .collectionName(collectionName)
                .data(insertData)
                .build();
            InsertResp rst = MilvusClientHolder.getClient().insert(insertReq);
            System.out.printf("insert result: %s\n", gson.toJson(rst));
        });
    }

    @Test
    public void deleteEntities() {
        DeleteReq deleteReq = DeleteReq.builder()
            .collectionName(collectionName)
            .filter("id in [453466849448688693, 453466849448688694, 453466849448688695]")
            .build();
        DeleteResp deleteRes = MilvusClientHolder.getClient().delete(deleteReq);
        System.out.println(gson.toJson(deleteRes));
    }

}
