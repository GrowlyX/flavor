package gg.scala.flavor

import java.io.IOException
import java.security.CodeSource
import java.util.*
import java.util.jar.JarFile
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
    val classes: MutableCollection<Class<*>> = ArrayList()
    val codeSource: CodeSource = java.protectionDomain.codeSource
    val resource = codeSource.location
    val relPath = getBasePackage().replace('.', '/')
    val resPath = resource.path.replace("%20", " ")
    val jarPath = resPath.replaceFirst("[.]jar[!].*".toRegex(), ".jar").replaceFirst("file:".toRegex(), "")

    val jarFile: JarFile = try
    {
        JarFile(jarPath)
    } catch (e: IOException)
    {
        throw RuntimeException("Unexpected IOException reading JAR File '$jarPath'", e)
    }

    val entries = jarFile.entries()

    while (entries.hasMoreElements())
    {
        val entry = entries.nextElement()
        val entryName = entry.name
        var className: String? = null
        if (entryName.endsWith(".class") && entryName.startsWith(relPath) && entryName.length > relPath.length + "/".length)
        {
            className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "")
        }
        if (className != null)
        {
            var clazz: Class<*>? = null
            try
            {
                clazz = Class.forName(className)
            } catch (e: ClassNotFoundException)
            {
                e.printStackTrace()
            }
            if (clazz != null)
            {
                classes.add(clazz)
            }
        }
    }
    try
    {
        jarFile.close()
    } catch (e: IOException)
    {
        e.printStackTrace()
    }

    return classes.toList()
}
