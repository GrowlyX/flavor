package gg.scala.flavor

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.inject.InjectScope
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 1/2/2022
 */
class Flavor(
    private val initializer: KClass<*>,
    private val options: FlavorOptions
)
{
    companion object
    {
        @JvmStatic
        inline fun <reified T> create(
            options: FlavorOptions = FlavorOptions()
        ): Flavor
        {
            return Flavor(T::class, options)
        }
    }

    val binders = mutableListOf<FlavorBinder<*>>()
    val services = mutableMapOf<KClass<*>, Any>()

    /**
     * Searches for & returns a
     * service matching type [T].
     *
     * @return the service
     * @throws RuntimeException if there is
     * no service matching type [T].
     */
    inline fun <reified T> service(): T
    {
        val service = services[T::class]
            ?: throw RuntimeException("A non-service class was provided.")

        return service as T
    }

    /**
     * Creates a new [FlavorBinder] for type [T].
     */
    inline fun <reified T : Any> bind(): FlavorBinder<T>
    {
        val binder = FlavorBinder(T::class)
        binders.add(binder)

        return binder
    }

    /**
     * Creates & inject a new
     * instance of [T].
     *
     * @return the injected instance of [T]
     * @throws InstantiationException
     * if instance creation fails
     */
    inline fun <reified T : Any> injected(): T
    {
        val instance = T::class
            .java.newInstance()

        inject(instance)
        return instance
    }

    /**
     * Injects fields into a pre-existing class, [any].
     */
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

            if (milli != -1L)
            {
                options.logger.info {
                    "[Services] Shutdown [${
                        service?.name ?: entry.key
                            .java.simpleName
                    }] in ${milli}ms."
                }
            } else
            {
                options.logger.info {
                    "[Services] Failed to shutdown [${
                        service?.name ?: entry.key
                            .java.simpleName
                    }]!"
                }
            }
        }
    }

    private fun tracked(lambda: () -> Unit): Long
    {
        val start = System.currentTimeMillis()

        try
        {
            lambda.invoke()
        } catch (exception: Exception)
        {
            exception.printStackTrace()
            return -1
        }

        return System.currentTimeMillis() - start
    }

    private fun scanAndInject(clazz: KClass<*>, instance: Any? = null)
    {
        val singletonRaw = try
        {
            InjectScope.SINGLETON
                .instanceCreator.invoke(clazz)
        } catch (exception: Exception)
        {
            null
        }
        val singleton = instance ?: singletonRaw!!

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
                    if (binder.scope == InjectScope.SINGLETON)
                    {
                        if (singletonRaw == null)
                        {
                            return@let
                        }
                    }

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
            services[clazz] = singleton

            val service = clazz.java
                .getDeclaredAnnotation(Service::class.java)

            val milli = tracked {
                configure?.invoke(singleton)
            }

            if (milli != -1L)
            {
                options.logger.info {
                    "[Services] Loaded [${
                        service?.name ?: clazz
                            .java.simpleName
                    }] in ${milli}ms."
                }
            } else
            {
                options.logger.info {
                    "[Services] Failed to load [${
                        service?.name ?: clazz
                            .java.simpleName
                    }]!"
                }
            }
        }
    }

}
