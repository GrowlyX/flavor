package gg.scala.flavor.test

import gg.scala.flavor.Flavor
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.inject.condition.Named
import org.junit.jupiter.api.Test

/**
 * @author GrowlyX
 * @since 1/11/2022
 */
class FlavorTest
{
    @Test
    fun onInjectorTest()
    {
        val flavor = Flavor.create<FlavorTest>()
        flavor.bind<String>()
            .annotated<Named> {
                it.value == "hello:world"
            } to "hello world"

        val injected = flavor.injected<InjectorTest>(
            "hello", "world"
        )

        println(injected.helloWorld)
    }

    class InjectorTest(
        val hello: String,
        val world: String
    )
    {
        @Inject @Named("hello:world")
        lateinit var helloWorld: String
    }
}
