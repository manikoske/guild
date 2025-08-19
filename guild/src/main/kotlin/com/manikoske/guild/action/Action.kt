package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.character.Status
import com.manikoske.guild.rules.Dice
import com.manikoske.guild.rules.Die
import com.manikoske.guild.rules.Event
import com.manikoske.guild.rules.Rules

sealed interface Action {

    object Actions {

        val basicAttack = TargetedAction.AttackAction.WeaponAttack.WeaponSingleAttack(
            name = "Basic Attack",
            movement = Movement(type = Movement.Type.Normal, amount = 1),
            resourceCost = 0,
            resolution = Resolution.AttackResolution.WeaponDamageResolution(
                attackRollModifier = 0,
                damageRollMultiplier = 1,
            ),
        )

        val cantrip = TargetedAction.AttackAction.SpellAttack.SpellSingleAttack(
            name = "Cantrip",
            movement = Movement(type = Movement.Type.Normal, amount = 1),
            resourceCost = 0,
            resolution = Resolution.AttackResolution.SpellDamageResolution(
                baseDifficultyClass = 8,
                executorAttributeType = Attribute.Type.intelligence,
                targetAttributeType = Attribute.Type.dexterity,
                damage =  Dice.of(Die.d6),
            ),
            range = 1
        )

        val dash = SelfAction(
            name = "Dash",
            movement = Movement(type = Movement.Type.Normal, amount = 2),
            resourceCost = 0,
        )

        val disengage = SelfAction(
            name = "Disengage",
            movement = Movement(type = Movement.Type.Special, amount = 1),
            resourceCost = 0,
        )

        val noAction = SelfAction(
            name = "No Action",
            movement = Movement(type = Movement.Type.Normal, amount = 0),
            resourceCost = 0,
        )

        val crawl = SelfAction(
            name = "Crawl",
            movement = Movement(type = Movement.Type.Normal, amount = 1),
            resourceCost = 0,
        )

        val standUp = SelfAction(
            name = "Stand up",
            movement = Movement(type = Movement.Type.Normal, amount = 0),
            resourceCost = 0,
            selfResolution = Resolution.SupportResolution.RemoveStatus(Status.Name.Prone)
        )

        val hideInShadows = SelfAction(
            name = "Hide in Shadows",
            movement = Movement(type = Movement.Type.Normal, amount = 1),
            resourceCost = 0,
            selfResolution = Resolution.SupportResolution.AddStatus(Status.StatusFactory.hidden())
        )

        val basicActions = listOf(basicAttack, cantrip, disengage, dash)
    }

    val name: String
    val movement: Movement
    val resourceCost: Int
    val selfResolution : Resolution.SupportResolution?
    val requiredStatus : Status?

    sealed interface Outcome {
        val actionStarted: Event.ActionStarted
        val selfResolutionEvent: Event.ResolutionEvent?
        val actionEnded: Event.ActionEnded
    }

    data class TargetedActionOutcome(
        override val actionStarted: Event.ActionStarted,
        override val selfResolutionEvent: Event.ResolutionEvent?,
        val targetEvents: List<Event.ResolutionEvent>,
        override val actionEnded: Event.ActionEnded
    ) : Outcome

    data class SelfActionOutcome(
        override val actionStarted: Event.ActionStarted,
        override val selfResolutionEvent: Event.ResolutionEvent?,
        override val actionEnded: Event.ActionEnded
    ) : Outcome

    data class SelfAction(
        override val selfResolution: Resolution.SupportResolution? = null,
        override val name: String,
        override val movement: Movement,
        override val resourceCost: Int,
        override val requiredStatus: Status? = null,
    ) : Action {

        fun execute(
            executor: CharacterState,
            newPositionNodeId: Int
        ) : SelfActionOutcome {

            val actionStarted = Rules.startAction(target = executor, actionName = this.name, newPositionNodeId = newPositionNodeId, resourcesSpent = resourceCost)
            val selfResolutionEvent = selfResolution?.resolve(executor = actionStarted.updatedTarget, target = actionStarted.updatedTarget)
            val actionEnded = Rules.endAction(target = selfResolutionEvent?.updatedTarget ?: actionStarted.updatedTarget)

            return SelfActionOutcome(
                actionStarted = actionStarted,
                selfResolutionEvent = selfResolutionEvent,
                actionEnded = actionEnded,
            )
        }
    }

