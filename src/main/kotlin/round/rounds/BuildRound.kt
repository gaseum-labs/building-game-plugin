package round.rounds

import game.Game
import round.Room
import org.bukkit.ChatColor
import round.RoomAccess
import round.TourData
import java.util.*

class BuildRound(game: Game, rooms: Array<Room>, index: Int) : AbstractBuildRound(game, rooms, index) {
	override fun postRoomsBuild() {}

	/* display */

	override fun splashText(uuid: UUID): Triple<String, String, String?> {
		return Triple(
			"${ChatColor.AQUA}Build!",
			AbstractTextRound.getSubmitted(RoomAccess.at(game, this, uuid).traverse(-1)),
			"Use /done when you are finished"
		)
	}

	override fun barText(): Pair<String, String> {
		return Pair("Build", "Builds Complete")
	}

	override fun reminderText(uuid: UUID): String {
		return "Prompt: ${AbstractTextRound.getSubmitted(RoomAccess.at(game, this, uuid).traverse(-1))}"
	}

	override fun tourText(tourData: TourData): Pair<String, String> {
		return Pair(
			"${tourData.name}'s build for",
			AbstractTextRound.getSubmitted(tourData.access.copy().traverse(-1))
		)
	}
}
