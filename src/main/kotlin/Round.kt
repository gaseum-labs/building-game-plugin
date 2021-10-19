import round.TourData
import java.util.*

abstract class Round(val game: Game) {
	abstract fun generateRooms(x: Int, z: Int, numPlayers: Int): Array<Room>?
	abstract fun progress(): Pair<Int, Int>
	abstract fun getTime(): Int?
	abstract fun isPlayerReady(uuid: UUID): Boolean
	abstract fun doRoomTeleport(): Boolean

	/* display */
	abstract fun splashText(uuid: UUID): Pair<String, String>?
	abstract fun barText(): Pair<String, String>
	abstract fun reminderText(uuid: UUID, reminderIndex: Int): String
	abstract fun tourText(tourData: TourData): Pair<String, String>
}
