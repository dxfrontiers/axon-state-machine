package de.digitalfrontiers.axon.statemachine.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventBus
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@Saga
@ProcessingGroup("order")
class OrderProcessing {

    @JsonIgnore
    @Autowired
    lateinit var commandGateway: CommandGateway

    @JsonIgnore
    @Autowired
    lateinit var eventBus: EventBus

    var orderId: UUID? = null
    var productItems: Map<UUID, Long>? = null
    var totalPrice: Double? = null

    var paid = false
    var invoiceId: UUID? = null
    var delivered = false
    var shipmentId: UUID? = null

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    fun on(event: OrderPlacedEvent) {
        orderId = event.orderId
        productItems = event.productItems
        totalPrice = event.totalPrice

        when(commandGateway.sendAndWait<Boolean>(ReserveCreditCommand(amount = event.totalPrice))) {
            true -> {
                eventBus.publishEvent(
                    CreditReservedEvent(
                        orderId = event.orderId,
                        amount = event.totalPrice
                    )
                )
            }
            false -> {
                commandGateway.sendAndWait<Unit>(
                    DenyOrderCommand(
                        orderId = event.orderId,
                        reason = "insufficient credits"
                    )
                )
                SagaLifecycle.end()
            }
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    fun on(event: CreditReservedEvent) {
        val invoiceId = commandGateway.sendAndWait<UUID>(
            SendInvoiceCommand(
                orderId = event.orderId,
                productItems = requireNotNull(productItems),
                totalPrice = requireNotNull(totalPrice)
            )
        )
        SagaLifecycle.associateWith("invoiceId", invoiceId.toString())

        eventBus.publishEvent(
            InvoiceRequestedEvent(invoiceId = invoiceId)
        )
    }

    @SagaEventHandler(associationProperty = "invoiceId")
    fun on(event: InvoiceRequestedEvent) {
        val shipmentId = commandGateway.sendAndWait<UUID>(
            ShipItemsCommand(productItems = requireNotNull(productItems))
        )
        SagaLifecycle.associateWith("shipmentId", shipmentId.toString())

        eventBus.publishEvent(
            ShipmentRequestedEvent(shipmentId = shipmentId)
        )
    }

    @SagaEventHandler(associationProperty = "invoiceId")
    fun on(event: InvoicePaidEvent) {
        invoiceId = event.invoiceId
        paid = true

        finishIfNecessary()
    }

    @SagaEventHandler(associationProperty = "shipmentId")
    fun on(event: ShipmentDeliveredEvent) {
        shipmentId = event.shipmentId
        delivered = true

        finishIfNecessary()
    }

    private fun finishIfNecessary() {
        if (paid && delivered) {
            commandGateway.sendAndWait<Unit>(
                MarkOrderCompleteCommand(
                    orderId = requireNotNull(orderId),
                    invoiceId = requireNotNull(invoiceId),
                    shipmentId = requireNotNull(shipmentId)
                )
            )
            SagaLifecycle.end()
        }
    }
}
