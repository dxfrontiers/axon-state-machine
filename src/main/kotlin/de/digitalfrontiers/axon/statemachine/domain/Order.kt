package de.digitalfrontiers.axon.statemachine.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.util.*

@Aggregate
class Order {

    @AggregateIdentifier
    lateinit var id: UUID
    lateinit var productItems: Map<UUID, Long>

    constructor() {}

    constructor(
        id: UUID,
        productItems: Map<UUID, Long>,
        stock: Stock
    ) {
        AggregateLifecycle.apply(
            OrderPlacedEvent(
                orderId = id,
                productItems = productItems,
                totalPrice = productItems
                    .mapValues { (id, quantity) -> quantity * stock.productItems().getValue(id) }
                    .values
                    .sum()
            )
        )
    }

    @EventSourcingHandler
    fun on(event: OrderPlacedEvent) {
        id = event.orderId
        productItems = event.productItems
    }

    @CommandHandler
    fun deny(command: DenyOrderCommand) {
        AggregateLifecycle.apply(
            OrderDeniedEvent(
                orderId = command.orderId,
                reason = command.reason
            )
        )
    }

    @EventSourcingHandler
    fun on(event: OrderDeniedEvent) {
        AggregateLifecycle.markDeleted()
    }

    @CommandHandler
    fun complete(command: MarkOrderCompleteCommand) {
        AggregateLifecycle.apply(
            OrderCompletedEvent(
                orderId = command.orderId,
                invoiceId = command.invoiceId,
                shipmentId = command.shipmentId
            )
        )
    }

    @EventSourcingHandler
    fun on(event: OrderCompletedEvent) {
        AggregateLifecycle.markDeleted()
    }
}
