package gg.scala.flavor.test

import gg.scala.flavor.Flavor
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import org.junit.jupiter.api.Test

@Service
object ServiceClassFinderTest
{
    @Configure
    fun configure()
    {
        println("hors")
    }
}

class TestWrap
{
    @Test
    fun test()
    {
        Flavor.create<ServiceClassFinderTest>().startup()
    }
}