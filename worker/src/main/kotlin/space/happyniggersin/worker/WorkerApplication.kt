package space.happyniggersin.worker

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication
@ComponentScan("space.happyniggersin.common", "space.happyniggersin.worker")
@EnableReactiveMongoRepositories("space.happyniggersin.common")
class WorkerApplication

fun main(args: Array<String>) {
    SpringApplication.run(WorkerApplication::class.java, *args)
}
