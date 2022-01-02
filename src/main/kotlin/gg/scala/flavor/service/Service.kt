package gg.scala.flavor.service

/**
 * Marks a class as a flavor Service.
 *
 * If the [name] is blank, an identifier
 * will be automatically created.
 *
 * @author GrowlyX
 * @since 1/2/2022
 */
@Target(AnnotationTarget.TYPE)
annotation class Service(
    val name: String = ""
)
