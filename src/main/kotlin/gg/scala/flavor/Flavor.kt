package gg.scala.flavor

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.inject.InjectScope
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import java.lang.reflect.Method
import java.util.logging.Level
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.kotlinProperty
import kotlin.system.exitProcess

/**
 * @author GrowlyX
 * @since 1/2/2022
 */
class Flavor(
    val initializer: KClass<*>,
    val options: FlavorOptions
)
{
    companion object
    {
        /**
         * Creates a new [Flavor] instance using [T]'s [KClass],
         * and the [options], if any are given.
         */
        @JvmStatic
        inline fun <reified T> create(
            options: FlavorOptions = FlavorOptions()
        ): Flavor
        {
            return Flavor(T::class, options)
        }

        @JvmStatic
        fun create(
            initializer: KClass<*>,
            options: FlavorOptions = FlavorOptions()
        ): Flavor
        {
            return Flavor(initializer, options)
        }
    }

    val binders = mutableListOf<FlavorBinder<*>>()
    val services = mutableMapOf<KClass<*>, Any>()

    val scanners =
        mutableMapOf<KClass<out Annotation>, (Method, Any) -> Unit>()

    inline fun <reified T : Annotation> listen(
        noinline lambda: (Method, Any) -> Unit
    )
    {
        scanners[T::class] = lambda
    }

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

    inline fun <reified A : Annotation> findSingletons(): List<Any>
    {
        return initializer
            .getAllClasses(options)
            .mapNotNull { it.kotlin.objectInstance }
            .filter {
                it.javaClass.isAnnotationPresent(A::class.java)
            }
    }

    /**
     * Creates & inject a new
     * instance of [T].
     *
     * @return the injected instance of [T]
     * @throws InstantiationException
     * if instance creation fails
     */
    inline fun <reified T : Any> injected(
        vararg params: Any
    ): T
    {
        val instance = T::class.java.let { clazz ->
            if (params.isEmpty())
            {
                clazz.newInstance()
            } else
            {
                clazz.getConstructor(
                    *params.map { it.javaClass }.toTypedArray()
                ).newInstance(
                    *params.toList().toTypedArray()
                )
            }
        }

        inject(instance)
        return instance
    }

    /**
     * Injects fields into a pre-existing class, [any].
     */
    fun inject(any: Any)
    {
        scanAndInject(any::class, any)
    }

    /**
     * Scans & injects any services and/or singletons (kt objects)
     * that contain fields annotated with [Inject].
     */
    fun startup()
    {
        val classes = initializer.getAllClasses(options)
            .sortedByDescending {
                it.getAnnotation(Service::class.java)
                    ?.priority ?: 1
            }

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

    /**
     * Invokes the `close` method in all registered services. If a
     * service does not have a close method, the service will be skipped.
     */
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
                        service.name.ifEmpty {
                            entry.key.java.simpleName
                        }
                    }] in ${milli}ms."
                }
            } else
            {
                options.logger.info {
                    "[Services] Failed to shutdown [${
                        service.name.ifEmpty {
                            entry.key.java.simpleName
                        }
                    }]!"
                }
            }
        }
    }

    /**
     * Invokes the provided [lambda] while keeping track of
     * the amount of time it took to run in milliseconds.
     *
     * Any exception thrown within the lambda will be printed,
     * and `-1` will be returned.
     */
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

    /**
     * Scans & injects a provided [KClass], along with its
     * singleton instance if there is one.
     */
    private fun scanAndInject(clazz: KClass<*>, instance: Any? = null)
    {
        // use the provided instance, or the singleton
        // we got through KClass#objectInstance.
        val singleton = instance ?: clazz.objectInstance!!

        for (field in clazz.java.declaredFields)
        {
            // making sure this field is annotated with
            // Inject before modifying its value.
            if (field.isAnnotationPresent(Inject::class.java))
            {
                // trying to find [FlavorBinder]s
                // of the field's type
                val bindersOfType = binders
                    .filter { it.kClass.java == field.type }
                    .toMutableList()

                for (flavorBinder in bindersOfType)
                {
                    for (annotation in field.declaredAnnotations)
                    {
                        // making sure if there are any annotation
                        // checks, that the field passes the check
                        flavorBinder.annotationChecks[annotation::class]?.let {
                            val passesCheck = it.invoke(annotation)

                            if (!passesCheck)
                            {
                                bindersOfType.remove(flavorBinder)
                            }
                        }
                    }
                }

                // retrieving the first binder of the field's type
                val binder = bindersOfType.firstOrNull()
                val accessability = field.isAccessible

                binder?.let {
                    // verifying the scope state of the binder
                    if (binder.scope == InjectScope.SINGLETON)
                    {
                        if (instance == null)
                            return@let
                    }

                    field.isAccessible = false
                    field.set(singleton, it.instance)
                    field.isAccessible = accessability
                }
            }
        }

        for (method in clazz.java.declaredMethods)
        {
            val annotations = method.annotations
                .filter { scanners[it::class] != null }

            for (annotation in annotations)
            {
                try
                {
                    scanners[annotation::class]
                        ?.invoke(method, singleton)
                } catch (exception: Exception)
                {
                    options.logger.log(
                        Level.SEVERE,
                        "Error occurred while invoking function",
                        exception
                    )
                }
            }
        }

        // checking if this class is a service
        val isServiceClazz = clazz.java
            .isAnnotationPresent(Service::class.java)

        if (isServiceClazz)
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

            // making sure an exception wasn't thrown
            // while trying to configure the service
            if (milli != -1L)
            {
                options.logger.info {
                    "[Services] Loaded [${
                        service.name.ifEmpty {
                            clazz.java.simpleName
                        }
                    }] in ${milli}ms."
                }
            } else
            {
                options.logger.info {
                    "[Services] Failed to load [${
                        service.name.ifEmpty {
                            clazz.java.simpleName
                        }
                    }]!"
                }
            }
        }
    }
}
