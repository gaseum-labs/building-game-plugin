
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerJoinEvent

class EventListener : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val bgPlayer = BGPlayer.from(event.player)

		Teams.updatePlayer(bgPlayer)

		val newLocation = bgPlayer.playerData.takeTeleportLocation()
		if (newLocation != null) {
			bgPlayer.teleport(newLocation)
		}
	}

	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
		val entity = event.entity

		if (entity.world !== WorldManager.gameWorld) return

		if (entity.entitySpawnReason === CreatureSpawnEvent.SpawnReason.NATURAL) {
			event.isCancelled = true
			return
		}

		entity.setGravity(false)
		if (entity is LivingEntity) {
			entity.setAI(false)
			entity.removeWhenFarAway = false

			if (entity is Zombie) {
				entity.setShouldBurnInDay(false)
				entity.canPickupItems = true
			}
		}

		if (entity is Item) {
			entity.pickupDelay = Int.MAX_VALUE
			entity.setWillAge(false)
		}
	}
}
