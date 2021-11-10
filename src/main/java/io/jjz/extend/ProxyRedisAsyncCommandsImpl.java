package io.jjz.extend;

import io.jjz.ProxyRedisConnection;
import io.jjz.common.ExtendCommandType;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.RedisAsyncCommandsImpl;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.SetArgs;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.output.MapScanOutput;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.CommandArgs;

import java.util.List;
import java.util.Map;

/**
 * 同步和异步操作
 * 最后将请求编解码的实际逻辑处
 */
public class ProxyRedisAsyncCommandsImpl<K, V> extends RedisAsyncCommandsImpl<K, V> implements ProxyAsyncCommands<K, V> {

    private static final String MUST_NOT_BE_NULL = "must not be null";
    private static final String MUST_NOT_BE_EMPTY = "must not be empty";

    private final RedisCodec<K, V> codec;

    private static final SetArgs EMPTY_SET_ARGS = new SetArgs();


    /**
     * Initialize a new instance.
     *
     * @param connection the connection to operate on
     * @param codec      the codec for command encoding
     */
    public ProxyRedisAsyncCommandsImpl(ProxyRedisConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
        this.codec = codec;
    }

    @Override
    public ProxyRedisConnection<K, V> getStatefulConnection() {
        return (ProxyRedisConnection<K, V>) super.getConnection();
    }

    // EXHMSET key field value [field value...]
    @Override
    public RedisFuture<String> exHMSet(K key, Map<K, V> map) {
        LettuceAssert.notNull(key, "Key " + MUST_NOT_BE_NULL);
        LettuceAssert.notNull(map, "Map " + MUST_NOT_BE_NULL);
        LettuceAssert.isTrue(!map.isEmpty(), "Map " + MUST_NOT_BE_EMPTY);

        CommandArgs<K, V> args = new CommandArgs<>(codec)
            .addKey(key).add(map);

        return dispatch(ExtendCommandType.EXHMSET, new StatusOutput<>(codec), args);
    }


    // EXHSCAN key cursor [MATCH pattern] [COUNT count]
    @Override
    public RedisFuture<List<Object>> exHScan1(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        LettuceAssert.notNull(key, "Key " + MUST_NOT_BE_NULL);
        LettuceAssert.notNull(scanArgs, "ScanArgs " + MUST_NOT_BE_NULL);
        LettuceAssert.notNull(scanCursor, "ScanCursor " + MUST_NOT_BE_NULL);
        LettuceAssert.isTrue(!scanCursor.isFinished(), "ScanCursor must not be finished");

        CommandArgs<K, V> args = new CommandArgs<>(codec)
            .addKey(key);

        args.add(scanCursor.getCursor());
        scanArgs.build(args);

        // todo: have to use ArrayOutput, it is not friendly
        return dispatch(new Command<>(ExtendCommandType.EXHSCAN, new ArrayOutput<>(codec), args));
    }


    @Override
    public RedisFuture<MapScanCursor<K, V>> exHScan2(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        LettuceAssert.notNull(key, "Key " + MUST_NOT_BE_NULL);
        LettuceAssert.notNull(scanArgs, "ScanArgs " + MUST_NOT_BE_NULL);
        LettuceAssert.notNull(scanCursor, "ScanCursor " + MUST_NOT_BE_NULL);
        LettuceAssert.isTrue(!scanCursor.isFinished(), "ScanCursor must not be finished");

        CommandArgs<K, V> args = new CommandArgs<>(codec)
            .addKey(key);

        args.add(scanCursor.getCursor());
        scanArgs.build(args);

        // todo: error not support decode long
        return dispatch(new Command<>(ExtendCommandType.EXHSCAN, new MapScanOutput<>(codec), args));
    }

}
