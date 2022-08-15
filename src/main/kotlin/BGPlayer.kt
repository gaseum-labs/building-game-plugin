import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Essentially, a temporary view into a PlayerData that is to the currently presence of the player
 */
class BGPlayer(
	val uuid: UUID,
	/** duplicate field of playerData for convenience */
	val name: String,
	val player: Player?,
	val playerData: PlayerData
) {
	companion object {
		fun getPlayer(uuid: UUID): BGPlayer {
			val onlinePlayer = Bukkit.getPlayer(uuid)
			val playerData = PlayerData.getUnsafe(uuid)
			return BGPlayer(uuid, playerData.name, onlinePlayer, playerData)
		}

		fun getPlayerName(name: String): BGPlayer? {
			val data = PlayerData.getName(name) ?: return null
			return BGPlayer(data.uuid, name, Bukkit.getPlayer(data.uuid), data)
		}

		fun from(player: Player): BGPlayer {
			return BGPlayer(player.uniqueId, player.name, player, PlayerData.getOrCreate(player))
		}
	}

	/** only fill teleport map if the player is absent */
	fun teleport(location: Location) {
		if (player != null) {
			player.teleport(location)
		} else {
			playerData.teleportLocation = location
		}
	}
}
