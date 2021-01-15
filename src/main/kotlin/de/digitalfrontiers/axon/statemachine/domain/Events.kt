package de.digitalfrontiers.axon.statemachine.domain

import org.axonframework.serialization.Revision
import java.util.*

@Revision("1")
data class ShoppingCartCreatedEvent(
    val shoppingCartId: UUID
)

@Revision("1")
data class ProductItemAddedEvent(
    val shoppingCartId: UUID,
    val productItemId: UUID,
    val quantity: Long
)

@Revision("1")
data class ProductItemRemovedEvent(
    val shoppingCartId: UUID,
    val productItemId: UUID,
    val quantity: Long
)

@Revision("1")
data class ShoppingCartOrderedEvent(
    val shoppingCartId: UUID,
    val orderId: UUID
)

@Revision("1")
data class OrderPlacedEvent(
    val orderId: UUID,
    val productItems: Map<UUID, Long>,
    val totalPrice: Double
)

@Revision("1")
data class CreditReservedEvent(
    val orderId: UUID,
    val amount: Double
)

@Revision("1")
data class InvoiceRequestedEvent(
    val invoiceId: UUID
)

@Revision("1")
data class InvoicePaidEvent(
    val invoiceId: UUID
)

@Revision("1")
data class ShipmentRequestedEvent(
    val shipmentId: UUID
)

@Revision("1")
data class ShipmentDeliveredEvent(
    val shipmentId: UUID
)

@Revision("1")
data class OrderDeniedEvent(
    val orderId: UUID,
    val reason: String
)

@Revision("1")
data class OrderCompletedEvent(
    val orderId: UUID,
    val invoiceId: UUID,
    val shipmentId: UUID
)
