package net.perfectdreams.loritta.cinnamon.common

import net.perfectdreams.loritta.cinnamon.common.services.Services
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import kotlin.random.Random

/**
 * Represents a Loritta Morenitta implementation.
 *
 * This should be extended by plataform specific Lori's :3
 */
abstract class LorittaBot(val config: LorittaConfig) {
    // TODO: *Really* set a random seed
    val random = Random(0)
    abstract val services: Services
}