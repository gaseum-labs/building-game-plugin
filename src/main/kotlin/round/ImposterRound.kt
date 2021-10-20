package round

import Game
import Room
import Round
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*
import kotlin.properties.Delegates

class ImposterRound(game: Game) : AbstractBuildRound(game) {
	lateinit var originalBuilder: UUID
	lateinit var imposter: UUID

	lateinit var prompt: String
	lateinit var corruptedPrompt: String

	override fun postRoomsBuild() {
		originalBuilder = game.gamePlayers[game.grid.access(1, game.imposterRoundIndex())]
		imposter = game.gamePlayers[game.grid.access(3, game.imposterRoundIndex())]

		prompt = (game.rounds[0] as AbstractTextRound).prompts[game.imposterRoundIndex()]
		corruptedPrompt = (game.rounds[2] as AbstractTextRound).prompts[game.imposterRoundIndex()]

		/* copy the original builders build over */

		val oldBuildRoom = game.rooms[1]!![game.imposterRoundIndex()]
		val newBuildRoom = game.currentRooms()[game.imposterPositions.indexOf(originalBuilder)]

		Room.copyArea(game.world, oldBuildRoom, newBuildRoom)

		donePlayers.add(originalBuilder)
	}

	/* would spoil who the original builder is */
	override fun isPlayerReady(uuid: UUID): Boolean = false

	/* display */

	override fun splashText(uuid: UUID): Pair<String, String> {
		return when (uuid) {
			originalBuilder -> Pair("${ChatColor.GOLD}Your Build!", "You're the original builder for this round")
			imposter -> Pair("${ChatColor.RED}You're the imposter!", corruptedPrompt)
			else -> Pair("${ChatColor.GREEN}Innocent!", prompt)
		}
	}

	override fun barText(): Pair<String, String> {
		return Pair("Imposter Build", "Builds Complete")
	}

	override fun reminderText(uuid: UUID, reminderIndex: Int): String {
		return if (uuid == originalBuilder) {
			"Original prompt: $prompt"

		} else if (reminderIndex % 3 == 1 && !donePlayers.contains(uuid)) {
			"Use /done when you are finished"

		} else {
			"Prompt: ${if (uuid == imposter) corruptedPrompt else prompt}"
		}
	}

	override fun tourText(tourData: TourData): Pair<String, String> {
		return Pair("${tourData.name}'s imposter round build for", prompt)
	}
}
