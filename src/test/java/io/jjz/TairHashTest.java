package io.jjz;

import io.jjz.extend.ProxyCommands;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

public class TairHashTest {

    protected static ProxyRedisClient<String, String> proxyRedisClient;

    private static final String K1 = "K1";
    private static final String F1 = "field1";
    private static final String F2 = "field2";
    private static final String V1 = "V1";
    private static final String V2 = "V2";

    @BeforeAll
    public static void setup() throws Exception {
        proxyRedisClient = new ProxyRedisClient<>(RedisCodec.of(StringCodec.UTF8, StringCodec.UTF8));

    }


    @Test
    public void testScan() {
        ProxyRedisConnection<String, String> connection = proxyRedisClient.getConnection("169.254.149.66", 30003);
        ProxyCommands<String, String> sync = connection.sync();
        sync.del(K1);

        HashMap<String, String> map = new HashMap<>();
        map.put("f1", "v1");
        map.put("f2", "v2");
        map.put("f3", "v3");
        map.put("f4", "v4");
        map.put("f5", "v5");
        map.put("f6", "v6");
        map.put("f7", "v7");
        map.put("f8", "v8");
        map.put("f9", "v9");
        Assertions.assertEquals("OK", sync.exHMSet(K1, map));

        // todo: it is ok
        List<Object> scanResult1 = sync.exHScan1(K1, ScanCursor.INITIAL, new ScanArgs());

        // todo: throw exception
        MapScanCursor<String, String> scanResult2 = sync.exHScan2(K1, ScanCursor.INITIAL, new ScanArgs());

    }

}
