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

	/** returns false if you could not vote for that player */
	fun vote(voter: UUID, candidate: UUID): Boolean {
		return if (game.gamePlayers.contains(candidate)) {
			votes[voter] = candidate
			Teams.updatePlayer(BGPlayer.getPlayer(voter))

			/* create the reveal order once everyone has voted */
			if (votes.size == game.numPlayers()) createRevealOrder()

			true
		} else {
			false
		}
	}

	private fun createRevealOrder() {
		val imposter = RoomAccess.at(game, this).traverse(-1).round<ImposterRound>().imposter

		/* don't include imposter yet */
		val voteCounts = ArrayList(votes
			.filter { (player, _) -> player != imposter }
			.map { (player, _) -> Pair(player, votes.filterValues { it == player }.size) }
			.sortedBy { (_, count) -> count })

		/* place imposter last */
		voteCounts.add(Pair(imposter, votes.filterValues { it == imposter }.size))

		revealOrder.addAll(voteCounts)
	}

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
