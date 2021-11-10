package io.jjz;

import io.jjz.extend.ProxyAsyncCommands;
import io.jjz.extend.ProxyCommands;
import io.jjz.extend.ProxyRedisAsyncCommandsImpl;
import io.lettuce.core.RedisChannelWriter;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.protocol.PushHandler;

import java.time.Duration;

public class ProxyRedisConnection<K, V> extends StatefulRedisConnectionImpl<K, V> {

    public ProxyRedisConnection(RedisChannelWriter writer, PushHandler pushHandler, RedisCodec<K, V> codec, Duration timeout) {
        super(writer, pushHandler, codec, timeout);
    }

    @Override
    public ProxyCommands<K, V> sync() {
        return (ProxyCommands<K, V>) sync;
    }

    @Override
    protected ProxyCommands<K, V> newRedisSyncCommandsImpl() {
        return syncHandler(async(), ProxyCommands.class);
    }

    @Override
    public ProxyAsyncCommands<K, V> async() {
        return (ProxyAsyncCommands<K, V>) async;
    }

    @Override
    protected ProxyRedisAsyncCommandsImpl<K, V> newRedisAsyncCommandsImpl() {
        return new ProxyRedisAsyncCommandsImpl<>(this, codec);
    }

}
