package round.rounds

import game.Game
import round.Room
import round.Round
import java.util.*

abstract class AbstractBuildRound(game: Game, rooms: Array<Room>, index: Int) : Round(game, rooms, index) {
	val donePlayers = ArrayList<UUID>()

	fun makeDone(uuid: UUID) {
		if (donePlayers.contains(uuid)) return
		donePlayers.add(uuid)
		Teams.updatePlayer(BGPlayer.getPlayer(uuid))
	}

	override fun progress(): Pair<Int, Int> = Pair(donePlayers.size, game.numPlayers())

	override fun getTime(): Int = game.setup.buildTime

	override fun isPlayerReady(uuid: UUID): Boolean {
		return donePlayers.contains(uuid)
	}

	override fun doRoomTeleport() = true
}
