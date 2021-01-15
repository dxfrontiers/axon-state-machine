package de.digitalfrontiers.axon.statemachine.domain

import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericEventMessage

fun <T> EventBus.publishEvent(event: T) =
    GenericEventMessage
        .asEventMessage<T>(event)
        .let { publish(it) }
