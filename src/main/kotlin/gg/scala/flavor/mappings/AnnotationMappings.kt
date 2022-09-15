package gg.scala.flavor.mappings

/**
 * @author GrowlyX
 * @since 9/14/2022
 */
object AnnotationMappings
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
        ),
        AnnotationType.Extract to listOf(
            gg.scala.flavor.inject.Extract::class.java
        ),
        AnnotationType.PostConstruct to listOf(
            gg.scala.flavor.service.Configure::class.java,
            javax.annotation.PostConstruct::class.java,
        ),
        AnnotationType.PreDestroy to listOf(
            gg.scala.flavor.service.Close::class.java,
            javax.annotation.PreDestroy::class.java,
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
