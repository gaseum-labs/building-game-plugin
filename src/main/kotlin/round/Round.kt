package round

import game.Game
import java.util.*

abstract class Round(val game: Game, val rooms: Array<Room>, val index: Int) {
	abstract fun progress(): Pair<Int, Int>
	abstract fun getTime(): Int?
	abstract fun isPlayerReady(uuid: UUID): Boolean
	abstract fun doRoomTeleport(): Boolean
	abstract fun postRoomsBuild()

	/* display */
	abstract fun splashText(uuid: UUID): Triple<String, String, String?>?
	abstract fun barText(): Pair<String, String>
	abstract fun reminderText(uuid: UUID): String
	abstract fun tourText(tourData: TourData): Pair<String, String>
}
