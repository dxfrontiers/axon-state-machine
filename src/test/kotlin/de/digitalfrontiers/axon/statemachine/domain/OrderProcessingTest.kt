package de.digitalfrontiers.axon.statemachine.domain

import assertk.fail
import io.mockk.junit5.MockKExtension
import org.axonframework.test.saga.SagaTestFixture
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation::class)
class OrderProcessingTest {

    val fixture: SagaTestFixture<OrderProcessing> by lazy {
        SagaTestFixture(OrderProcessing::class.java).apply {
            withTransienceCheckDisabled()
        }
    }

    val orderId = UUID.randomUUID()
    val orderPlacedEvent = OrderPlacedEvent(
        orderId = orderId,
        productItems = mapOf(
            UUID.randomUUID() to 2,
            UUID.randomUUID() to 1
        ),
        totalPrice = 42.1
    )

    @Test
    @Order(1)
    fun `order denied due to insufficient credits`() {
        fixture.setCallbackBehavior { commandPayload, _ ->
            when (commandPayload) {
                is ReserveCreditCommand -> false
                is DenyOrderCommand -> Unit
                else -> fail("unexpected command")
            }
        }

        fixture
            .whenAggregate(orderId.toString())
            .publishes(orderPlacedEvent)
            .expectActiveSagas(0)
            .expectDispatchedCommands(
                ReserveCreditCommand(amount = orderPlacedEvent.totalPrice),
                DenyOrderCommand(orderId = orderId, reason = "insufficient credits")
            )
            .expectPublishedEvents(
                // no saga events expected
            )
    }

    @Test
    @Order(2)
    fun `credit successfully reserved`() {
        fixture.setCallbackBehavior { commandPayload, _ ->
            when (commandPayload) {
                is ReserveCreditCommand -> true
                else -> fail("unexpected command")
            }
        }

        fixture
            .whenAggregate(orderId.toString())
            .publishes(orderPlacedEvent)
            .expectActiveSagas(1)
            .expectDispatchedCommands(
                ReserveCreditCommand(amount = orderPlacedEvent.totalPrice)
            )
            .expectPublishedEvents(
                CreditReservedEvent(
                    orderId = orderId,
                    amount = orderPlacedEvent.totalPrice
                )
            )
    }

    @Test
    @Order(3)
    fun `invoice requested`() {
        val invoiceId = UUID.randomUUID()

        fixture.setCallbackBehavior { commandPayload, _ ->
            when (commandPayload) {
                is ReserveCreditCommand -> true
                is SendInvoiceCommand -> invoiceId
                else -> fail("unexpected command")
            }
        }

        fixture
            .givenAggregate(orderId.toString())
            .published(orderPlacedEvent)
            .whenPublishingA(
                CreditReservedEvent(
                    orderId = orderId,
                    amount = orderPlacedEvent.totalPrice
                )
            )
            .expectDispatchedCommands(
                SendInvoiceCommand(
                    orderId = orderId,
                    productItems = orderPlacedEvent.productItems,
                    totalPrice = orderPlacedEvent.totalPrice
                )
            )
            .expectAssociationWith("invoiceId", invoiceId)
            .expectPublishedEvents(
                InvoiceRequestedEvent(invoiceId = invoiceId)
            )
    }

    @Test
    @Order(4)
    fun `shipment requested`() {
        val invoiceId = UUID.randomUUID()
        val shipmentId = UUID.randomUUID()

        fixture.setCallbackBehavior { commandPayload, _ ->
            when (commandPayload) {
                is ReserveCreditCommand -> true
                is SendInvoiceCommand -> invoiceId
                is ShipItemsCommand -> shipmentId
                else -> fail("unexpected command")
            }
        }

        fixture
            .givenAggregate(orderId.toString())
            .published(orderPlacedEvent)
            .andThenAPublished(
                CreditReservedEvent(
                    orderId = orderId,
                    amount = orderPlacedEvent.totalPrice
                )
            )
            .whenPublishingA(
                InvoiceRequestedEvent(invoiceId = invoiceId)
            )
            .expectDispatchedCommands(
                ShipItemsCommand(productItems = orderPlacedEvent.productItems)
            )
            .expectAssociationWith("shipmentId", shipmentId)
            .expectPublishedEvents(
                ShipmentRequestedEvent(shipmentId = shipmentId)
            )
    }

