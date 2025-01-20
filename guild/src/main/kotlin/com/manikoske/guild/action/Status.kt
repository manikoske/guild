package com.manikoske.guild.action

import kotlin.math.max


sealed interface Status {

    val name: String
    val roundsLeft: Int

    sealed interface MovementAlteringStatus : Status {
        fun altersActionMovement(movement: Movement): Movement

    }

    sealed interface MovementProhibitingStatus : Status {
        fun prohibitsActionMovement(movement: Movement): Movement

    }

    sealed interface ActionLimitingStatus : Status {
        fun limitsActionExecution(action: Action): Boolean
    }

    data class Stun(
        override val roundsLeft: Int
    ) : ActionLimitingStatus {
        override val name: String
            get() = "Stun"

        override fun limitsActionExecution(action: Action): Boolean {
            return true
        }
    }

    data class Disarmed(
        override val roundsLeft: Int
    ) : ActionLimitingStatus {
        override val name: String
            get() = "Disarmed"

        override fun limitsActionExecution(action: Action): Boolean {
            return action is Action.WeaponAction
        }
    }

    data class Silence(
        override val roundsLeft: Int
    ) : ActionLimitingStatus {
        override val name: String
            get() = "Silence"

        override fun limitsActionExecution(action: Action): Boolean {
            return action is Action.SpellAction
        }
    }

    data class Entangled(
        override val roundsLeft: Int
    ) : MovementProhibitingStatus {
        override val name: String
            get() = "Entangled"

        override fun prohibitsActionMovement(movement: Movement): Movement {
            return movement.let { if (it.type == Movement.Type.Normal) it.copy(amount = 0) else it }
        }
    }

    data class Prone(
        override val roundsLeft: Int
    ) : MovementProhibitingStatus {
        override val name: String
            get() = "Prone"

        override fun prohibitsActionMovement(movement: Movement): Movement {
            return movement.copy(amount = 0)
        }
    }

    data class Slow(
        override val roundsLeft: Int
    ) : MovementAlteringStatus {
        override val name: String
            get() = "Slow"

        override fun altersActionMovement(movement: Movement): Movement {
            return movement.let { it.copy(amount = max(it.amount - 1, 0)) }
        }
    }

    data class Haste(
        override val roundsLeft: Int
    ) : MovementAlteringStatus {
        override val name: String
            get() = "Haste"

        override fun altersActionMovement(movement: Movement): Movement {
            return movement.let { it.copy(amount = max(it.amount + 1, 0)) }
        }
    }


    data class DamageOverTime(
        override val name: String,
        override val roundsLeft: Int,
        val damageRoll: () -> Int,
    ) : Status {
    }


}