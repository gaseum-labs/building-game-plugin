package round.rounds

import game.Game
import round.Room
import round.Round
import org.bukkit.Bukkit
import round.RoomAccess
import round.TourData
import java.util.*
import kotlin.collections.HashMap

class VoteRound(game: Game, rooms: Array<Room>, index: Int) : Round(game, rooms, index) {
	val votes = HashMap<UUID, UUID>()

	/* set after all votes are cast */
	val revealOrder = ArrayList<Pair<UUID, Int>>()

	override fun postRoomsBuild() {
		/* break down the barriers */
		for (i in 1 until game.numPlayers()) {
			rooms[i].destroyLeftWall(game.world)
		}
		for (i in 0 until game.numPlayers() - 1) {
			rooms[i].destroyRightWall(game.world)
		}
	}

	override fun progress(): Pair<Int, Int> = Pair(votes.size, game.numPlayers())

	override fun getTime(): Int? = null

	override fun isPlayerReady(uuid: UUID): Boolean = votes.containsKey(uuid)

	override fun doRoomTeleport(): Boolean = false

	/* display */

	override fun splashText(uuid: UUID): Triple<String, String, String?> {
		return Triple(
			"There is 1 imposter among us for:",
			RoomAccess.at(game, this).traverse(-1).round<ImposterRound>().prompt,
			"Use /vote to vote out the imposter"
		)
	}

	override fun barText(): Pair<String, String> {
		return Pair("Vote", "Votes Cast")
	}

	override fun reminderText(uuid: UUID): String {
		val vote = votes[uuid]

		return if (vote != null) {
			"You have voted out ${Bukkit.getOfflinePlayer(vote).name}"
		} else {
			"Correct prompt: ${RoomAccess.at(game, this).traverse(-1).round<ImposterRound>().prompt}"
		}
	}

	override fun tourText(tourData: TourData): Pair<String, String> {
		val numVotes = votes.filterValues { it == tourData.player }.size

		return if (tourData.player == tourData.access.copy().traverse(-1).round<ImposterRound>().imposter) {
			Pair("${tourData.name} was the imposter!", if (numVotes.toFloat() < game.numPlayers() / 2.0f) {
				"Safe with $numVotes votes"
			} else {
				"Voted out with $numVotes votes!"
			})
		} else {
			Pair("${tourData.name} was innocent!", "Received $numVotes votes")
		}
	}
}
