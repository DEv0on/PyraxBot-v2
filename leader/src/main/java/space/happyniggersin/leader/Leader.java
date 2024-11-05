package space.happyniggersin.leader;

import discord4j.common.JacksonResources;
import discord4j.common.store.Store;
import discord4j.common.store.legacy.LegacyStoreLayout;
import discord4j.connect.common.ConnectGatewayOptions;
import discord4j.connect.common.UpstreamGatewayClient;
import discord4j.connect.rabbitmq.ConnectRabbitMQ;
import discord4j.connect.rabbitmq.ConnectRabbitMQSettings;
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSink;
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSource;
import discord4j.connect.rabbitmq.gateway.RabbitMQSinkMapper;
import discord4j.connect.rabbitmq.gateway.RabbitMQSourceMapper;
import discord4j.connect.rsocket.global.RSocketGlobalRateLimiter;
import discord4j.connect.rsocket.router.RSocketRouter;
import discord4j.connect.rsocket.router.RSocketRouterOptions;
import discord4j.connect.rsocket.shard.RSocketShardCoordinator;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.dispatch.DispatchEventMapper;
import discord4j.core.shard.ShardingStrategy;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.store.api.readonly.ReadOnlyStoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Optional;

@Component
public class Leader {
    private static final InetSocketAddress routerServerAddress = new InetSocketAddress(
            Optional.ofNullable(System.getenv("GLOBAL_ROUTER_SERVER_HOST")).orElse("localhost"),
            Integer.parseInt(Optional.ofNullable(System.getenv("GLOBAL_ROUTER_SERVER_PORT")).orElse("33330"))
    );
    private static final InetSocketAddress coordinatorServerAddress = new InetSocketAddress(
            Optional.ofNullable(System.getenv("SHARD_COORDINATOR_SERVER_HOST")).orElse("localhost"),
            Integer.parseInt(Optional.ofNullable(System.getenv("SHARD_COORDINATOR_SERVER_PORT")).orElse("33332"))
    );
    private static final String redisAddress = Optional.ofNullable(System.getenv("REDIS_CLIENT_URI")).orElse("redis://localhost:6379");
    private static final String rabbitmqHost = Optional.ofNullable(System.getenv("RABBITMQ_HOSTNAME")).orElse("localhost");
    private static final int rabbitmqPort = Integer.parseInt(Optional.ofNullable(System.getenv("RABBITMQ_PORT")).orElse("5672"));
    private static final String rabbitmqUser = System.getenv("RABBITMQ_DEFAULT_USER");
    private static final String rabbitmqPass = System.getenv("RABBITMQ_DEFAULT_PASS");


    @Bean
    public GatewayDiscordClient discordClient() {
        RedisClient redisClient = RedisClient.create(redisAddress);

        JacksonResources jackson = JacksonResources.create();
        ShardingStrategy shardingStrategy = ShardingStrategy.recommended();

        ConnectRabbitMQSettings settings = ConnectRabbitMQSettings.create().withAddress(rabbitmqHost, rabbitmqPort).withUser(rabbitmqUser).withPassword(rabbitmqPass);
        ConnectRabbitMQ rabbitMQ = ConnectRabbitMQ.createFromSettings(settings);

        RabbitMQSinkMapper sink = RabbitMQSinkMapper.createBinarySinkToDirect("payload");
        RabbitMQSourceMapper source = RabbitMQSourceMapper.createBinarySource();

        GatewayDiscordClient client = DiscordClient.builder(System.getenv("BOT_TOKEN"))
                .setJacksonResources(jackson)
                .setGlobalRateLimiter(RSocketGlobalRateLimiter.createWithServerAddress(routerServerAddress))
                .setExtraOptions(o -> new RSocketRouterOptions(o, request -> routerServerAddress))
                .build(RSocketRouter::new)
                .gateway()
                .setSharding(shardingStrategy)
                .setShardCoordinator(RSocketShardCoordinator.createWithServerAddress(coordinatorServerAddress))
                .setDisabledIntents(IntentSet.of(
                        Intent.GUILD_PRESENCES,
                        Intent.GUILD_MESSAGE_TYPING,
                        Intent.DIRECT_MESSAGE_TYPING))
                .setEnabledIntents(IntentSet.all())
                .setDispatchEventMapper(DispatchEventMapper.discardEvents())
                .setStore(Store.fromLayout(LegacyStoreLayout.of(new ReadOnlyStoreService(RedisStoreService.builder()
                        .redisClient(redisClient)
                        .useSharedConnection(false)
                        .build()))))
                .setExtraOptions(o -> new ConnectGatewayOptions(o,
                        RabbitMQPayloadSink.create(sink, rabbitMQ),
                        RabbitMQPayloadSource.create(source, rabbitMQ, "gateway")))
                .login(UpstreamGatewayClient::new)
                .blockOptional()
                .orElseThrow(RuntimeException::new);

        client.onDisconnect().block();
        rabbitMQ.close();

        return client;
    }
}