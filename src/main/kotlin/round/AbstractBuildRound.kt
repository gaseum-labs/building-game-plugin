package round

import Game
import Room
import Round
import java.util.*

abstract class AbstractBuildRound(game: Game) : Round(game) {
	val donePlayers = ArrayList<UUID>()

	override fun generateRooms(x: Int, z: Int, numPlayers: Int): Array<Room> {
		return Array(numPlayers) { i ->
			val roomSize = game.setup.roomSize
			val buildSize = game.setup.buildSize

			Room(
				x + i * roomSize, z,
				roomSize,
				roomSize,
				roomSize / 2,
				(roomSize - buildSize) / 4,
				Room.Area(
					(roomSize - buildSize) / 2,
					(roomSize - buildSize) / 2,
					buildSize,
					buildSize
				)
			)
		}
	}

	override fun progress(): Pair<Int, Int> = Pair(donePlayers.size, game.numPlayers())

	override fun getTime(): Int = game.setup.buildTime

	override fun isPlayerReady(uuid: UUID): Boolean {
		return donePlayers.contains(uuid)
	}

	override fun doRoomTeleport() = true
}
