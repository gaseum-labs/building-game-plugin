package round.rounds

import BGPlayer
import PlayerData
import game.Game
import round.Room
import round.Round
import org.bukkit.Bukkit
import round.RoomAccess
import round.TourData
import java.util.*

class TourRound(game: Game, teams: Array<Room>, index: Int) : Round(game, teams, index) {
	var tourAlong = 0
	fun tourSize() = game.numPlayers() * game.numPlayers()

	override fun progress(): Pair<Int, Int> = Pair(tourAlong, tourSize())

	override fun getTime(): Int? = null

	fun fromAlong(along: Int): TourData {
		val access = if (game.setup.imposter) {
			/* skip the first 3 regular rounds (and the first imposter build round) */
			/* also skip all imposter build rounds */
			/* keep the same round, traverse across rooms */
			val access = RoomAccess.at(game, (along / game.numPlayers()) * 2 + 4)
			val order = access.round<VoteRound>().revealOrder
			access.jumpToPlayer(order[along % game.numPlayers()].first)
		} else {
			/* keep the same room, traverse across rounds */
			RoomAccess.at(game, along % game.numPlayers(), along / game.numPlayers())
		}

		val player = game.getRoomsPlayer(access)

		return TourData(
			access,
			player,
			PlayerData.getUnsafe(player).name,
			doTeleport=when (access.round) {
				is EntryRound -> true
				is BuildRound -> true
				is GuessRound -> false
				is VoteRound -> true
				else -> false
			}
		)
	}

	fun startTour(along: Int) {
		val tourData = fromAlong(along)
		val (title, subtitle) = tourData.access.round.tourText(tourData)

		game.gamePlayers.map(BGPlayer::getPlayer).forEach { bgPlayer ->
			if (tourData.doTeleport) {
				bgPlayer.teleport(tourData.access.room.spawnLocation(game.world))
			}
			game.sendTitle(bgPlayer, title, subtitle, null)
		}
	}

	override fun isPlayerReady(uuid: UUID): Boolean {
		return false
	}

	override fun doRoomTeleport() = false

	override fun postRoomsBuild() {
		startTour(tourAlong) /* 0 */
	}

	/* display */

	override fun splashText(uuid: UUID): Triple<String, String, String?>? = null

	override fun barText(): Pair<String, String> {
		return Pair("Tour", "Visited")
	}

	override fun reminderText(uuid: UUID): String {
		val tourData = fromAlong(tourAlong)
		val (title, subtitle) = tourData.access.round.tourText(tourData)
		return "$title: $subtitle"
	}

	override fun tourText(tourData: TourData): Pair<String, String> = Pair("", "")
}
