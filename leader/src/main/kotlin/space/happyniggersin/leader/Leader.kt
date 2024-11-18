package space.happyniggersin.leader

import discord4j.common.JacksonResources
import discord4j.common.store.Store
import discord4j.common.store.legacy.LegacyStoreLayout
import discord4j.connect.common.ConnectGatewayOptions
import discord4j.connect.common.UpstreamGatewayClient
import discord4j.connect.rabbitmq.ConnectRabbitMQ
import discord4j.connect.rabbitmq.ConnectRabbitMQSettings
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSink
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSource
import discord4j.connect.rabbitmq.gateway.RabbitMQSinkMapper
import discord4j.connect.rabbitmq.gateway.RabbitMQSourceMapper
import discord4j.connect.rsocket.global.RSocketGlobalRateLimiter
import discord4j.connect.rsocket.router.RSocketRouter
import discord4j.connect.rsocket.router.RSocketRouterOptions
import discord4j.connect.rsocket.shard.RSocketShardCoordinator
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.dispatch.DispatchEventMapper
import discord4j.core.shard.ShardingStrategy
import discord4j.gateway.GatewayOptions
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import discord4j.rest.request.DiscordWebRequest
import discord4j.rest.request.RouterOptions
import discord4j.store.api.readonly.ReadOnlyStoreService
import discord4j.store.redis.RedisStoreService
import io.lettuce.core.RedisClient
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
class Leader {
    companion object {
        val routerServerAddress = InetSocketAddress(
            System.getenv("GLOBAL_ROUTER_SERVER_HOST") ?: "localhost",
            (System.getenv("GLOBAL_ROUTER_SERVER_PORT") ?: "33330").toInt()
        )
        val coordinatorServerAddress = InetSocketAddress(
            System.getenv("SHARD_COORDINATOR_SERVER_HOST") ?: "localhost",
            (System.getenv("SHARD_COORDINATOR_SERVER_PORT") ?: "33332").toInt()
        )
        val redisAddress = System.getenv("REDIS_CLIENT_URI") ?: "redis://localhost:6379"
        val rabbitmqHost = System.getenv("RABBITMQ_HOSTNAME") ?: "localhost"
        val rabbitmqPort = (System.getenv("RABBITMQ_PORT") ?: "5672").toInt()
        val rabbitmqUser: String? = System.getenv("RABBITMQ_DEFAULT_USER")
        val rabbitmqPass: String? = System.getenv("RABBITMQ_DEFAULT_PASS")
    }

    @Bean
    fun discordClient(): GatewayDiscordClient {
        val redisClient = RedisClient.create(redisAddress)

        val jackson = JacksonResources.create()
        val shardingStrategy = ShardingStrategy.recommended()

        val settings = ConnectRabbitMQSettings.create().withAddress(rabbitmqHost, rabbitmqPort).withUser(rabbitmqUser)
            .withPassword(
                rabbitmqPass
            )
        val rabbitMQ = ConnectRabbitMQ.createFromSettings(settings)

        val sink = RabbitMQSinkMapper.createBinarySinkToDirect("payload")
        val source = RabbitMQSourceMapper.createBinarySource()

        val client = DiscordClient.builder(System.getenv("BOT_TOKEN"))
            .setJacksonResources(jackson)
            .setGlobalRateLimiter(RSocketGlobalRateLimiter.createWithServerAddress(routerServerAddress))
            .setExtraOptions { o: RouterOptions ->
                RSocketRouterOptions(
                    o
                ) { _: DiscordWebRequest -> routerServerAddress }
            }
            .build { routerOptions: RSocketRouterOptions ->
                RSocketRouter(
                    routerOptions
                )
            }
            .gateway()
            .setSharding(shardingStrategy)
            .setShardCoordinator(RSocketShardCoordinator.createWithServerAddress(coordinatorServerAddress))
            .setDisabledIntents(
                IntentSet.of(
                    Intent.GUILD_PRESENCES,
                    Intent.GUILD_MESSAGE_TYPING,
                    Intent.DIRECT_MESSAGE_TYPING
                )
            )
            .setEnabledIntents(IntentSet.all())
            .setDispatchEventMapper(DispatchEventMapper.discardEvents())
            .setStore(
                Store.fromLayout(
                    LegacyStoreLayout.of(
                        ReadOnlyStoreService(
                            RedisStoreService.builder()
                                .redisClient(redisClient)
                                .useSharedConnection(false)
                                .build()
                        )
                    )
                )
            )
            .setExtraOptions { o: GatewayOptions ->
                ConnectGatewayOptions(
                    o,
                    RabbitMQPayloadSink.create(sink, rabbitMQ),
                    RabbitMQPayloadSource.create(source, rabbitMQ, "gateway")
                )
            }
            .login { gatewayOptions: ConnectGatewayOptions ->
                UpstreamGatewayClient(
                    gatewayOptions
                )
            }
            .blockOptional()
            .orElseThrow { RuntimeException() }

        client.onDisconnect().block()
        rabbitMQ.close()

        return client
    }
}