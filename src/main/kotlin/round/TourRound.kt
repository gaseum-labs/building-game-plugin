package round

import Game
import Room
import Round
import org.bukkit.Bukkit
import java.util.*

class TourRound(game: Game) : Round(game) {
	var tourAlong = 0
	fun tourSize() = game.numPlayers() * game.numPlayers()

	override fun generateRooms(x: Int, z: Int, numPlayers: Int): Array<Room>? = null

	override fun progress(): Pair<Int, Int> = Pair(tourAlong, tourSize())

	override fun getTime(): Int? = null

	fun fromAlong(along: Int):  TourData {
		val col = along / game.numPlayers()
		val row = along % game.numPlayers()

		return TourData(
			row, col,
			game.rounds[row], game.rooms[row]?.get(col),
			Bukkit.getOfflinePlayer(game.gamePlayers[game.grid.access(row, col)]).name
				?: "Unknown"
		)
	}

	fun startTour(along: Int) {
		val tourData = fromAlong(along)
		val (title, subtitle) = tourData.round.tourText(tourData)

		game.gamePlayers.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
			val room = tourData.room
			if (room != null) {
				player.teleport(tourData.room.spawnLocation(game.world))
			}

			game.sendTitle(player, title, subtitle)
		}
	}

	init {
		startTour(tourAlong)
	}

	override fun isPlayerReady(uuid: UUID): Boolean {
		return false
	}

	override fun doRoomTeleport() = false

	/* display */

	override fun splashText(uuid: UUID): Pair<String, String>? = null

	override fun barText(): Pair<String, String> {
		return Pair("Tour", "Visited")
	}

	override fun reminderText(uuid: UUID, reminderIndex: Int): String {
		val player = Bukkit.getPlayer(uuid)

		return if (player?.isOp == true && reminderIndex % 3 == 0) {
			"Use /next to see the next build/guess"

		} else {
			val tourData = fromAlong(tourAlong)
			val (title, subtitle) = tourData.round.tourText(tourData)
			"$title: $subtitle"
		}
	}

	override fun tourText(tourData: TourData): Pair<String, String> = Pair("", "")
}
