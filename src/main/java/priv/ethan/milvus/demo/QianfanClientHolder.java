package priv.ethan.milvus.demo;

import com.baidubce.qianfan.Qianfan;

public class QianfanClientHolder {

    private static final String accessKey = "ALTAKLnmLFNRH06tPrtbcDzcun";
    private static final String secretKey = "ab30d2249b7f45c8a18c0992ddb4a510";

    private static final Qianfan qianfan;

    static {
        qianfan = new Qianfan(accessKey, secretKey);
    }

    public static Qianfan getClient() {
        return qianfan;
    }

}
