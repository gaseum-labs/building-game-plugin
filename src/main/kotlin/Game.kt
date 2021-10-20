
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import round.BuildRound
import round.EntryRound
import round.GuessRound
import round.TourRound
import java.util.*

class Game(val world: World, val gamePlayers: List<UUID>, val setup: Setup) {
	val rounds = ArrayList<Round>()
	val rooms = ArrayList<Array<Room>?>()

	var time: Int = 1
	var timer: Int? = null

	val grid = Grid(gamePlayers.size)

	var nextZ = 0

	val imposterPositions = gamePlayers.shuffled()

	fun roundIndex() = rounds.lastIndex
	fun imposterRoundIndex() = rounds.lastIndex - 3
	fun numPlayers() = gamePlayers.size

	fun playerInGame(uuid: UUID): Boolean {
		return playersIndex(uuid) != -1
	}

	fun playersIndex(uuid: UUID): Int {
		return gamePlayers.indexOf(uuid)
	}

	fun playersRoom(uuid: UUID): Int {
		var roundIndex = roundIndex()
		if (roundIndex >= numPlayers()) roundIndex = numPlayers() -1

		return grid.indexOnRow(roundIndex, playersIndex(uuid))
	}

	fun findRooms(row: Int): Array<Room> {
		for (i in row downTo 0) {
			val rooms = rooms[i]
			if (rooms != null) return rooms
		}

		return emptyArray()
	}

	fun currentRooms(): Array<Room> {
		return findRooms(roundIndex())
	}

	fun currentRound(): Round {
		return rounds.last()
	}

	fun previousRound(): Round {
		return rounds[roundIndex() - 1]
	}

	fun generateRound(): Round? {
		return when {
			rounds.isEmpty() -> EntryRound(this)
			rounds.size == numPlayers() -> TourRound(this)
			rounds.size > numPlayers() -> null
			(rounds.size - 1) % 2 == 0 -> BuildRound(this)
			else -> GuessRound(this)
		}
	}

	fun startRound(newRound: Round) {
		rounds.add(newRound)

		val newRooms = newRound.generateRooms(0, nextZ, numPlayers())
		if (newRooms != null) {
			rooms.add(newRooms)

			newRooms.forEach { room ->
				room.build(world)
			}

			nextZ += newRooms.first().depth
		} else {
			/* mark as having no rooms of its own, relies on previous rooms */
			rooms.add(null)
		}

		newRound.postRoomsBuild()

		/* teleport players to the next round */
		gamePlayers.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
			if (newRound.doRoomTeleport()) {
				player.teleport(currentRooms()[playersRoom(player.uniqueId)].spawnLocation(world))
			}

			val splash = newRound.splashText(player.uniqueId)
			if (splash != null) {
				sendTitle(player, splash.first, splash.second)
			}

			Teams.updatePlayer(player)
			Teams.resetPlayerStats(player)
		}

		/* start the timer if applicable */
		time = newRound.getTime() ?: 1
		timer = newRound.getTime()
	}

	fun sendTitle(player: Player, title: String, subtitle: String) {
		player.sendTitle(title, subtitle, 0, 40, 20)
		player.sendMessage(Component.text("$title ").append(Component.text(subtitle).style(Style.style(TextDecoration.BOLD))))
	}

	fun getPlayerLoginLocation(uuid: UUID): Location? {
		val roomIndex = playersRoom(uuid)

		return if (roomIndex == -1) {
			null
		} else {
			currentRooms()[roomIndex].spawnLocation(world)
		}
	}
}
