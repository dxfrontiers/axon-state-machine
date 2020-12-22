package de.digitalfrontiers.axon.statemachine.domain

import org.springframework.stereotype.Component
import java.util.*

@Component
class OrderIdProvider {

    fun generateOrderId(): UUID =
        UUID.randomUUID()
}
