import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

object BarManager {
	val barMap = HashMap<UUID, BossBar>()

	fun newBossBar(
		player: Player,
		name: TextComponent,
		progress: Float,
		barColor: BossBar.Color
	) {
		val bossBar = barMap.getOrPut(player.uniqueId) {
			BossBar.bossBar(name, progress, barColor, BossBar.Overlay.PROGRESS)
		}
		bossBar.name(name)
		bossBar.progress(progress)
		bossBar.color(barColor)
		player.showBossBar(bossBar)
	}
}
