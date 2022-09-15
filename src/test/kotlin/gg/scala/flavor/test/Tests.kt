package gg.scala.flavor.test

import gg.scala.flavor.Flavor
import gg.scala.flavor.binder.FlavorBinderContainer
import gg.scala.flavor.inject.Extract
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import org.junit.jupiter.api.Test
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Named

/**
 * @author GrowlyX
 * @since 9/14/2022
 */
class Tests
{

    @Service
    object AsfService
    {
        @Inject
        @Named("horse")
        lateinit var hors: String

        @PostConstruct
        fun postConstruct()
        {

        }

        @PreDestroy
        fun preDestroy()
        {
            println("rip")
        }
    }

    @Test
    fun test()
    {
        val flavor = Flavor.create<Tests>()

        flavor.inherit(object : FlavorBinderContainer()
        {
            override fun populate()
            {
                bind("Horse")
                    .populate {
                        annotated<Named> { it.value == "horse" }
                    }
                    .to<String>()
            }

            @Extract
            fun hey(): String
            {
                return "Hey"
            }
        })

        class Injected @Inject constructor(
            @Named("horse")
            val horseString: String,
            val heyString: String
        )
        {
            @Inject
            @Named("horse")
            fun setHorse(horse: String)
            {
                println(horse)
            }
        }

        println(
            flavor.injected<Injected>().horseString
        )
    }

}
