package priv.ethan.vector.db.demo;

import com.google.common.collect.Lists;
import io.pinecone.clients.Index;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.Embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PineconeHelper {

    /**
     * Specify the embedding model and parameters
     */
    private static final String embeddingModel = "multilingual-e5-large";

    public static IndexModel createIndex(String indexName) {
        return PineconeClientHolder.getPc()
            .createServerlessIndex(indexName, "cosine", 1024, "aws", "us-east-1", DeletionProtection.DISABLED);
    }

    public static Index getIndex(String indexName) {
        // Target the index where you'll store the vector embeddings
        return PineconeClientHolder.getPc().getIndexConnection(indexName);
    }

    public static List<Embedding> embedding(List<String> inputs, String inputType) throws ApiException {
        List<Embedding> rst = new ArrayList<>(inputs.size());
        Lists.partition(inputs, 96).forEach(partition -> {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("input_type", inputType);
            parameters.put("truncate", "END");
            // Convert the text into numerical vectors that Pinecone can index
            try {
                List<Embedding> embeddings = PineconeClientHolder.getInference()
                    .embed(embeddingModel, parameters, partition).getData();
                rst.addAll(embeddings);
            } catch (ApiException e) {
                log.error("embedding error", e);
            }
        });
        return rst;
    }

    public static Embedding embedding(String input, String inputType) throws ApiException {
        return embedding(Collections.singletonList(input), inputType).iterator().next();
    }

}
