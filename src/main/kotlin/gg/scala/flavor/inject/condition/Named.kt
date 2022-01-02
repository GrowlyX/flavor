package gg.scala.flavor.inject.condition

/**
 * @author GrowlyX
 * @since 1/2/2022
 */
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
annotation class Named(
    val value: String
)
