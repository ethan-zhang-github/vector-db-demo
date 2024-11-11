package priv.ethan.milvus.demo;

import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import lombok.Getter;

public class PineconeClientHolder {

    @Getter
    private static final Pinecone pc;
    @Getter
    private static final Inference inference;

    static {
        // Initialize a Pinecone client with your API key
        pc = new Pinecone.Builder(System.getenv("pineconeApiKey")).build();
        inference = pc.getInferenceClient();
    }

}
