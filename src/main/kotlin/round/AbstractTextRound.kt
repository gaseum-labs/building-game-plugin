package round

import Game
import Round
import org.bukkit.Bukkit
import java.util.*

abstract class AbstractTextRound(game: Game) : Round(game) {
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

		val roomIndex = game.playersRoom(uuid)
		if (roomIndex == -1) {
			return false
		}

		if (!submittedPlayers.contains(uuid)) {
			submittedPlayers.add(uuid)
		}

		prompts[roomIndex] = prompt

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
}
