package round

import Game
import org.bukkit.ChatColor
import java.util.*

class BuildRound(game: Game) : AbstractBuildRound(game) {
	override fun postRoomsBuild() {}

	/* display */

	override fun splashText(uuid: UUID): Pair<String, String> {
		val previous = game.previousRound() as AbstractTextRound
		return Pair("${ChatColor.AQUA}Build!", previous.prompts[game.playersRoom(uuid)])
	}

	override fun barText(): Pair<String, String> {
		return Pair("Build", "Builds Complete")
	}

	override fun reminderText(uuid: UUID, reminderIndex: Int): String {
		return if (reminderIndex % 3 == 1 && !donePlayers.contains(uuid)) {
			"Use /done when you are finished"

		} else {
			val previous = game.previousRound() as AbstractTextRound
			"Prompt: ${previous.prompts[game.playersRoom(uuid)]}"
		}
	}

	override fun tourText(tourData: TourData): Pair<String, String> {
		return Pair("${tourData.name}'s build for",
			(game.rounds[tourData.row - 1] as AbstractTextRound).prompts[tourData.col]
		)
	}
}
