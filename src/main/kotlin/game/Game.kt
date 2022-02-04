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
	val grid = Grid(gamePlayers.size) { inputRow ->
		if (setup.imposter) when (inputRow) {
			-1 -> 2 /* access -1 for determining imposters */
			0 -> 0 /* prompt */
			1 -> 0 /* builder builds their own prompt */
			2 -> 1 /* another person guesses the build */
			else -> 0 /* builders go back to their own lanes */
		} else inputRow
	}

	/**
	 * indices list of which column of the first three rounds defines the next imposter/vote round
	 */
	val imposterColumnOrder = Array(gamePlayers.size) { it }

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

	/**
	 * for imposter mode:
	 * > use row 0 for prompt and build
	 * > use row 1 for guess
	 * > use row 0 again for imposter rounds
	 * otherwise use the row corresponding to the round index
	 */

	fun getPlayersRoom(round: Round, uuid: UUID): Room {
		/* for imposter and vote rounds, use the positions from row index 3 */
		return round.rooms[grid.indexOnRow(round.index, playersIndex(uuid))]
	}

	fun getRoomsPlayer(roundIndex: Int, roomIndex: Int): UUID {
		return gamePlayers[grid.access(roundIndex, roomIndex)]
	}
	fun getRoomsPlayer(round: Round, room: Room) = getRoomsPlayer(round.index, room.index)
	fun getRoomsPlayer(access: RoomAccess) = getRoomsPlayer(access.round.index, access.room.index)

	/**
	 * @param partIndex - the previous round which is being examined
	 */
	private fun getOriginal(round: Round, partIndex: Int): UUID {
		return getRoomsPlayer(partIndex, imposterColumnOrder[imposterRoundIndex(round.index)])
	}
	fun getOriginalBuilder(round: Round) = getOriginal(round, 0) /* prompt round (1 would work too) */
	fun getOriginalGuesser(round: Round) = getOriginal(round, 2) /* guess round */
	fun getImposter(round: Round) = getOriginal(round, -1) /* special access for imposters */

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
