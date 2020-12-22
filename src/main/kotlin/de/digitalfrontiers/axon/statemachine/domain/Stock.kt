package de.digitalfrontiers.axon.statemachine.domain

import org.springframework.stereotype.Component
import java.util.*
import kotlin.random.Random

@Component
class Stock {

    private val randomStock =
        (1..10)
            .map { UUID.randomUUID() to Random.nextDouble(10.0, 100.0)}
            .toMap()

    fun productItems(): Map<UUID, Double> = randomStock
}


