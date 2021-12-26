package game
import Grid
import Teams
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import round.Room
import round.RoomAccess
import round.Round
import round.RoundType
import java.time.Duration
import java.util.*

class Game(val world: World, val gamePlayers: List<UUID>, val setup: Setup) {
	val grid = Grid(gamePlayers.size)
	/**
	 *  completely independent of game positions
	 *
	 *  column: the imposter round index
	 *  row 0: the original builder
	 *  row 1: the imposter
	 */
	val imposterGrid = Grid(gamePlayers.size)

	val rounds = ArrayList<Round>()

	var time: Int = 1
	var timer: Int? = null
	var nextZ = 0

	/* game state readers */

	fun imposterRoundIndex(roundIndex: Int) = (roundIndex - 3) / 2
	fun numPlayers() = gamePlayers.size
	fun playersIndex(uuid: UUID) = gamePlayers.indexOf(uuid)
	fun playerInGame(uuid: UUID) = playersIndex(uuid) != -1
	fun currentRound() = rounds.last()

	fun getPlayersRoom(round: Round, uuid: UUID): Room {
		/* for imposter and vote rounds, use the positions from row index 3 */
		return round.rooms[grid.indexOnRow(
			if (setup.imposter && round.index > 3) 3 else round.index,
			playersIndex(uuid)
		)]
	}

	fun getRoomsPlayer(roundIndex: Int, roomIndex: Int): UUID {
		return gamePlayers[grid.access(if (setup.imposter && roundIndex > 3) 3 else roundIndex, roomIndex)]
	}
	fun getRoomsPlayer(round: Round, room: Room): UUID {
		return gamePlayers[grid.access(if (setup.imposter && round.index > 3) 3 else round.index, room.index)]
	}
	fun getRoomsPlayer(access: RoomAccess): UUID {
		return gamePlayers[grid.access(if (setup.imposter && access.round.index > 3) 3 else access.round.index, access.room.index)]
	}

	fun getOriginalBuilder(round: Round): UUID {
		return gamePlayers[imposterGrid.access(0, imposterRoundIndex(round.index))]
	}
	fun getImposter(round: Round): UUID {
		return gamePlayers[imposterGrid.access(1, imposterRoundIndex(round.index))]
	}

	/**
	 * @return null to end the game
	 */
	private fun getRoundType(index: Int): RoundType? {
		return when {
			/* first time this is called */
			index == 0 -> RoundType.ENTRY
			else -> if (setup.imposter) {
				when {
					/* the round after all game rounds have been played */
					index == 3 + numPlayers() * 2 -> RoundType.TOUR
					index > 3 + numPlayers() * 2 -> null
					/* entry, build, guess, then alternating imposter then vote */
					index == 1 -> RoundType.BUILD
					index == 2 -> RoundType.GUESS
					(index - 3) % 2 == 0 -> RoundType.IMPOSTER
					else -> RoundType.VOTE
				}
			} else {
				when {
					/* the round after all game rounds have been played */
					index == numPlayers() -> RoundType.TOUR
					index > numPlayers() -> null
					/* entry, alternating build then guess */
					(index - 1) % 2 == 0 -> RoundType.BUILD
					else -> RoundType.GUESS
				}
			}
		}
	}

	/**
	 * @return null to end the game, or the new round which was created
	 */
	fun startNextRound(): Round? {
		val roundIndex = rounds.size
		val type = getRoundType(roundIndex) ?: return null

		val generatedRooms = type.generateRooms(0, nextZ, setup.roomSize, setup.buildSize, setup.holdingSize, numPlayers())

		val newRound = type.create(this, generatedRooms ?: currentRound().rooms, roundIndex)

		if (generatedRooms != null) {
			generatedRooms.forEach { it.build(world) }
			nextZ += generatedRooms.first().depth
		}

		newRound.postRoomsBuild()

		gamePlayers.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
			if (newRound.doRoomTeleport()) {
				player.teleport(getPlayersRoom(newRound, player.uniqueId).spawnLocation(world))
			}

			val splash = newRound.splashText(player.uniqueId)
			if (splash != null) {
				sendTitle(player, splash.first, splash.second, splash.third)
			}

			Teams.updatePlayer(player)
			Teams.resetPlayerStats(player)
		}

		/* start the timer if applicable */
		time = newRound.getTime() ?: 1
		timer = newRound.getTime()

		rounds.add(newRound)
		return newRound
	}

	fun sendTitle(player: Player, title: String, subtitle: String, reminder: String?) {
		player.showTitle(Title.title(Component.text(title), Component.text(subtitle), Title.Times.of(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(1))))
		player.sendMessage(Component.text("$title ").append(Component.text(subtitle).style(Style.style(TextDecoration.BOLD))))

		if (reminder != null) {
			player.sendMessage(Component.text(reminder).style(Style.style(TextDecoration.ITALIC)))
		}
	}

	fun getPlayerLoginLocation(uuid: UUID): Location? {
		if (!playerInGame(uuid)) return null
		return getPlayersRoom(currentRound(), uuid).spawnLocation(world)
	}
}
