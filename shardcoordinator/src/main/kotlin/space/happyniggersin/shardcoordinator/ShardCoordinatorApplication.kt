package space.happyniggersin.shardcoordinator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class ShardCoordinatorApplication {
    @Bean(name = ["shardCoordinatorServerBean"], initMethod = "init")
    fun getShardCoordinatorServerBean(): ShardCoordinatorServerBean {
        return ShardCoordinatorServerBean()
    }
}

fun main(args: Array<String>) {
    runApplication<ShardCoordinatorApplication>(*args)
}
