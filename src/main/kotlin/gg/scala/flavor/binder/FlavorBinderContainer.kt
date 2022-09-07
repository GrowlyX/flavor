package gg.scala.flavor.binder

import gg.scala.flavor.FlavorBinder

/**
 * @author GrowlyX
 * @since 9/6/2022
 */
abstract class FlavorBinderContainer
{
    internal val binders = mutableListOf<FlavorBinder<*>>()

    abstract fun populate()

    fun bind(any: Any) = FlavorBinderMultiType(this)
}
