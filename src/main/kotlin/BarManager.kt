import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.world.BossEvent
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

object BarManager {
	class CustomBossEvent(uuid: UUID, name: Component, color: BossBarColor, style: BossBarOverlay)
		: BossEvent(uuid, name, color, style)

	fun addBossBar(player: Player) {
		(player as CraftPlayer).handle.connection.send(
			ClientboundBossEventPacket.createAddPacket(
				CustomBossEvent(
					player.uniqueId,
					TextComponent(""),
					BossEvent.BossBarColor.WHITE,
					BossEvent.BossBarOverlay.PROGRESS
				)
			)
		)
	}

	fun updateBossBar(
		player: Player,
		name: String,
		progress: Float,
		barColor: BossEvent.BossBarColor,
	) {
		val bossBar = CustomBossEvent(
			player.uniqueId,
			TextComponent(name),
			barColor,
			BossEvent.BossBarOverlay.PROGRESS
		)
		bossBar.progress = progress

		val connection = (player as CraftPlayer).handle.connection

		connection.send(ClientboundBossEventPacket.createUpdateNamePacket(bossBar))
		connection.send(ClientboundBossEventPacket.createUpdateProgressPacket(bossBar))
		// connection.send(ClientboundBossEventPacket.createUpdateStylePacket(bossBar))
	}
}
