package de.digitalfrontiers.axon.statemachine.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class AddToShoppingCartCommand(
    @TargetAggregateIdentifier
    val shoppingCartId: UUID,
    val productItemId: UUID,
    val quantity: Long
)

data class RemoveFromShoppingCartCommand(
    @TargetAggregateIdentifier
    val shoppingCartId: UUID,
    val productItemId: UUID,
    val quantity: Long
)

data class PlaceOrderCommand(
    @TargetAggregateIdentifier
    val shoppingCartId: UUID
)

data class ReserveCreditCommand(
    val amount: Double
)

data class SendInvoiceCommand(
    val orderId: UUID,
    val productItems: Map<UUID, Long>,
    val totalPrice: Double
)

data class ShipItemsCommand(
    // destination address omitted for brevity
    val productItems: Map<UUID, Long>
)

data class MarkOrderCompleteCommand(
    @TargetAggregateIdentifier
    val orderId: UUID,
    val invoiceId: UUID,
    val shipmentId: UUID
)

data class DenyOrderCommand(
    @TargetAggregateIdentifier
    val orderId: UUID,
    val reason: String
)
