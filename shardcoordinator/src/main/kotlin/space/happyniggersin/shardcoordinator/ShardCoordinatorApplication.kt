package space.happyniggersin.shardcoordinator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShardCoordinatorApplication

fun main(args: Array<String>) {
    runApplication<ShardCoordinatorApplication>(*args)
}
