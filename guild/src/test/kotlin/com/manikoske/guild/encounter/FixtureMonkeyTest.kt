package com.manikoske.guild.encounter

import com.navercorp.fixturemonkey.FixtureMonkeyBuilder
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.customizer.Values
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.pushAssignableTypeArbitraryIntrospector
import org.junit.jupiter.api.Test

class FixtureMonkeyTest {

    sealed class Scheme {
        data class Url(val scheme: String) : Scheme()
        data object None : Scheme()
    }

    data class MyData(
        val id: Int,
        val name: String,
        val scheme: Scheme,
    )

    @Test
    @Seed(30)
    fun fixtureMonkey1() {
        val sampled = FixtureMonkeyBuilder()
            .plugin(KotlinPlugin())
            .build()
            .giveMeKotlinBuilder<MyData>()
            .set(MyData::scheme, Scheme.None) // new line of code
            .sample()

        println("sampled: $sampled")
    }

    @Test
    @Seed(30)
    fun fixtureMonkey2() {
        val sampled = FixtureMonkeyBuilder()
            .plugin(KotlinPlugin())
            .pushAssignableTypeArbitraryIntrospector<Scheme> {
                ArbitraryIntrospectorResult(CombinableArbitrary.from(Scheme.None))
            }
            .build()
            .giveMeKotlinBuilder<MyData>()
            .sample()

        println("sampled: $sampled")
    }

    @Test
    @Seed(30)
    fun fixtureMonkey3() {
        val sampled = FixtureMonkeyBuilder()
            .plugin(KotlinPlugin())
            .build()
            .giveMeKotlinBuilder<MyData>()
            .set(MyData::scheme, Values.just(Scheme.None)) // new line of code
            .sample()

        println("sampled: $sampled")
    }

    data class MyData2(
        val roll : () -> Int
    )

    @Test
    @Seed(30)
    fun test() {
        val sampled = FixtureMonkeyBuilder()
            .plugin(KotlinPlugin())
            .build()
            .giveMeKotlinBuilder<MyData2>()
            .sample()

        println("sampled: $sampled")
    }

}