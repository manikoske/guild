package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Class
import com.manikoske.guild.encounter.*
import com.manikoske.guild.encounter.Target
import com.manikoske.guild.rules.Die
import kotlin.collections.plus

sealed interface Action {

    object Actions {

        val noClassRestriction = listOf(Class.Fighter, Class.Rogue, Class.Ranger, Class.Cleric, Class.Wizard)

        val basicActions = listOf(
            TargetedAction.AttackAction.WeaponAttack.WeaponSingleAttack(
                name = "Basic Attack",
                movement = Movement(type = Movement.Type.Normal, amount = 1),
                classRestriction = noClassRestriction,
                resourceCost = 0,
                resolution = Resolution.AttackResolution.WeaponDamageResolution(
                    attackRollModifier = 0,
                    damageRollMultiplier = 1,
                    effectsOnHit = listOf()
                ),
            ),
            TargetedAction.AttackAction.SpellAttack.SpellSingleAttack(
                name = "Cantrip",
                movement = Movement(type = Movement.Type.Normal, amount = 1),
                classRestriction = listOf(Class.Wizard),
                resourceCost = 0,
                resolution = Resolution.AttackResolution.SpellDamageResolution(
                    baseDifficultyClass = 10,
                    executorAttributeType = Attribute.Type.intelligence,
                    targetAttributeType = Attribute.Type.dexterity,
                    damage =  Die.Dice.of(Die.d6),
                    effectsOnHit = listOf()
                ),
                range = 1
            ),
            SelfAction(
                name = "Dash",
                movement = Movement(type = Movement.Type.Normal, amount = 2),
                resourceCost = 0,
                classRestriction = noClassRestriction,
            ),
            SelfAction(
                name = "Disengage",
                movement = Movement(type = Movement.Type.Special, amount = 1),
                resourceCost = 0,
                classRestriction = noClassRestriction,
            ),
        )

        val noAction = SelfAction(
            name = "No Action",
            movement = Movement(type = Movement.Type.Normal, amount = 0),
            resourceCost = 0,
            classRestriction = noClassRestriction,
        )

        val standUp = SelfAction(
            name = "Stand Up",
            movement = Movement(type = Movement.Type.Normal, amount = 0),
            resourceCost = 0,
            classRestriction = noClassRestriction,
            selfResolution = Resolution.SupportResolution.RemoveEffect(
                effect = Effect.ActionForcingEffect.Prone(dummy = 1)
            )
        )

        val fightForLife = SelfAction(
            name = "Fight For Life",
            movement = Movement(type = Movement.Type.Normal, amount = 0),
            resourceCost = 0,
            classRestriction = noClassRestriction,
        )
    }

    val name: String
    val movement: Movement
    val resourceCost: Int
    val classRestriction: List<Class>
    val selfResolution : Resolution.SupportResolution?

    fun canAccess(executor: CharacterState, vantageNode: PointOfView.VantageNode) : Boolean {
        return when (movement.type) {
            Movement.Type.Normal -> executor.canMoveBy(movement, vantageNode.requiredNormalMovement)
            Movement.Type.Special -> executor.canMoveBy(movement, vantageNode.requiredSpecialMovement)
        }
    }

    sealed interface Outcome {
        val actionName: String
        val executor: CharacterState
        val actionStarted: Event.ActionStarted
        val selfResolutionEvent: Event.ResolutionEvent?
        val actionEnded: Event.ActionEnded
    }

    data class TargetedActionOutcome(
        override val actionName: String,
        override val executor: CharacterState,
        val target: Target,
        override val actionStarted: Event.ActionStarted,
        override val selfResolutionEvent: Event.ResolutionEvent?,
        val targetEvents: List<Event.ResolutionEvent>,
        override val actionEnded: Event.ActionEnded
    ) : Outcome

    data class SelfActionOutcome(
        override val actionName: String,
        override val executor: CharacterState,
        override val actionStarted: Event.ActionStarted,
        override val selfResolutionEvent: Event.ResolutionEvent?,
        override val actionEnded: Event.ActionEnded
    ) : Outcome

    data class SelfAction(
        override val selfResolution: Resolution.SupportResolution? = null,
        override val name: String,
        override val movement: Movement,
        override val resourceCost: Int,
        override val classRestriction: List<Class>
    ) : Action {

        fun execute(
            executor: CharacterState,
            newPositionNodeId: Int
        ) : SelfActionOutcome {

            val actionStarted = executor.startAction(newPositionNodeId = newPositionNodeId, resourcesSpent = resourceCost)
            val selfResolutionEvent = selfResolution?.resolve(executor = actionStarted.updatedTarget, target = actionStarted.updatedTarget)
            val actionEnded = selfResolutionEvent?.updatedTarget?.endAction() ?: executor.endAction()

            return SelfActionOutcome(
                actionName = this.name,
                executor = executor,
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


            val actionStarted = executor.startAction(newPositionNodeId = newPositionNodeId, resourcesSpent = resourceCost)
            val selfResolutionEvent = selfResolution?.resolve(executor = actionStarted.updatedTarget, target = actionStarted.updatedTarget)
            val updatedExecutor = selfResolutionEvent?.updatedTarget ?: actionStarted.updatedTarget
            val targetEvents = target.targetedCharacterStates.map { resolution.resolve(executor = updatedExecutor, target = it) }
            val actionEnded = updatedExecutor.endAction()

            return TargetedActionOutcome(
                actionName = this.name,
                executor = executor,
                target = target,
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
                    override val classRestriction: List<Class>
                ) : SpellSupportAction() {
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.SingleAlly && target.range <= range
                    }
                }

                data class DoubleSpellSupport(
                    override val resolution: Resolution.SupportResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>
                ) : SpellSupportAction() {
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.DoubleAlly && target.range <= range
                    }
                }

                data class NodeSpellSupport(
                    override val resolution: Resolution.SupportResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>
                ) : SpellSupportAction() {
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.NodeAlly && target.range <= range
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
                    override val classRestriction: List<Class>,

                ) : WeaponAttack() {

                    // TODO when target.range == 0 and arms().range() > 0, then return disadvantage instead of boolean
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.SingleEnemy && target.range <= executor.character.arms().range()
                    }
                }

                data class WeaponDoubleAttack(
                    override val resolution: Resolution.AttackResolution.WeaponDamageResolution,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : WeaponAttack() {

                    // TODO when target.range == 0 and arms().range() > 0, then return disadvantage instead of boolean
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.DoubleEnemy && target.range <= executor.character.arms().range()
                    }
                }

                data class WeaponNodeAttack(
                    override val resolution: Resolution.AttackResolution.WeaponDamageResolution,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : WeaponAttack() {
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.NodeEveryone &&
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
                    override val classRestriction: List<Class>,
                ) : SpellAttack() {

                    // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.SingleEnemy && target.range <= range
                    }
                }

                data class SpellDoubleAttack(
                    override val resolution: Resolution.AttackResolution.SpellDamageResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : SpellAttack() {

                    // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.DoubleEnemy && target.range <= range
                    }
                }

                data class SpellNodeAttack(
                    override val resolution: Resolution.AttackResolution.SpellDamageResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : SpellAttack() {

                    // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
                    override fun canTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.NodeEveryone && target.range <= range
                    }
                }
            }
        }
    }

}