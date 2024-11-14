package priv.ethan.vector.db.demo;

import io.milvus.v2.service.collection.request.CreateCollectionReq;

public class MilvusClientOperator {

    public static void createCollection(String name, int dimension) {
        // 2. Create a collection in quick setup mode
        CreateCollectionReq quickSetupReq = CreateCollectionReq.builder()
            .collectionName(name)
            .dimension(dimension) // The dimensionality should be an integer greater than 1.
            .build();

        MilvusClientHolder.getClient().createCollection(quickSetupReq);
    }

}
