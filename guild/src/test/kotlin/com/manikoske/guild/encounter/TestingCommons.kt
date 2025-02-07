package com.manikoske.guild.encounter

import com.manikoske.guild.action.Effect
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.instantiator.Instantiator
import com.navercorp.fixturemonkey.kotlin.*
import com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy
import com.navercorp.fixturemonkey.kotlin.introspector.KotlinAndJavaCompositeArbitraryIntrospector

object TestingCommons {

    /**
     *
     *    (1) --2-- (2) --2-- (3)
     *     |                   |
     *     1                   1
     *     |                   |
     *    (4) --1-- (5) --1-- (6)
     *     |                   |
     *     1                   1
     *     |                   |
     *    (7) --2-- (8) --2-- (9)
     *
     */

    val bigBattleground = Battleground(
        nodes = setOf(
            Battleground.Node(
                id = 1,
                capacity = 3,
                paths = setOf(
                    Battleground.Path(cost = 1, toNodeId = 4),
                    Battleground.Path(cost = 2, toNodeId = 2)
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(toNodeId = 1, range = 0),
                    Battleground.LineOfSight(toNodeId = 2, range = 1),
                    Battleground.LineOfSight(toNodeId = 3, range = 2),
                    Battleground.LineOfSight(toNodeId = 4, range = 1),
                    Battleground.LineOfSight(toNodeId = 5, range = 2),
                    Battleground.LineOfSight(toNodeId = 6, range = 3),
                    Battleground.LineOfSight(toNodeId = 7, range = 2),
                    Battleground.LineOfSight(toNodeId = 8, range = 3),
                    Battleground.LineOfSight(toNodeId = 9, range = 4),
                )
            ),
            Battleground.Node(
                id = 2,
                capacity = 3,
                paths = setOf(
                    Battleground.Path(cost = 2, toNodeId = 1),
                    Battleground.Path(cost = 2, toNodeId = 3)
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(toNodeId = 1, range = 1),
                    Battleground.LineOfSight(toNodeId = 2, range = 0),
                    Battleground.LineOfSight(toNodeId = 3, range = 1),
                    Battleground.LineOfSight(toNodeId = 4, range = 2),
                    Battleground.LineOfSight(toNodeId = 5, range = 1),
                    Battleground.LineOfSight(toNodeId = 6, range = 2),
                    Battleground.LineOfSight(toNodeId = 7, range = 3),
                    Battleground.LineOfSight(toNodeId = 8, range = 2),
                    Battleground.LineOfSight(toNodeId = 9, range = 3),
                )
            ),
            Battleground.Node(
                id = 3,
                capacity = 3,
                paths = setOf(
                    Battleground.Path(cost = 1, toNodeId = 6),
                    Battleground.Path(cost = 2, toNodeId = 2)
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(toNodeId = 1, range = 2),
                    Battleground.LineOfSight(toNodeId = 2, range = 1),
                    Battleground.LineOfSight(toNodeId = 3, range = 0),
                    Battleground.LineOfSight(toNodeId = 4, range = 3),
                    Battleground.LineOfSight(toNodeId = 5, range = 2),
                    Battleground.LineOfSight(toNodeId = 6, range = 1),
                    Battleground.LineOfSight(toNodeId = 7, range = 4),
                    Battleground.LineOfSight(toNodeId = 8, range = 3),
                    Battleground.LineOfSight(toNodeId = 9, range = 2),
                )
            ),
            Battleground.Node(
                id = 4,
                capacity = 5,
                paths = setOf(
                    Battleground.Path(cost = 1, toNodeId = 1),
                    Battleground.Path(cost = 1, toNodeId = 5),
                    Battleground.Path(cost = 1, toNodeId = 7),
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(toNodeId = 1, range = 1),
                    Battleground.LineOfSight(toNodeId = 2, range = 2),
                    Battleground.LineOfSight(toNodeId = 3, range = 3),
                    Battleground.LineOfSight(toNodeId = 4, range = 0),
                    Battleground.LineOfSight(toNodeId = 5, range = 1),
                    Battleground.LineOfSight(toNodeId = 6, range = 2),
                    Battleground.LineOfSight(toNodeId = 7, range = 1),
                    Battleground.LineOfSight(toNodeId = 8, range = 2),
                    Battleground.LineOfSight(toNodeId = 9, range = 3),
                )
            ),
            Battleground.Node(
                id = 5,
                capacity = 2,
                paths = setOf(
                    Battleground.Path(cost = 1, toNodeId = 4),
                    Battleground.Path(cost = 1, toNodeId = 6)
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(toNodeId = 1, range = 2),
                    Battleground.LineOfSight(toNodeId = 2, range = 1),
                    Battleground.LineOfSight(toNodeId = 3, range = 2),
                    Battleground.LineOfSight(toNodeId = 4, range = 1),
                    Battleground.LineOfSight(toNodeId = 5, range = 0),
                    Battleground.LineOfSight(toNodeId = 6, range = 1),
                    Battleground.LineOfSight(toNodeId = 7, range = 2),
                    Battleground.LineOfSight(toNodeId = 8, range = 1),
                    Battleground.LineOfSight(toNodeId = 9, range = 2),
                )
            ),
            Battleground.Node(
                id = 6,
                capacity = 5,
                paths = setOf(
                    Battleground.Path(cost = 1, toNodeId = 3),
                    Battleground.Path(cost = 1, toNodeId = 5),
                    Battleground.Path(cost = 1, toNodeId = 9),
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(toNodeId = 1, range = 3),
                    Battleground.LineOfSight(toNodeId = 2, range = 2),
                    Battleground.LineOfSight(toNodeId = 3, range = 1),
                    Battleground.LineOfSight(toNodeId = 4, range = 2),
                    Battleground.LineOfSight(toNodeId = 5, range = 1),
                    Battleground.LineOfSight(toNodeId = 6, range = 0),
                    Battleground.LineOfSight(toNodeId = 7, range = 3),
                    Battleground.LineOfSight(toNodeId = 8, range = 2),
                    Battleground.LineOfSight(toNodeId = 9, range = 1),
                )
            ),
            Battleground.Node(
                id = 7,
                capacity = 3,
                paths = setOf(
                    Battleground.Path(cost = 1, toNodeId = 4),
                    Battleground.Path(cost = 2, toNodeId = 8)
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(toNodeId = 1, range = 1),
                    Battleground.LineOfSight(toNodeId = 2, range = 3),
                    Battleground.LineOfSight(toNodeId = 3, range = 4),
                    Battleground.LineOfSight(toNodeId = 4, range = 1),
                    Battleground.LineOfSight(toNodeId = 5, range = 2),
                    Battleground.LineOfSight(toNodeId = 6, range = 3),
                    Battleground.LineOfSight(toNodeId = 7, range = 0),
                    Battleground.LineOfSight(toNodeId = 8, range = 1),
                    Battleground.LineOfSight(toNodeId = 9, range = 2),
                )
            ),

            Battleground.Node(
                id = 8,
                capacity = 3,
                paths = setOf(
                    Battleground.Path(cost = 2, toNodeId = 7),
                    Battleground.Path(cost = 2, toNodeId = 9)
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(toNodeId = 1, range = 3),
                    Battleground.LineOfSight(toNodeId = 2, range = 2),
                    Battleground.LineOfSight(toNodeId = 3, range = 3),
                    Battleground.LineOfSight(toNodeId = 4, range = 2),
                    Battleground.LineOfSight(toNodeId = 5, range = 1),
                    Battleground.LineOfSight(toNodeId = 6, range = 2),
                    Battleground.LineOfSight(toNodeId = 7, range = 1),
                    Battleground.LineOfSight(toNodeId = 8, range = 0),
                    Battleground.LineOfSight(toNodeId = 9, range = 1),
                )
            ),
            Battleground.Node(
                id = 9,
                capacity = 3,
                paths = setOf(
                    Battleground.Path(cost = 1, toNodeId = 6),
                    Battleground.Path(cost = 2, toNodeId = 8)
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(toNodeId = 1, range = 4),
                    Battleground.LineOfSight(toNodeId = 2, range = 3),
                    Battleground.LineOfSight(toNodeId = 3, range = 2),
                    Battleground.LineOfSight(toNodeId = 4, range = 3),
                    Battleground.LineOfSight(toNodeId = 5, range = 2),
                    Battleground.LineOfSight(toNodeId = 6, range = 1),
                    Battleground.LineOfSight(toNodeId = 7, range = 2),
                    Battleground.LineOfSight(toNodeId = 8, range = 1),
                    Battleground.LineOfSight(toNodeId = 9, range = 0),
                )
            ),
        ),
    )

