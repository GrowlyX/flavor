package gg.scala.flavor.reflections

import gg.scala.flavor.FlavorOptions
import org.reflections.Reflections
import org.reflections.Store
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.Scanners
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.QueryFunction
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 4/15/2022
 */
class PackageIndexer(
    private val clazz: KClass<*>,
    options: FlavorOptions
)
{
    val reflections =
        Reflections(
            ConfigurationBuilder()
                .forPackage(
                    options.`package` ?: this.clazz.java.`package`.name,
                    this.clazz.java.classLoader
                )

                .addScanners(
                    MethodAnnotationsScanner(),
                    TypeAnnotationsScanner()
                )
        )

    inline fun <reified T : Annotation> getMethodsAnnotatedWith(): List<Method>
    {
        return reflections
            .get(annotated<T>())
            .toList()
    }

    inline fun <reified T : Annotation> getTypesAnnotatedWith(): List<Class<*>>
    {
        return reflections
            .getTypesAnnotatedWith(T::class.java)
            .toList()
    }

    inline fun <reified T> annotated(): QueryFunction<Store, Method>
    {
        return Scanners.MethodsAnnotated
            .with(T::class.java)
            .`as`(Method::class.java)
    }
}
