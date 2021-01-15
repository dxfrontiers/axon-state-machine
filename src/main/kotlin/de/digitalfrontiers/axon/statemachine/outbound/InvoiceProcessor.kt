package de.digitalfrontiers.axon.statemachine.outbound

import de.digitalfrontiers.axon.statemachine.domain.InvoicePaidEvent
import de.digitalfrontiers.axon.statemachine.domain.SendInvoiceCommand
import de.digitalfrontiers.axon.statemachine.domain.ShipmentDeliveredEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import kotlin.random.Random

/**
 * [CommandHandler] bean that simulates a third-party invoicing service,
 * by providing an invoice id and an [InvoicePaidEvent] after a random
 * delay.
 */
@Component
class InvoiceProcessor(
    private val taskScheduler: TaskScheduler,
    private val eventGateway: EventGateway
) {

    @CommandHandler
    fun handle(command: SendInvoiceCommand): UUID {
        val invoiceId = UUID.randomUUID()

        taskScheduler.schedule(
            {
                eventGateway.publish(
                    InvoicePaidEvent(invoiceId = invoiceId)
                )
            },
            Instant.now().plusSeconds(Random.nextLong(from = 10, until = 20)))

        return invoiceId
    }
}
