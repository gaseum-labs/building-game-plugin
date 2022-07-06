package round.rounds

import game.Game
import round.Room
import round.RoomAccess
import round.TourData
import java.util.*

class EntryRound(game: Game, rooms: Array<Room>, index: Int) : AbstractTextRound(game, rooms, index) {
	override fun getTime(): Int? = null

	override fun progress(): Pair<Int, Int> {
		return Pair(numSubmitted(), game.numPlayers())
	}

	override fun doRoomTeleport() = true

	override fun postRoomsBuild() {}

	/* display */

	override fun splashText(uuid: UUID): Triple<String, String, String?> {
		return Triple("Submit a Prompt!", "", "Use /prompt [prompt] to submit")
	}

	override fun barText(): Pair<String, String> {
		return Pair("Enter Prompt", "Prompts Submitted")
	}

	override fun reminderText(uuid: UUID): String {
		return if (submittedPlayers.contains(uuid)) {
			"Submitted prompt: ${getSubmitted(RoomAccess.at(game, this, uuid))}"
		} else {
			"Use /prompt [prompt] to submit"
		}
	}

	override fun tourText(tourData: TourData): Pair<String, String> {
		return "${tourData.name}'s prompt" to getSubmitted(tourData.access)
	}
}
