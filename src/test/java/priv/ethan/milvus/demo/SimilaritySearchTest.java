package priv.ethan.milvus.demo;

import com.baidubce.qianfan.model.embedding.EmbeddingData;
import com.baidubce.qianfan.model.embedding.EmbeddingResponse;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        List<AlibabaJavaText> texts = parseAlibabaJavaTexts();
        Lists.partition(texts, 16).forEach(partition -> {
            // vector embedding
            EmbeddingResponse response = QianfanClientHolder.getClient().embedding()
                // embedding model
                .model("Embedding-V1")
                .input(partition.stream().map(AlibabaJavaText::getText).collect(Collectors.toList()))
                .execute();
            List<JsonObject> insertData = response.getData().stream().map(embedding -> {
                AlibabaJavaText text = partition.get(embedding.getIndex());
                List<BigDecimal> vector = embedding.getEmbedding();
                System.out.printf("%s\n=>%s\n", text.getText(), vector);
                JsonObject row = new JsonObject();
                JsonArray vectorArray = new JsonArray();
                vector.forEach(vectorArray::add);
                row.add("vector", vectorArray);
                row.addProperty("h1", text.getFirstHeadline());
                row.addProperty("h2", text.getSecondHeadline());
                row.addProperty("text", text.format());
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
    public void similaritySearch() {
        String text = "创建线程池应该注意哪些问题？";
        EmbeddingResponse response = QianfanClientHolder.getClient().embedding()
            // embedding model
            .model("Embedding-V1")
            .input(Collections.singletonList(text))
            .execute();
        EmbeddingData embedding = response.getData().get(0);
        List<BigDecimal> vector = embedding.getEmbedding();
        System.out.printf("%s => %s\n", text, vector);
        List<BaseVector> singleVectorSearchData = Collections.singletonList(
            new FloatVec(vector.stream().map(BigDecimal::floatValue).collect(Collectors.toList())));
        SearchReq searchReq = SearchReq.builder()
            .collectionName(collectionName)
            .data(singleVectorSearchData)
            .topK(3)
            .outputFields(Arrays.asList("id", "score", "h1", "h2", "text"))
            .build();
        SearchResp singleVectorSearchRes = MilvusClientHolder.getClient().search(searchReq);
        System.out.println("similarity search result: ");
        singleVectorSearchRes.getSearchResults().stream().flatMap(Collection::stream)
            .forEach(rst -> System.out.printf("score: %s\nh1: %s\nh2: %s\n%s\n", rst.getScore(),
                rst.getEntity().get("h1"), rst.getEntity().get("h2"), rst.getEntity().get("text")));
    }

    @Test
    public void deleteEntities() {
        DeleteReq deleteReq = DeleteReq.builder()
            .collectionName(collectionName)
            .filter("id > 0")
            .build();
        DeleteResp deleteRes = MilvusClientHolder.getClient().delete(deleteReq);
        System.out.println(gson.toJson(deleteRes));
    }

    @Test
    public void readText() throws IOException {
        List<AlibabaJavaText> texts = parseAlibabaJavaTexts();
        System.out.println(texts.size());
        texts.forEach(text -> System.out.println(gson.toJson(text)));
    }

    private List<AlibabaJavaText> parseAlibabaJavaTexts() throws IOException {
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
                text.setFirstHeadline(line);
                continue;
            }
            if (isSecondHeadline(line)) {
                if (text.isNotBlank()) {
                    texts.add(text.clone());
                    text.clear();
                }
                text.setSecondHeadline(line);
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

    private boolean isFirstHeadline(String line) {
        Pattern pattern = Pattern.compile("^[零一二三四五六七八九]+、");
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    private boolean isSecondHeadline(String line) {
        Pattern pattern = Pattern.compile("^\\([零一二三四五六七八九]+\\)");
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    private boolean isThirdHeadline(String line) {
        Pattern pattern = Pattern.compile("^\\d+\\.");
        Matcher matcher = pattern.matcher(line);
        return matcher.find() && line.contains("【");
    }

}
