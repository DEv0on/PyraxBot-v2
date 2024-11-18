package space.happyniggersin.shardcoordinator

import discord4j.connect.rsocket.shard.RSocketShardCoordinatorServer
import io.rsocket.transport.netty.server.CloseableChannel
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import reactor.util.Logger
import reactor.util.Loggers
import java.net.InetSocketAddress

@Component
class ShardCoordinatorServerBean {
    companion object {
        val port: Int = (System.getenv("SHARD_COORDINATOR_SERVER_PORT") ?: "33332").toInt()
        val log: Logger = Loggers.getLogger(ShardCoordinatorServerBean::class.java)
    }

    @Bean
    fun init() {
        RSocketShardCoordinatorServer(InetSocketAddress(port))
            .start()
            .doOnNext { cc: CloseableChannel ->
                log.info(
                    "Started shard coordinator server at {}",
                    cc.address()
                )
            }
            .blockOptional()
            .orElseThrow { RuntimeException() }
            .onClose()
            .block()
    }
}