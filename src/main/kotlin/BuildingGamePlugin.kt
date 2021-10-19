import co.aikar.commands.PaperCommandManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class BuildingGamePlugin : JavaPlugin() {
	companion object {
		lateinit var plugin: BuildingGamePlugin
		private set
		lateinit var commandManager: PaperCommandManager
		private set
	}

	init {
		plugin = this
	}

	override fun onEnable() {
		commandManager = PaperCommandManager(this)
		Commands.registerCompletions(commandManager)
		commandManager.registerCommand(Commands())

		server.pluginManager.registerEvents(EventListener(), this)

		Bukkit.getScheduler().scheduleSyncDelayedTask(this) {
			GameRunner.beginTicking()
		}
	}
}