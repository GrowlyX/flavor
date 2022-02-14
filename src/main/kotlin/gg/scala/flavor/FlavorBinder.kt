package gg.scala.flavor

import gg.scala.flavor.inject.InjectScope
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * A wrapper class to easily
 * create and register a flavor binder.
 *
 * @author GrowlyX
 * @since 1/2/2022
 */
@Suppress("UNCHECKED_CAST")
class FlavorBinder<T : Any>(
    val kClass: KClass<out T>
)
{
    val annotationChecks = mutableMapOf<KClass<out Annotation>, (Annotation) -> Boolean>()

    var instance by Delegates.notNull<Any>()
    var scope = InjectScope.NO_SCOPE

    fun scoped(scope: InjectScope): FlavorBinder<T>
    {
        this.scope = scope
        return this
    }

    infix fun to(any: Any): FlavorBinder<T>
    {
        instance = any
        return this
    }

    inline fun <reified A : Annotation> annotated(
        noinline lambda: (A) -> Boolean
    ): FlavorBinder<T>
    {
        annotationChecks[A::class] = lambda as (Annotation) -> Boolean
        return this
    }
}
