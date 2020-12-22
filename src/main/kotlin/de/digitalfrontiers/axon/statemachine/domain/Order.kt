package de.digitalfrontiers.axon.statemachine.domain

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
}
