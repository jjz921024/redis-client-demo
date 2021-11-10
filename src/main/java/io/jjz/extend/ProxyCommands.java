package io.jjz.extend;

import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.List;
import java.util.Map;

public interface ProxyCommands<K, V> extends RedisCommands<K, V> {

    String exHMSet(K key, Map<K, V> map);

    List<Object> exHScan1(K key, ScanCursor scanCursor, ScanArgs scanArgs);

    MapScanCursor<K, V> exHScan2(K key, ScanCursor scanCursor, ScanArgs scanArgs);

}
