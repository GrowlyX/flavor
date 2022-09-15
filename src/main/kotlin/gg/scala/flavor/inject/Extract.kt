package gg.scala.flavor.inject

/**
 * @author GrowlyX
 * @since 9/14/2022
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER
)
annotation class Extract
