package dev.themobileapps.mrrsb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class MrrSbApplication

fun main(args: Array<String>) {
    runApplication<MrrSbApplication>(*args)
}
