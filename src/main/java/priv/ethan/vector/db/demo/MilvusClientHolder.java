package priv.ethan.vector.db.demo;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;

public class MilvusClientHolder {

    private static final String CLUSTER_ENDPOINT = "https://in03-2dec7c54e9a16ae.serverless.gcp-us-west1.cloud.zilliz.com";
    private static final String TOKEN = "f1278f51a3b507241cff00319ea331fa44cef2781d96bbf66ee6dcb0c4fa372f077c7c4498d6136984f534328775fae0edf69bd2";

    private static final MilvusClientV2 client;

    static {
        // 1. Connect to Milvus server
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri(CLUSTER_ENDPOINT)
            .token(TOKEN)
            .build();
        client = new MilvusClientV2(connectConfig);
    }

    public static MilvusClientV2 getClient() {
        return client;
    }

}