    /**
     *
     *    (1) <-1-----2-> (2) <-2-----1-> (3)
     *
     */

    val smallBattleground = Battleground(
        nodes = setOf(
            Battleground.Node(
                id = 1,
                capacity = 5,
                paths = setOf(
                    Battleground.Path(cost = 2, toNodeId = 2)
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(
                        toNodeId = 1,
                        range = 0
                    ),
                    Battleground.LineOfSight(
                        toNodeId = 2,
                        range = 1
                    ),
                )
            ),
            Battleground.Node(
                id = 2,
                capacity = 3,
                paths = setOf(
                    Battleground.Path(cost = 1, toNodeId = 1),
                    Battleground.Path(cost = 1, toNodeId = 3),
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(
                        toNodeId = 1,
                        range = 1
                    ),
                    Battleground.LineOfSight(
                        toNodeId = 2,
                        range = 0
                    ),
                    Battleground.LineOfSight(
                        toNodeId = 3,
                        range = 1
                    ),
                )
            ),
            Battleground.Node(
                id = 3,
                capacity = 5,
                paths = setOf(
                    Battleground.Path(cost = 2, toNodeId = 2)
                ),
                lineOfSight = setOf(
                    Battleground.LineOfSight(
                        toNodeId = 2,
                        range = 1
                    ),
                    Battleground.LineOfSight(
                        toNodeId = 3,
                        range = 0
                    ),
                )
            ),
        ),
    )



    val randomBuilder = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
//        .objectIntrospector(KotlinAndJavaCompositeArbitraryIntrospector())
//        .register(Effect.ActionForcingEffect.Dying::class.java) {
//            it.giveMeBuilder<Effect.ActionForcingEffect.Dying>().instantiateBy { constructor<Effect.ActionForcingEffect.Dying>() }
//        }
        .build()


}