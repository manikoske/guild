package com.manikoske.guild.action

import kotlin.math.max


sealed interface Status {

    val name: String

    sealed class ActionForcingStatus : Status {
        abstract fun severity(): Int
        abstract fun forcedAction() : Action

        data object Prone : ActionForcingStatus() {
            override fun severity(): Int {
                return 3
            }
            override fun forcedAction(): Action {
                return Action.StandUp
            }

            override val name: String
                get() = "Prone"
        }

        data class Stun(
            val roundsLeft: Int
        ) : ActionForcingStatus(), HasRoundsLeftStatus {
            override fun severity(): Int {
                return 2
            }
            override fun forcedAction(): Action {
                return Action.StandUp
            }
            override fun roundsLeft(): Int {
                return roundsLeft
            }
            override val name: String
                get() = "Stun"
        }

        data object Dying : ActionForcingStatus() {
            override fun severity(): Int {
                return 5
            }
            override fun forcedAction(): Action {
                return Action.StandUp
            }
            override val name: String
                get() = "Dying"
        }
    }

    sealed class MovementAlteringStatus : Status {
        abstract fun alterActionMovement(movement: Movement): Movement

        data class Slow(
            val roundsLeft: Int
        ) : MovementAlteringStatus(), HasRoundsLeftStatus {
            override fun roundsLeft(): Int {
                return roundsLeft
            }
            override val name: String
                get() = "Slow"
            override fun alterActionMovement(movement: Movement): Movement {
                return movement.let { it.copy(amount = max(it.amount - 1, 0)) }
            }
        }

        data class Haste(
            val roundsLeft: Int
        ) : MovementAlteringStatus(), HasRoundsLeftStatus {
            override fun roundsLeft(): Int {
                return roundsLeft
            }

            override val name: String
                get() = "Haste"

            override fun alterActionMovement(movement: Movement): Movement {
                return movement.let { it.copy(amount = max(it.amount + 1, 0)) }
            }
        }

    }

    sealed class MovementRestrictingStatus : Status {
        abstract fun restrictActionMovement(movement: Movement): Movement
        abstract fun severity(): Int
        data class Entangled(
            val roundsLeft: Int
        ) : MovementRestrictingStatus(), HasRoundsLeftStatus, RemovedOnDamageTakenStatus {
            override fun roundsLeft(): Int {
                return roundsLeft
            }

            override val name: String
                get() = "Entangled"

            override fun restrictActionMovement(movement: Movement): Movement {
                return movement.let { if (it.type == Movement.Type.Normal) it.copy(amount = 0) else it }
            }

            override fun severity(): Int {
                return 1
            }
        }

        data class Hold(
            val roundsLeft: Int
        ) : MovementRestrictingStatus(), HasRoundsLeftStatus {
            override fun roundsLeft(): Int {
                return roundsLeft
            }
            override val name: String
                get() = "Hold"
            override fun restrictActionMovement(movement: Movement): Movement {
                return movement.copy(amount = 0)
            }

            override fun severity(): Int {
                return 2
            }
        }
    }

    sealed class ActionRestrictingStatus : Status {
        abstract fun restrictedAction(action: Action) : Boolean
        data class Disarmed(
            val roundsLeft: Int
        ) : ActionRestrictingStatus(), HasRoundsLeftStatus {
            override fun roundsLeft(): Int {
                return roundsLeft
            }
            override fun restrictedAction(action: Action): Boolean {
                return action is Action.WeaponAction
            }
            override val name: String
                get() = "Disarmed"
        }

        data class Silenced(
            val roundsLeft: Int
        ) : ActionRestrictingStatus(), HasRoundsLeftStatus {
            override fun roundsLeft(): Int {
                return roundsLeft
            }
            override fun restrictedAction(action: Action): Boolean {
                return action is Action.SpellAction
            }
            override val name: String
                get() = "Silenced"
        }
    }

    sealed class DamageOverTimeStatus : Status {
        abstract fun damageRoll(): () -> Int

        data class Bleed(
            val roundsLeft: Int,
            val damageRoll: () -> Int,
        ) : DamageOverTimeStatus(), HasRoundsLeftStatus {
            override fun roundsLeft(): Int {
                return roundsLeft
            }
            override val name: String
                get() = "Bleed"
            override fun damageRoll(): () -> Int {
                return damageRoll
            }
        }

        data class Poison(
            override val name: String,
            val roundsLeft: Int,
            val damageRoll: () -> Int,
        ) : HasRoundsLeftStatus, DamageOverTimeStatus {
            override fun roundsLeft(): Int {
                return roundsLeft
            }

            override fun damageRoll(): () -> Int {
                return damageRoll
            }

            override val category: Category
                get() = Category.Poison
        }
    }

    sealed interface RemovedOnDamageTakenStatus : Status

    sealed interface HasRoundsLeftStatus : Status {
        fun roundsLeft() : Int
    }



    data class HealOverTime(
        override val name: String,
        val roundsLeft: Int,
        val healRoll: () -> Int,
    ) : HasRoundsLeftStatus {
        override fun roundsLeft(): Int {
            return roundsLeft
        }

        override val category: Category
            get() = Category.Heal
    }


}