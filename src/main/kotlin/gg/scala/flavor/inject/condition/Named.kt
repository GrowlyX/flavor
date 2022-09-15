package gg.scala.flavor.inject.condition

/**
 * @author GrowlyX
 * @since 1/2/2022
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class Named(
    val value: String
)
