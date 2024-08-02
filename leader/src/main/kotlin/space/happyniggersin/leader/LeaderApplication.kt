package space.happyniggersin.leader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LeaderApplication

fun main(args: Array<String>) {
    runApplication<LeaderApplication>(*args)
}
