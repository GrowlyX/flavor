package gg.scala.flavor

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.inject.InjectScope
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import java.util.logging.Logger
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 1/2/2022
 */
class Flavor(
    private val initializer: KClass<*>
)
{
    companion object
    {
        @JvmStatic
        inline fun <reified T> create(): Flavor
        {
            return Flavor(T::class)
        }
    }

    val binders = mutableListOf<FlavorBinder<*>>()
    val services = mutableMapOf<KClass<*>, Any>()

    inline fun <reified T> service(): T
    {
        val service = services[T::class]
            ?: throw RuntimeException("A non-service class was provided.")

        return service as T
    }

    inline fun <reified T : Any> bind(): FlavorBinder<T>
    {
        val binder = FlavorBinder(T::class)
        binders.add(binder)

        return binder
    }

    fun inject(any: Any)
    {
        scanAndInject(any::class)
    }

    fun startup()
    {
        val classes = initializer
            .getAllClasses()

        for (clazz in classes)
        {
            try
            {
                if (!clazz.isAnnotationPresent(IgnoreAutoScan::class.java))
                {
                    scanAndInject(clazz.kotlin)
                }
            } catch (e: Exception)
            {
                continue
            }
        }
    }

    fun close()
    {
        for (entry in services.entries)
        {
            val close = entry.key.java.declaredMethods
                .firstOrNull { it.isAnnotationPresent(Close::class.java) }

            val service = entry.key.java
                .getDeclaredAnnotation(Service::class.java)

            val milli = tracked {
                close?.invoke(entry.value)
            }

            Logger.getAnonymousLogger().info {
                "[Flavor] Shutdown [${
                    service?.name ?: entry.key
                        .java.simpleName
                }] in ${milli}ms."
            }
        }
    }

    fun tracked(lambda: () -> Unit): Long
    {
        val start = System.currentTimeMillis()
        lambda.invoke()

        return System.currentTimeMillis() - start
    }

    private fun scanAndInject(clazz: KClass<*>, instance: Any? = null)
    {
        val singleton = instance ?: InjectScope.SINGLETON
            .instanceCreator.invoke(clazz)

        for (field in clazz.java.fields)
        {
            if (field.isAnnotationPresent(Inject::class.java))
            {
                val kotlinType = field.type.kotlin

                val bindersOfType = binders
                    .filter { it.kClass == kotlinType }
                    .toMutableList()

                for (flavorBinder in bindersOfType)
                {
                    for (annotation in field.annotations)
                    {
                        flavorBinder.annotationChecks[annotation::class]?.let {
                            val passesCheck = it.invoke(annotation)

                            if (!passesCheck)
                            {
                                bindersOfType.remove(flavorBinder)
                            }
                        }
                    }
                }

                val binder = bindersOfType.firstOrNull()
                val accessability = field.isAccessible

                binder?.let {
                    field.isAccessible = false
                    field.set(singleton, it.instance)
                    field.isAccessible = accessability
                }
            }
        }

        val isServiceSingleton = clazz.java
            .isAnnotationPresent(Service::class.java)

        if (isServiceSingleton)
        {
            val configure = clazz.java.declaredMethods
                .firstOrNull { it.isAnnotationPresent(Configure::class.java) }

            // singletons should always be non-null
            services[clazz] = singleton!!

            val service = clazz.java
                .getDeclaredAnnotation(Service::class.java)

            val milli = tracked {
                configure?.invoke(singleton)
            }

            Logger.getAnonymousLogger().info {
                "[Flavor] Loaded [${
                    service.name.ifEmpty {
                        clazz.java.simpleName
                    }
                }] in ${milli}ms."
            }
        }
    }

}
