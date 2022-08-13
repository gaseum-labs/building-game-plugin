import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class BGPlayer(val uuid: UUID, val player: Player?) {
	companion object {
		fun getPlayer(uuid: UUID): BGPlayer {
			return BGPlayer(uuid, Bukkit.getPlayer(uuid))
		}

		private val teleportMaps = HashMap<UUID, Location>()

		fun clearTeleportMaps() {
			teleportMaps.clear()
		}

		/** also clears map entry */
		fun getTeleportLocation(uuid: UUID): Location? {
			return teleportMaps.remove(uuid)
		}
	}

	/** only fill teleport map if the player is absent */
	fun teleport(location: Location) {
		if (player != null) {
			player.teleport(location)
		} else {
			teleportMaps[uuid] = location
		}
	}


}
