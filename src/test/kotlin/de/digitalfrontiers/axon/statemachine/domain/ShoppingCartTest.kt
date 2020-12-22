package de.digitalfrontiers.axon.statemachine.domain

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class ShoppingCartTest {

    val stock = Stock()

    @MockK
    lateinit var orderIdProvider: OrderIdProvider

    val fixture: AggregateTestFixture<ShoppingCart> by lazy {
        AggregateTestFixture(ShoppingCart::class.java).apply {
            registerInjectableResource(orderIdProvider)
            registerInjectableResource(stock)
        }
    }

    val cartId = UUID.randomUUID()
    val productId01: UUID by lazy { stock.productItems().keys.first() }
    val productId02: UUID by lazy { stock.productItems().keys.last() }

    @Test
    fun `created upon first item added`() {
        fixture
            .givenNoPriorActivity()
            .`when`(
                AddToShoppingCartCommand(
                    shoppingCartId = cartId,
                    productItemId = productId01,
                    quantity = 1
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                ShoppingCartCreatedEvent(
                    shoppingCartId = cartId
                ),
                ProductItemAddedEvent(
                    shoppingCartId = cartId,
                    productItemId = productId01,
                    quantity = 1
                )
            )
    }

    @Test
    fun `empty cart cannot be ordered`() {
        fixture
            .givenCommands(
                AddToShoppingCartCommand(
                    shoppingCartId = cartId,
                    productItemId = productId01,
                    quantity = 3
                )
            )
            .andGivenCommands(
                RemoveFromShoppingCartCommand(
                    shoppingCartId = cartId,
                    productItemId = productId01,
                    quantity = 1
                ),
                RemoveFromShoppingCartCommand(
                    shoppingCartId = cartId,
                    productItemId = productId01,
                    quantity = 2
                )
            )
            .`when`(
                PlaceOrderCommand(
                    shoppingCartId = cartId
                )
            )
            .expectNoEvents()
            .expectExceptionMessage("Empty shopping cart cannot be ordered.")
    }

    @Test
    fun `can be fully ordered`() {
        val orderId = UUID.randomUUID()

        every {
            orderIdProvider.generateOrderId()
        } returns orderId

        fixture
            .givenCommands(
                AddToShoppingCartCommand(
                    shoppingCartId = cartId,
                    productItemId = productId01,
                    quantity = 2
                ),
                AddToShoppingCartCommand(
                    shoppingCartId = cartId,
                    productItemId = productId01,
                    quantity = 1
                ),
                AddToShoppingCartCommand(
                    shoppingCartId = cartId,
                    productItemId = productId02,
                    quantity = 7
                ),
                RemoveFromShoppingCartCommand(
                    shoppingCartId = cartId,
                    productItemId = productId02,
                    quantity = 2
                )
            )
            .`when`(
                PlaceOrderCommand(
                    shoppingCartId = cartId
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectMarkedDeleted()
            .expectEvents(
                ShoppingCartOrderedEvent(
                    shoppingCartId = cartId,
                    orderId = orderId
                ),
                OrderPlacedEvent(
                    orderId = orderId,
                    productItems = mapOf(
                        productId01 to 2 + 1,
                        productId02 to 7 - 2
                    ),
                    totalPrice = (stock.productItems().getValue(productId01) * (2 + 1)) +
                        (stock.productItems().getValue(productId02) * (7 - 2))
                )
            )
    }
}
