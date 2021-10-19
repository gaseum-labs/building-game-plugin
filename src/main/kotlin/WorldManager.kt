import org.bukkit.*
import java.io.File

object WorldManager {
	const val GAME_WORLD_NAME = "game"

	var gameWorld: World? = null

	fun refreshGameWorld(): World {
		destroyWorld(GAME_WORLD_NAME)
		val world = Bukkit.createWorld(WorldCreator(GAME_WORLD_NAME).environment(World.Environment.NORMAL).generateStructures(false).type(WorldType.FLAT))!!

		world.time = 1000
		world.setStorm(false)
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
		world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)

		gameWorld = world
		return world
	}

	fun destroyWorld(name: String): World? {
		val oldWorld = Bukkit.getServer().getWorld(name)
		if (oldWorld != null) Bukkit.getServer().unloadWorld(oldWorld, false)

		val file = File(name)
		if (file.exists() && file.isDirectory) file.deleteRecursively()

		return oldWorld
	}
}
