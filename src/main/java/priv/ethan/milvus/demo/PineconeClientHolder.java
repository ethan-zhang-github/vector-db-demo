package priv.ethan.milvus.demo;

import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;

public class PineconeClientHolder {

    private static final Inference inference;

    static {
        // Initialize a Pinecone client with your API key
        Pinecone pc = new Pinecone.Builder("0666c6d6-f457-45b0-88e0-f5afa98c1889").build();
        inference = pc.getInferenceClient();
    }

    public static Inference get() {
        return inference;
    }

}