    @Test
    @Order(5)
    fun `invoice paid shipment pending`() {
        val invoiceId = UUID.randomUUID()
        val shipmentId = UUID.randomUUID()

        fixture.setCallbackBehavior { commandPayload, _ ->
            when (commandPayload) {
                is ReserveCreditCommand -> true
                is SendInvoiceCommand -> invoiceId
                is ShipItemsCommand -> shipmentId
                else -> fail("unexpected command")
            }
        }

        fixture
            .givenAggregate(orderId.toString())
            .published(orderPlacedEvent)
            .andThenAPublished(
                CreditReservedEvent(
                    orderId = orderId,
                    amount = orderPlacedEvent.totalPrice
                )
            )
            .andThenAPublished(
                InvoiceRequestedEvent(invoiceId = invoiceId)
            )
            .andThenAPublished(
                ShipmentRequestedEvent(shipmentId = shipmentId)
            )
            .whenPublishingA(
                InvoicePaidEvent(invoiceId = invoiceId)
            )
            .expectActiveSagas(1)
            .expectNoDispatchedCommands()
            .expectPublishedEvents(
                // no saga events expected
            )
    }

    @Test
    @Order(6)
    fun `shipment delivered invoice payment due`() {
        val invoiceId = UUID.randomUUID()
        val shipmentId = UUID.randomUUID()

        fixture.setCallbackBehavior { commandPayload, _ ->
            when (commandPayload) {
                is ReserveCreditCommand -> true
                is SendInvoiceCommand -> invoiceId
                is ShipItemsCommand -> shipmentId
                else -> fail("unexpected command")
            }
        }

        fixture
            .givenAggregate(orderId.toString())
            .published(orderPlacedEvent)
            .andThenAPublished(
                CreditReservedEvent(
                    orderId = orderId,
                    amount = orderPlacedEvent.totalPrice
                )
            )
            .andThenAPublished(
                InvoiceRequestedEvent(invoiceId = invoiceId)
            )
            .andThenAPublished(
                ShipmentRequestedEvent(shipmentId = shipmentId)
            )
            .whenPublishingA(
                ShipmentDeliveredEvent(shipmentId = shipmentId)
            )
            .expectActiveSagas(1)
            .expectNoDispatchedCommands()
            .expectPublishedEvents(
                // no saga events expected
            )
    }

    @Test
    @Order(7)
    fun `invoice paid then shipment delivered`() {
        val invoiceId = UUID.randomUUID()
        val shipmentId = UUID.randomUUID()

        fixture.setCallbackBehavior { commandPayload, _ ->
            when (commandPayload) {
                is ReserveCreditCommand -> true
                is SendInvoiceCommand -> invoiceId
                is ShipItemsCommand -> shipmentId
                is MarkOrderCompleteCommand -> Unit
                else -> fail("unexpected command")
            }
        }

        fixture
            .givenAggregate(orderId.toString())
            .published(orderPlacedEvent)
            .andThenAPublished(
                CreditReservedEvent(
                    orderId = orderId,
                    amount = orderPlacedEvent.totalPrice
                )
            )
            .andThenAPublished(
                InvoiceRequestedEvent(invoiceId = invoiceId)
            )
            .andThenAPublished(
                ShipmentRequestedEvent(shipmentId = shipmentId)
            )
            .andThenAPublished(
                InvoicePaidEvent(invoiceId = invoiceId)
            )
            .whenPublishingA(
                ShipmentDeliveredEvent(shipmentId = shipmentId)
            )
            .expectActiveSagas(0)
            .expectDispatchedCommands(
                MarkOrderCompleteCommand(
                    orderId = orderId,
                    invoiceId = invoiceId,
                    shipmentId = shipmentId
                )
            )
            .expectPublishedEvents(
                // no saga events expected
            )
    }

    @Test
    @Order(7)
    fun `shipment delivered then invoice paid`() {
        val invoiceId = UUID.randomUUID()
        val shipmentId = UUID.randomUUID()

        fixture.setCallbackBehavior { commandPayload, _ ->
            when (commandPayload) {
                is ReserveCreditCommand -> true
                is SendInvoiceCommand -> invoiceId
                is ShipItemsCommand -> shipmentId
                is MarkOrderCompleteCommand -> Unit
                else -> fail("unexpected command")
            }
        }

        fixture
            .givenAggregate(orderId.toString())
            .published(orderPlacedEvent)
            .andThenAPublished(
                CreditReservedEvent(
                    orderId = orderId,
                    amount = orderPlacedEvent.totalPrice
                )
            )
            .andThenAPublished(
                InvoiceRequestedEvent(invoiceId = invoiceId)
            )
            .andThenAPublished(
                ShipmentRequestedEvent(shipmentId = shipmentId)
            )
            .andThenAPublished(
                ShipmentDeliveredEvent(shipmentId = shipmentId)
            )
            .whenPublishingA(
                InvoicePaidEvent(invoiceId = invoiceId)
            )
            .expectActiveSagas(0)
            .expectDispatchedCommands(
                MarkOrderCompleteCommand(
                    orderId = orderId,
                    invoiceId = invoiceId,
                    shipmentId = shipmentId
                )
            )
            .expectPublishedEvents(
                // no saga events expected
            )
    }
}
