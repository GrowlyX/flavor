package gg.scala.flavor.inject

/**
 * @author GrowlyX
 * @since 1/2/2022
 */
@Retention
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
annotation class Inject
