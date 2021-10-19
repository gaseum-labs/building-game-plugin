import java.util.*
import kotlin.collections.HashMap

class PlayerData() {
	companion object {
		val list = HashMap<UUID, PlayerData>()

		fun get(uuid: UUID): PlayerData {
			return list.getOrPut(uuid) { PlayerData() }
		}
	}

	var participating: Boolean = false
}
