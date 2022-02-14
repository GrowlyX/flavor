package gg.scala.flavor

import java.util.logging.Logger

/**
 * Contains several configuration options for a flavor instance.
 * 
 * @author GrowlyX
 * @since 1/2/2022
 */
data class FlavorOptions(
    val logger: Logger = Logger.getAnonymousLogger()
)
