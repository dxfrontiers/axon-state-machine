package de.digitalfrontiers.axon.statemachine.outbound

import de.digitalfrontiers.axon.statemachine.domain.ShipItemsCommand
import de.digitalfrontiers.axon.statemachine.domain.ShipmentDeliveredEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import kotlin.random.Random

/**
 * [CommandHandler] bean that simulates a third-party shipment service,
 * by providing a shipment id and a [ShipmentDeliveredEvent] after a random
 * delay.
 */
@Component
class ShipmentProcessor(
    private val taskScheduler: TaskScheduler,
    private val eventGateway: EventGateway
) {

    @CommandHandler
    fun handle(command: ShipItemsCommand): UUID {
        val shipmentId = UUID.randomUUID()

        taskScheduler.schedule(
            {
                eventGateway.publish(
                    ShipmentDeliveredEvent(shipmentId = shipmentId)
                )
            },
            Instant.now().plusSeconds(Random.nextLong(from = 10, until = 20)))

        return shipmentId
    }
}
