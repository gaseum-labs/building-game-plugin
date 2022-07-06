
import game.GameRunner
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class EventListener : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val player = event.player

		Teams.updatePlayer(player)

		val game = GameRunner.ongoing ?: return
		val gameLocation = game.getPlayerLoginLocation(player.uniqueId)

		if (gameLocation != null) {
			player.teleport(gameLocation)

		} else if (player.world !== Bukkit.getWorlds().first()) {
			player.teleport(Teams.lobbyLocation())
		}
	}
}
