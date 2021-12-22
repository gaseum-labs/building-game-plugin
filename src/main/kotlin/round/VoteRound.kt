package round

import Game
import Room
import Round
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*
import kotlin.collections.HashMap

class VoteRound(game: Game) : Round(game) {
	val votes = HashMap<UUID, UUID>()

	/* set after all votes are cast */
	val revealOrder = ArrayList<Pair<UUID, Int>>()

	override fun postRoomsBuild() {
		val rooms = game.currentRooms()

		/* break down the barriers */
		for (i in 1 until game.numPlayers()) {
			rooms[i].destroyLeftWall(game.world)
		}
		for (i in 0 until game.numPlayers() - 1) {
			rooms[i].destroyRightWall(game.world)
		}
	}

	override fun generateRooms(x: Int, z: Int, numPlayers: Int): Array<Room>? = null

	override fun progress(): Pair<Int, Int> = Pair(votes.size, game.numPlayers())

	override fun getTime(): Int? = null

	override fun isPlayerReady(uuid: UUID): Boolean = votes.containsKey(uuid)

	override fun doRoomTeleport(): Boolean = false

	/* display */

	override fun splashText(uuid: UUID): Pair<String, String> {
		return Pair(
			"There is ${ChatColor.RED}${ChatColor.BOLD}1 ${ChatColor.RESET}imposter among us",
			(game.previousRound() as ImposterRound).prompt
		)
	}

	override fun barText(): Pair<String, String> {
		return Pair("Vote", "Votes Cast")
	}

	override fun reminderText(uuid: UUID, reminderIndex: Int): String {
		val vote = votes[uuid]

		return if (vote != null) {
			"You have voted out ${Bukkit.getOfflinePlayer(vote).name}"

		} else if (reminderIndex % 2 == 0) {
			"Use /vote to vote out the imposter"

		} else {
			"Original prompt: ${(game.previousRound() as ImposterRound).prompt}"
		}
	}

	/* tour order will switch to row first for the imposter rounds */
	/* it will also go in vote order, but always showing imposter last */
	override fun tourText(tourData: TourData): Pair<String, String> {
		val builder = game.imposterPositions[tourData.col]
		val builderName = Bukkit.getOfflinePlayer(builder).name ?: "Unknown"
		val numVotes = votes.filterValues { it == builder }.size

		return if (builder == (game.rounds[tourData.row - 1] as ImposterRound).imposter) {
			Pair("$builderName was the imposter!", if (numVotes.toFloat() < game.numPlayers() / 2.0f) {
				"${ChatColor.GREEN}Safe with $numVotes votes"
			} else {
				"${ChatColor.RED}Voted out with $numVotes votes!"
			})
		} else {
			Pair("$builderName was innocent!", "Received $numVotes votes")
		}
	}
}
