package gg.scala.flavor

import java.util.*
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 1/2/2022
 */
fun KClass<*>.getBasePackage(): String
{
    val splitPackageName = this.java.`package`
        .name.split(".")

    return splitPackageName.take(
        if (splitPackageName.size >= 3) 3 else 2
    ).joinToString(".")
}

fun KClass<*>.getAllClasses(): List<Class<*>>
{
    return getLoadedClasses()
        .filter {
            it.`package` != null && it.`package`.name.startsWith(this.getBasePackage())
        }
}

fun getLoadedClasses(): Vector<Class<*>>
{
    val classLoader = Thread.currentThread().contextClassLoader
    val classesField = ClassLoader::class.java.getDeclaredField("classes")
        .apply {
            this.isAccessible = true
        }

    return (classesField.get(classLoader) as Vector<Class<*>>).clone() as Vector<Class<*>>
}