package io.jjz.extend;

import io.lettuce.core.MapScanCursor;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.List;
import java.util.Map;

public interface ProxyAsyncCommands<K, V> extends RedisAsyncCommands<K, V> {

    RedisFuture<String> exHMSet(K key, Map<K, V> map);

    RedisFuture<List<Object>> exHScan1(K key, ScanCursor scanCursor, ScanArgs scanArgs);

    RedisFuture<MapScanCursor<K, V>> exHScan2(K key, ScanCursor scanCursor, ScanArgs scanArgs);

}
