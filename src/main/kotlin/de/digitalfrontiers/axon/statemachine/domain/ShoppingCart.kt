package de.digitalfrontiers.axon.statemachine.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@Aggregate
class ShoppingCart {

    @AggregateIdentifier
    var id: UUID? = null

    val productItems = mutableMapOf<UUID, Long>()

    @EventSourcingHandler
    fun on(event: ShoppingCartCreatedEvent) {
        id = event.shoppingCartId
    }

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    fun addToCart(command: AddToShoppingCartCommand) {
        if (id == null) {
            AggregateLifecycle.apply(
                ShoppingCartCreatedEvent(shoppingCartId = command.shoppingCartId)
            )
        }

        AggregateLifecycle.apply(
            ProductItemAddedEvent(
                shoppingCartId = command.shoppingCartId,
                productItemId = command.productItemId,
                quantity = command.quantity
            )
        )
    }

    @EventSourcingHandler
    fun on(event: ProductItemAddedEvent) {
        productItems.merge(event.productItemId, event.quantity, Math::addExact)
    }

    @CommandHandler
    fun removeFromCart(command: RemoveFromShoppingCartCommand) {
        require(command.productItemId in productItems) { "Product ${command.productItemId} not in cart." }
        require(command.quantity <= productItems.getValue(command.productItemId)) { "Invalid quantity: ${command.quantity}" }

        AggregateLifecycle.apply(
            ProductItemRemovedEvent(
                shoppingCartId = command.shoppingCartId,
                productItemId = command.productItemId,
                quantity = command.quantity
            )
        )
    }

    @EventSourcingHandler
    fun on(event: ProductItemRemovedEvent) {
        productItems.merge(event.productItemId, event.quantity) { a, b ->
            (a - b).takeIf { it > 0L }
        }
    }

    @CommandHandler
    fun placeOrder(
        command: PlaceOrderCommand,
        @Autowired orderIdProvider: OrderIdProvider,
        @Autowired stock: Stock
    ) {
        require(productItems.isNotEmpty()) { "Empty shopping cart cannot be ordered." }

        val orderId = orderIdProvider.generateOrderId()

        AggregateLifecycle.apply(
            ShoppingCartOrderedEvent(
                shoppingCartId = command.shoppingCartId,
                orderId = orderId
            )
        )

        AggregateLifecycle.createNew(Order::class.java) {
            Order(
                id = orderId,
                productItems = productItems,
                stock = stock
            )
        }
    }

    @EventSourcingHandler
    fun on(event: ShoppingCartOrderedEvent) {
        AggregateLifecycle.markDeleted()
    }
}
