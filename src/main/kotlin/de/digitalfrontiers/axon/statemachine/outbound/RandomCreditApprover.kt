package de.digitalfrontiers.axon.statemachine.outbound

import de.digitalfrontiers.axon.statemachine.domain.ReserveCreditCommand
import org.axonframework.commandhandling.CommandHandler
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * [CommandHandler] bean that randomly approves [ReserveCreditCommand], thus simulating
 * a third-party system communication.
 */
@Component
class RandomCreditApprover {

    @CommandHandler
    fun handle(command: ReserveCreditCommand) =
        when (Random.nextInt(10)) {
            0 -> false
            else -> true
        }
}