    sealed interface TargetedAction : Action {

        val resolution: Resolution
        override val selfResolution: Resolution.SupportResolution?

        fun canTarget(executor: CharacterState, target: Target): Boolean

        fun execute(
            executor: CharacterState,
            target: Target,
            newPositionNodeId: Int
        ) : TargetedActionOutcome {


            val actionStarted = Rules.startAction(target = executor, actionName = this.name, newPositionNodeId = newPositionNodeId, resourcesSpent = resourceCost)
            val selfResolutionEvent = selfResolution?.resolve(executor = actionStarted.updatedTarget, target = actionStarted.updatedTarget)
            val updatedExecutor = selfResolutionEvent?.updatedTarget ?: actionStarted.updatedTarget
            val targetEvents = target.targetedCharacterStates.map { resolution.resolve(executor = updatedExecutor, target = it) }
            val actionEnded = Rules.endAction(target = updatedExecutor)

            return TargetedActionOutcome(
                actionStarted = actionStarted,
                selfResolutionEvent = selfResolutionEvent,
                targetEvents = targetEvents,
                actionEnded = actionEnded,
            )
        }

        sealed class SupportAction : TargetedAction {

            abstract override val resolution: Resolution.SupportResolution

            sealed class SpellSupportAction : SupportAction() {
                abstract val range: Int

                data class SingleSpellSupport(
                    override val resolution: Resolution.SupportResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val requiredStatus: Status? = null,
                ) : SpellSupportAction() {
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target.type == Target.Type.SingleAlly && target.range <= range
                    }
                }

                data class NodeSpellSupport(
                    override val resolution: Resolution.SupportResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val requiredStatus: Status? = null,
                ) : SpellSupportAction() {
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target.type == Target.Type.NodeAlly && target.range <= range
                    }
                }
            }
        }

        sealed class AttackAction : TargetedAction {

            sealed class WeaponAttack : AttackAction() {

                abstract override val resolution: Resolution.AttackResolution.WeaponDamageResolution

                data class WeaponSingleAttack(
                    override val resolution: Resolution.AttackResolution.WeaponDamageResolution,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val requiredStatus: Status? = null,

                ) : WeaponAttack() {

                    // TODO when target.range == 0 and arms().range() > 0, then return disadvantage instead of boolean
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target.type == Target.Type.SingleEnemy && target.range <= executor.character.arms().range()
                    }
                }

                data class WeaponNodeAttack(
                    override val resolution: Resolution.AttackResolution.WeaponDamageResolution,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val requiredStatus: Status? = null,
                ) : WeaponAttack() {
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target.type == Target.Type.NodeEveryone &&
                                ((target.range == 0 && executor.character.arms().range() == 0) ||
                                        target.range > 0 && executor.character.arms().range() > 0)
                    }
                }

            }

            sealed class SpellAttack : AttackAction() {
                abstract val range: Int
                abstract override val resolution: Resolution.AttackResolution.SpellDamageResolution

                data class SpellSingleAttack(
                    override val resolution: Resolution.AttackResolution.SpellDamageResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val requiredStatus: Status? = null,
                ) : SpellAttack() {

                    // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target.type == Target.Type.SingleEnemy && target.range <= range
                    }
                }

                data class SpellNodeAttack(
                    override val resolution: Resolution.AttackResolution.SpellDamageResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val requiredStatus: Status? = null,
                ) : SpellAttack() {

                    // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target.type == Target.Type.NodeEveryone && target.range <= range
                    }
                }
            }
        }
    }

}