package gg.scala.flavor.mappings

/**
 * @author GrowlyX
 * @since 9/14/2022
 */
object InjectAnnotationMappings
{
    private val mappings = mutableMapOf(
        AnnotationType.Inject to listOf(
            javax.inject.Inject::class.java,
            jakarta.inject.Inject::class.java,
            gg.scala.flavor.inject.Inject::class.java
        ),
        AnnotationType.Named to listOf(
            javax.inject.Named::class.java,
            jakarta.inject.Named::class.java,
            gg.scala.flavor.inject.condition.Named::class.java
        )
    )

    fun matchesAny(
        type: AnnotationType,
        annotations: Array<Annotation>
    ): Boolean
    {
        val mapping = this.mappings[type]!!
        return annotations.any { it.javaClass in mapping }
    }
}
