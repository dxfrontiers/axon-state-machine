package de.digitalfrontiers.axon.statemachine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class AxonStateMachineApplication

fun main(args: Array<String>) {
    runApplication<AxonStateMachineApplication>(*args)
}
