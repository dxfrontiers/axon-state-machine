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
