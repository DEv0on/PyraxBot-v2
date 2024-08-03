package space.happyniggersin.worker

import discord4j.common.JacksonResources
import discord4j.common.store.Store
import discord4j.common.store.legacy.LegacyStoreLayout
import discord4j.connect.common.ConnectGatewayOptions
import discord4j.connect.common.DownstreamGatewayClient
import discord4j.connect.rabbitmq.ConnectRabbitMQ
import discord4j.connect.rabbitmq.ConnectRabbitMQSettings
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSink
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSource
import discord4j.connect.rabbitmq.gateway.RabbitMQSinkMapper
import discord4j.connect.rabbitmq.gateway.RabbitMQSourceMapper
import discord4j.connect.rsocket.global.RSocketGlobalRateLimiter
import discord4j.connect.rsocket.router.RSocketRouter
import discord4j.connect.rsocket.router.RSocketRouterOptions
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.shard.MemberRequestFilter
import discord4j.core.shard.ShardingStrategy
import discord4j.store.api.readonly.ReadOnlyStoreService
import discord4j.store.redis.RedisStoreService
import io.lettuce.core.RedisClient
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.Logger
import reactor.util.Loggers
import java.net.InetSocketAddress

@Component
class Worker {
    companion object {
        val routerServerAddress = InetSocketAddress(
            System.getenv("GLOBAL_ROUTER_SERVER_HOST") ?: "localhost",
            (System.getenv("GLOBAL_ROUTER_SERVER_PORT") ?: "33330").toInt()
        )
        val redisAddress = System.getenv("REDIS_CLIENT_URI") ?: "redis://localhost:6379"
        val rabbitmqHost = System.getenv("RABBITMQ_HOSTNAME") ?: "localhost"
        val rabbitmqPort = (System.getenv("RABBITMQ_PORT") ?: "5672").toInt()
        val rabbitmqUser: String? = System.getenv("RABBITMQ_DEFAULT_USER")
        val rabbitmqPass: String? = System.getenv("RABBITMQ_DEFAULT_PASS")

    }

    @Bean
    fun workerApp(): GatewayDiscordClient {
        val redisClient = RedisClient.create(redisAddress)

        var rmqSettings = ConnectRabbitMQSettings.create().withAddress(rabbitmqHost, rabbitmqPort)
        if (rabbitmqUser != null)
            rmqSettings = rmqSettings.withUser(rabbitmqUser)
        if (rabbitmqPass != null)
            rmqSettings = rmqSettings.withPassword(rabbitmqPass)
        val rabbitMQ = ConnectRabbitMQ.createFromSettings(rmqSettings)
        val sinkMapper = RabbitMQSinkMapper.createBinarySinkToDirect("gateway")
        val sourceMapper = RabbitMQSourceMapper.createBinarySource()

        val jackson = JacksonResources.create()
        val shardingStrategy = ShardingStrategy.recommended()

        val client: GatewayDiscordClient = DiscordClient.builder(System.getenv("BOT_TOKEN"))
            .setJacksonResources(jackson)
            .setGlobalRateLimiter(RSocketGlobalRateLimiter.createWithServerAddress(routerServerAddress))
            .setExtraOptions { o ->
                RSocketRouterOptions(
                    o
                ) { routerServerAddress }
            }
            .build { routerOptions: RSocketRouterOptions? ->
                RSocketRouter(
                    routerOptions
                )
            }
            .gateway()
            .setSharding(shardingStrategy)
            .setMemberRequestFilter(MemberRequestFilter.none())
            .setStore(
                Store.fromLayout(
                    LegacyStoreLayout.of(
                        ReadOnlyStoreService(
                            RedisStoreService.builder()
                                .redisClient(redisClient)
                                .build()
                        )
                    )
                )
            )
            .setExtraOptions { o ->
                ConnectGatewayOptions(
                    o,
                    RabbitMQPayloadSink.create(sinkMapper, rabbitMQ),
                    RabbitMQPayloadSource.create(sourceMapper, rabbitMQ, "payload")
                )
            }
            .login(::DownstreamGatewayClient)
            .blockOptional()
            .orElseThrow(::RuntimeException)

        NoBotSupport.create(client)
            .eventHandlers()
            .block()
        rabbitMQ.close()

        return client
    }

    class NoBotSupport internal constructor(private val client: GatewayDiscordClient) {
        fun eventHandlers(): Mono<Void> {
            return Mono.`when`(readyHandler(client), ciInteractionHandler(client))
        }

        companion object {
            private val log: Logger = Loggers.getLogger(NoBotSupport::class.java)

            fun create(client: GatewayDiscordClient): NoBotSupport {
                return NoBotSupport(client)
            }

            fun readyHandler(client: GatewayDiscordClient): Mono<Void> {
                return client.on(ReadyEvent::class.java)
                    .doOnNext { ready: ReadyEvent ->
                        log.info(
                            "Shard [{}] logged in as {}",
                            ready.shardInfo,
                            ready.self.username
                        )
                    }
                    .then()
            }

            fun ciInteractionHandler(client: GatewayDiscordClient): Mono<Void> {
                return client.on(ChatInputInteractionEvent::class.java)
                    .flatMap {
                        Mono.`when`(chatInputInteractionEvent(it))
                    }.then()
            }

            fun chatInputInteractionEvent(event: ChatInputInteractionEvent): Mono<Void> {
                log.info(event.toString())
                return event.deferReply()
            }
        }
    }
}