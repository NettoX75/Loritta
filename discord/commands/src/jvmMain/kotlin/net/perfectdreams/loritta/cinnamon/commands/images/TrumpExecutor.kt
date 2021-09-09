package net.perfectdreams.loritta.cinnamon.commands.images

import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.TwoImagesOptions
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration

class TrumpExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    client,
    "/api/v1/images/trump",
    "trump.gif"
) {
    companion object : CommandExecutorDeclaration(TrumpExecutor::class) {
        override val options = TwoImagesOptions
    }
}