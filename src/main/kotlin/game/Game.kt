package game
import BGPlayer
import Grid
import Teams
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.World
import round.*
import java.time.Duration
import java.util.*

class Game(val world: World, val gamePlayers: List<UUID>, val setup: Setup) {
	val grid = Grid.generateGrid(gamePlayers.size) { inputRow ->
		if (setup.imposter) when (inputRow) {
			-1 -> 2 /* access -1 for determining imposters */
			0 -> 0 /* prompt */
			1 -> 0 /* builder builds their own prompt */
			2 -> 1 /* another person guesses the build */
			else -> 0 /* builders go back to their own lanes */
		} else when {
			inputRow < gamePlayers.size -> inputRow
			else -> 0/* make sure tour round doesn't break */
		}
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
	fun getOriginalBuilder(round: Round) = getOriginal(round, 1) /* build round (0 would work too) */
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

		val generatedRooms = type.generateRooms(
			0,
			nextZ,
			setup.roomWidth,
			setup.roomDepth,
			setup.buildSize,
			setup.holdingSize,
			numPlayers()
		)

		val newRound = type.create(this, generatedRooms ?: currentRound().rooms, roundIndex)

		if (generatedRooms != null) {
			generatedRooms.forEach { it.build(world) }
			nextZ += generatedRooms.first().depth
		}

		newRound.postRoomsBuild()

		gamePlayers.map(BGPlayer::getPlayer).forEach { bgPlayer ->
			val playersRoom = getPlayersRoom(newRound, bgPlayer.uuid)

			if (newRound.doRoomTeleport()) {
				bgPlayer.teleport(playersRoom.spawnLocation(world))
			}

			val splash = newRound.splashText(bgPlayer.uuid)
			if (splash != null) {
				sendTitle(bgPlayer, splash.first, splash.second, splash.third)
			}

			/* fake players performing actions automatically */
			if (bgPlayer.playerData.dummy) {
				FakeBuilder.performRoundAction(bgPlayer, newRound, playersRoom)
			}

			Teams.updatePlayer(bgPlayer)
			Teams.resetPlayerStats(bgPlayer)
		}

		/* start the timer if applicable */
		time = newRound.getTime() ?: 1
		timer = newRound.getTime()

		rounds.add(newRound)
		return newRound
	}

	fun sendTitle(bgPlayer: BGPlayer, title: String, subtitle: String, reminder: String?) {
		val player = bgPlayer.player ?: return
		player.showTitle(Title.title(Component.text(title), Component.text(subtitle), Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(1))))
		player.sendMessage(Component.text("$title ").append(Component.text(subtitle).style(Style.style(TextDecoration.BOLD))))

		if (reminder != null) {
			player.sendMessage(Component.text(reminder).style(Style.style(TextDecoration.ITALIC)))
		}
	}
}
