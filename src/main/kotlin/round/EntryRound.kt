package round

import Game
import Room
import org.bukkit.ChatColor
import java.util.*

class EntryRound(game: Game) : AbstractTextRound(game) {
	override fun generateRooms(x: Int, z: Int, numPlayers: Int): Array<Room> {
		return Array(numPlayers) { i ->
			val roomSize = game.setup.roomSize
			val holdingSize = game.setup.holdingSize
			val offsetX = (roomSize - holdingSize) / 2

			Room(
				x + i * roomSize + offsetX, z,
				holdingSize,
				holdingSize,
				holdingSize / 2,
				holdingSize / 2,
				null
			)
		}
	}

	override fun getTime(): Int? = null

	override fun progress(): Pair<Int, Int> {
		return Pair(numSubmitted(), game.numPlayers())
	}

	override fun doRoomTeleport() = true

	override fun postRoomsBuild() {}

	/* display */

	override fun splashText(uuid: UUID): Pair<String, String> {
		return Pair("${ChatColor.GREEN}Submit a Prompt!", "")
	}

	override fun barText(): Pair<String, String> {
		return Pair("Enter Prompt", "Prompts Submitted")
	}

	override fun reminderText(uuid: UUID, reminderIndex: Int): String {
		return if (submittedPlayers.contains(uuid)) {
			"Submitted prompt: ${prompts[game.playersRoom(uuid)]}"

		} else {
			"Use /prompt [prompt] to submit"
		}
	}

	override fun tourText(tourData: TourData): Pair<String, String> {
		return Pair("${tourData.name}'s prompt", (tourData.round as EntryRound).prompts[tourData.col])
	}
}
