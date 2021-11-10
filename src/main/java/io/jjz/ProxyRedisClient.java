package io.jjz;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.CommandListenerWriter;
import io.lettuce.core.ConnectionBuilder;
import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.ConnectionState;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisChannelWriter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.SslConnectionBuilder;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.protocol.CommandExpiryWriter;
import io.lettuce.core.protocol.CommandHandler;
import io.lettuce.core.protocol.DefaultEndpoint;
import io.lettuce.core.protocol.Endpoint;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.protocol.PushHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

import static io.lettuce.core.internal.LettuceStrings.isEmpty;

/**
 * Client of WeRedis Proxy Cluster
 */
public class ProxyRedisClient<K, V> extends RedisClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRedisClient.class);

    private static final String VERSION = ProxyRedisClient.class.getPackage().getImplementationVersion();

    private final RedisCodec<K, V> codec;


    public ProxyRedisClient(RedisCodec<K, V> codec) {
        Objects.requireNonNull(codec, "redis codec is null");
        this.codec = codec;
    }




    /**
     * return a available connection
     * throw a exception if not connection available
     */
    public ProxyRedisConnection<K, V> getConnection(String host, int port) {
        RedisURI redisURI = RedisURI.builder()
            .withHost(host).withPort(port)
            .withTimeout(Duration.ofMillis(1000))
            .build();

        setOptions(clientOptions());

        return connect(codec, redisURI);
    }


    /*
     * copy from super class
     */
    @Override
    public <KK, VV> ProxyRedisConnection<KK, VV> connect(RedisCodec<KK, VV> codec, RedisURI redisURI) {
        assertNotNull(redisURI);
        return getConnection(connectStandaloneAsync(codec, redisURI, redisURI.getTimeout()));
    }

    private <KK, VV> ConnectionFuture<ProxyRedisConnection<KK, VV>> connectStandaloneAsync(RedisCodec<KK, VV> codec,
                                                                                           RedisURI redisURI, Duration timeout) {
        assertNotNull(codec);
        checkValidRedisURI(redisURI);

        DefaultEndpoint endpoint = new DefaultEndpoint(getOptions(), getResources());
        RedisChannelWriter writer = endpoint;

        if (CommandExpiryWriter.isSupported(getOptions())) {
            writer = new CommandExpiryWriter(writer, getOptions(), getResources());
        }

        if (CommandListenerWriter.isSupported(getCommandListeners())) {
            writer = new CommandListenerWriter(writer, getCommandListeners());
        }

        ProxyRedisConnection<KK, VV> connection = newStatefulRedisConnection(writer, endpoint, codec, timeout);
        ConnectionFuture<ProxyRedisConnection<KK, VV>> future = connectStatefulAsync(connection, endpoint, redisURI,
            () -> new CommandHandler(getOptions(), getResources(), endpoint));

        future.whenComplete((channelHandler, throwable) -> {
            if (throwable != null) {
                connection.closeAsync();
            }
        });
        return future;
    }

    @Override
    protected <KK, VV> ProxyRedisConnection<KK, VV> newStatefulRedisConnection(RedisChannelWriter channelWriter,
                                                                                                      PushHandler pushHandler,
                                                                                                      RedisCodec<KK, VV> codec,
                                                                                                      Duration timeout) {
        return new ProxyRedisConnection<>(channelWriter, pushHandler, codec, timeout);
    }

    private <KK, VV, S> ConnectionFuture<S> connectStatefulAsync(ProxyRedisConnection<KK, VV> connection, Endpoint endpoint,
                                                                 RedisURI redisURI, Supplier<CommandHandler> commandHandlerSupplier) {

        ConnectionBuilder connectionBuilder;
        if (redisURI.isSsl()) {
            SslConnectionBuilder sslConnectionBuilder = SslConnectionBuilder.sslConnectionBuilder();
            sslConnectionBuilder.ssl(redisURI);
            connectionBuilder = sslConnectionBuilder;
        } else {
            connectionBuilder = ConnectionBuilder.connectionBuilder();
        }

        ConnectionState state = connection.getConnectionState();
        state.apply(redisURI);

        connectionBuilder.connection(connection);
        connectionBuilder.clientOptions(getOptions());
        connectionBuilder.clientResources(getResources());
        connectionBuilder.commandHandler(commandHandlerSupplier).endpoint(endpoint);

        connectionBuilder(getSocketAddress(redisURI), connectionBuilder, redisURI);
        connectionBuilder.connectionInitializer(createHandshake(state));

        ConnectionFuture<RedisChannelHandler<KK, VV>> future = initializeChannelAsync(connectionBuilder);

        return future.thenApply(channelHandler -> (S) connection);
    }



    /**
     * options of client
     */
    private ClientOptions clientOptions() {
        SocketOptions socketOptions = SocketOptions.builder()
            .keepAlive(true)
            .tcpNoDelay(true)
            .connectTimeout(Duration.ofMillis(1000))
            .build();

        return ClientOptions.builder()
            .protocolVersion(ProtocolVersion.RESP2) // HELLO 3 AUTH
            .autoReconnect(false) // 不自动重连
            .pingBeforeActivateConnection(false)
            //.timeoutOptions()  // todo
            .socketOptions(socketOptions)
            .build();
    }


    private static <K, V> void assertNotNull(RedisCodec<K, V> codec) {
        LettuceAssert.notNull(codec, "RedisCodec must not be null");
    }

    private static void assertNotNull(RedisURI redisURI) {
        LettuceAssert.notNull(redisURI, "RedisURI must not be null");
    }

    private static void checkValidRedisURI(RedisURI redisURI) {

        LettuceAssert.notNull(redisURI, "A valid RedisURI is required");

        if (redisURI.getSentinels().isEmpty()) {
            if (isEmpty(redisURI.getHost()) && isEmpty(redisURI.getSocket())) {
                throw new IllegalArgumentException("RedisURI for Redis Standalone does not contain a host or a socket");
            }
        } else {

            if (isEmpty(redisURI.getSentinelMasterId())) {
                throw new IllegalArgumentException("RedisURI for Redis Sentinel requires a masterId");
            }

            for (RedisURI sentinel : redisURI.getSentinels()) {
                if (isEmpty(sentinel.getHost()) && isEmpty(sentinel.getSocket())) {
                    throw new IllegalArgumentException("RedisURI for Redis Sentinel does not contain a host or a socket");
                }
            }
        }
    }
}
