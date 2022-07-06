package round.rounds

import game.Game
import round.Room
import round.TourData
import java.util.*

class GuessRound(game: Game, rooms: Array<Room>, index: Int) : AbstractTextRound(game, rooms, index) {
	override fun progress(): Pair<Int, Int> = Pair(numSubmitted(), game.numPlayers())

	override fun getTime(): Int? = null

	override fun doRoomTeleport() = true

	override fun postRoomsBuild() {}

	/* display */

	override fun splashText(uuid: UUID): Triple<String, String, String?> {
		return Triple("Guess!", "", "Use /guess to submit a guess")
	}

	override fun barText(): Pair<String, String> {
		return Pair("Guess", "Guesses")
	}

	override fun reminderText(uuid: UUID): String {
		return "Use /guess to guess the original prompt of this build"
	}

	override fun tourText(tourData: TourData): Pair<String, String> {
		return Pair("${tourData.name}'s guess", getSubmitted(tourData.access))
	}
}
