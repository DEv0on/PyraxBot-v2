package com.dev0on.globalrouter

import discord4j.connect.rsocket.global.RSocketGlobalRouterServer
import discord4j.rest.request.BucketGlobalRateLimiter
import discord4j.rest.request.RequestQueueFactory
import io.rsocket.transport.netty.server.CloseableChannel
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import reactor.util.Logger
import reactor.util.Loggers
import reactor.util.retry.Retry
import java.net.InetSocketAddress
import java.time.Duration

@Component
class GlobalRouterServer {

    companion object {
        val port: Int = (System.getenv("GLOBAL_ROUTER_SERVER_PORT") ?: "33330").toInt()
        val log: Logger = Loggers.getLogger(GlobalRouterServer::class.java)
    }

    @Bean
    fun globalRouterServerService() {
        RSocketGlobalRouterServer(InetSocketAddress(port), BucketGlobalRateLimiter.create(), Schedulers.parallel(), RequestQueueFactory.buffering())
            .start()
            .doOnNext { log.info("Started global router server at {}", it.address()) }
            .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofMinutes(1)))
            .flatMap(CloseableChannel::onClose)
            .block()
    }

}