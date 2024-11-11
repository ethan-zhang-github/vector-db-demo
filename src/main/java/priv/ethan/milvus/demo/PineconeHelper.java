package priv.ethan.milvus.demo;

import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.Embedding;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PineconeHelper {

    /**
     * Specify the embedding model and parameters
     */
    private static final String embeddingModel = "multilingual-e5-large";

    public static List<Embedding> embedding(List<String> inputs, String inputType) throws ApiException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input_type", inputType);
        parameters.put("truncate", "END");
        // Convert the text into numerical vectors that Pinecone can index
        return PineconeClientHolder.getInference().embed(embeddingModel, parameters, inputs).getData();
    }

    public static Embedding embedding(String input, String inputType) throws ApiException {
        return embedding(Collections.singletonList(input), inputType).iterator().next();
    }

}
