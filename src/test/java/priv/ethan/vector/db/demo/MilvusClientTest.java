package priv.ethan.vector.db.demo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class MilvusClientTest {

    private final Gson gson = new Gson();
    private final String collectionName = "collection_test_1";
    private final int dimension = 5;

    @Test
    public void createCollection() {
        MilvusClientOperator.createCollection(collectionName, dimension);
    }

    @Test
    public void insertData() {
        List<JsonObject> insertData = new ArrayList<>();
        List<String> colors = Arrays.asList("green", "blue", "yellow", "red", "black", "white", "purple", "pink",
            "orange", "brown", "grey");
        IntStream.range(10, 1000).forEach(i -> {
            Random rand = new Random();
            JsonObject row = new JsonObject();
            row.addProperty("id", (long) i);
            JsonArray vector = new JsonArray();
            IntStream.generate(() -> 0).limit(dimension).forEach(z -> {
                vector.add(rand.nextFloat());
            });
            row.add("vector", vector);
            row.addProperty("color", colors.get(rand.nextInt(colors.size() - 1)) + '_' + rand.nextInt(1000));
            insertData.add(row);
        });
        InsertReq insertReq = InsertReq.builder()
            .collectionName(collectionName)
            .data(insertData)
            .build();

        InsertResp rst = MilvusClientHolder.getClient().insert(insertReq);

        System.out.println(gson.toJson(rst));

    }

    @Test
    public void singleVectorSearch() {
        List<BaseVector> singleVectorSearchData = new ArrayList<>();
        singleVectorSearchData.add(
            new FloatVec(new float[]{0.041732933f, 0.013779674f, -0.027564144f, -0.013061441f, 0.009748648f}));
        SearchReq searchReq = SearchReq.builder()
            .collectionName(collectionName)
            .data(singleVectorSearchData)
            .topK(3)
            .build();
        SearchResp singleVectorSearchRes = MilvusClientHolder.getClient().search(searchReq);
        System.out.println(gson.toJson(singleVectorSearchRes));
    }

    @Test
    public void bulkVectorSearch() {
        List<BaseVector> multiVectorSearchData = new ArrayList<>();
        multiVectorSearchData.add(
            new FloatVec(new float[]{0.041732933f, 0.013779674f, -0.027564144f, -0.013061441f, 0.009748648f}));
        multiVectorSearchData.add(
            new FloatVec(new float[]{0.0039737443f, 0.003020432f, -0.0006188639f, 0.03913546f, -0.00089768134f}));
        SearchReq searchReq = SearchReq.builder()
            .collectionName(collectionName)
            .data(multiVectorSearchData)
            .topK(3)
            .outputFields(Arrays.asList("color"))
            .build();
        SearchResp multiVectorSearchRes = MilvusClientHolder.getClient().search(searchReq);
        System.out.println(gson.toJson(multiVectorSearchRes));
    }

    @Test
    public void getEntities() {
        GetReq getReq = GetReq.builder()
            .collectionName(collectionName)
            .ids(Arrays.asList(100L, 101L, 102L))
            .build();
        GetResp getRes = MilvusClientHolder.getClient().get(getReq);
        System.out.println(gson.toJson(getRes));
    }

}
