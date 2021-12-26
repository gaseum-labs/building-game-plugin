package round.rounds

import game.Game
import round.Room
import round.RoomAccess
import round.Round
import org.bukkit.Bukkit
import java.util.*

abstract class AbstractTextRound(game: Game, rooms: Array<Room>, index: Int) : Round(game, rooms, index) {
	val submittedPlayers = ArrayList<UUID>()
	val prompts = Array(game.numPlayers()) { "" }

	/**
	 * @return whether the prompt was accepted or not
	 *
	 * can be not accepted if only whitespace or contains illegal characters
	 */
	fun submit(uuid: UUID, prompt: String): Boolean {
		val whitespaceRemoved = prompt.trim()
		if (whitespaceRemoved.isEmpty() || whitespaceRemoved.any { c -> c !in ' '..'~' }) {
			return false
		}

		val room = RoomAccess.at(game, this, uuid).room

		if (!submittedPlayers.contains(uuid))submittedPlayers.add(uuid)
		prompts[room.index] = whitespaceRemoved

		val player = Bukkit.getPlayer(uuid)
		if (player != null) Teams.updatePlayer(player)

		return true
	}

	fun numSubmitted(): Int {
		return submittedPlayers.size
	}

	override fun isPlayerReady(uuid: UUID): Boolean {
		return submittedPlayers.contains(uuid)
	}

	/**
	 * @return the prompt after this round has completed
	 */
	fun getSubmittedPrompt(room: Room): String {
		return prompts[room.index]
	}

	companion object {
		fun getSubmitted(access: RoomAccess): String {
			return access.round<AbstractTextRound>().getSubmittedPrompt(access.room)
		}
	}
}
