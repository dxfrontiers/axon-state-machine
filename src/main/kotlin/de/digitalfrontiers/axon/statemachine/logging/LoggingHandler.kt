package de.digitalfrontiers.axon.statemachine.logging

import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("logging")
class LoggingHandler {

    @EventHandler
    fun on(event: Any) {
        logger.info(event.toString())
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(LoggingHandler::class.java)
    }
}
