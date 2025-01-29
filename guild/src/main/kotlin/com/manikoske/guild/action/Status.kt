package com.manikoske.guild.action

import kotlin.math.max


sealed interface Status {

    val name: String
    val category: Category
    val severity: Int


    fun allowsAction(action: Action) : Boolean {
        return true
    }

    sealed interface MovementAlteringStatus : Status {
        fun altersActionMovement(movement: Movement): Movement

        override val category: Category
            get() = Category.MovementAlteration

    }

    sealed interface MovementProhibitingStatus : Status {
        fun prohibitsActionMovement(movement: Movement): Movement

        override val category: Category
            get() = Category.Root

    }

    sealed interface DamageOverTimeStatus : Status {
        fun damageRoll(): () -> Int
    }

    sealed interface RemovedOnDamageTakenStatus : Status

    sealed interface HasRoundsLeftStatus : Status {
        fun roundsLeft() : Int
    }

    enum class Category {
        Debilitating, Dying, Disarmed, Silence, Root, MovementAlteration, Bleed, Poison, Heal
    }

    data class Stun(
        val roundsLeft: Int
    ) : HasRoundsLeftStatus {
        override fun roundsLeft(): Int {
            return roundsLeft
        }

        override val name: String
            get() = "Stun"
        override val category: Category
            get() = Category.Debilitating
        override val severity: Int
            get() = 1

        override fun allowsAction(action: Action): Boolean {
            return false
        }
    }

    data class Dying(
        val tries : Int
    ) : Status {
        override val name: String
            get() = "Dying"

        override val category: Category
            get() = Category.Dying

        override fun allowsAction(action: Action): Boolean {
            return action is Action.FightForLife
        }
    }

    data class Disarmed(
        val roundsLeft: Int
    ) : HasRoundsLeftStatus {
        override fun roundsLeft(): Int {
            return roundsLeft
        }

        override val name: String
            get() = "Disarmed"

        override fun allowsAction(action: Action): Boolean {
            return action !is Action.WeaponAction
        }

        override val category: Category
            get() = Category.Disarmed
    }

    data class Silence(
        val roundsLeft: Int
    ) : HasRoundsLeftStatus {
        override fun roundsLeft(): Int {
            return roundsLeft
        }

        override val name: String
            get() = "Silence"

        override fun allowsAction(action: Action): Boolean {
            return action !is Action.SpellAction
        }

        override val category: Category
            get() = Category.Silence
    }

    data class Entangled(
        val roundsLeft: Int
    ) : MovementProhibitingStatus, HasRoundsLeftStatus, RemovedOnDamageTakenStatus {
        override fun roundsLeft(): Int {
            return roundsLeft
        }

        override val name: String
            get() = "Entangled"

        override fun prohibitsActionMovement(movement: Movement): Movement {
            return movement.let { if (it.type == Movement.Type.Normal) it.copy(amount = 0) else it }
        }

    }

    data class Slow(
        val roundsLeft: Int
    ) : MovementAlteringStatus, HasRoundsLeftStatus {
        override fun roundsLeft(): Int {
            return roundsLeft
        }

        override val name: String
            get() = "Slow"

        override fun altersActionMovement(movement: Movement): Movement {
            return movement.let { it.copy(amount = max(it.amount - 1, 0)) }
        }
    }

    data class Haste(
        val roundsLeft: Int
    ) : MovementAlteringStatus, HasRoundsLeftStatus {
        override fun roundsLeft(): Int {
            return roundsLeft
        }

        override val name: String
            get() = "Haste"

        override fun altersActionMovement(movement: Movement): Movement {
            return movement.let { it.copy(amount = max(it.amount + 1, 0)) }
        }
    }


    data class Bleed(
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
            get() = Category.Bleed
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