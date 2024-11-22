package com.dev0on.worker

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication
@ComponentScan("com.dev0on.common", "com.dev0on.worker")
@EnableReactiveMongoRepositories("com.dev0on.common")
class WorkerApplication

fun main(args: Array<String>) {
    SpringApplication.run(WorkerApplication::class.java, *args)
}
