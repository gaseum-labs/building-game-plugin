import game.GameRunner
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.scoreboard.Team

object Teams {
	const val UNREADY_TEAM_NAME = "unready"
	const val READY_TEAM_NAME = "ready"

	lateinit var unreadyTeam: Team
	lateinit var readyTeam: Team

	fun initTeams() {
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

		unreadyTeam = scoreboard.getTeam(UNREADY_TEAM_NAME)
			?: scoreboard.registerNewTeam(UNREADY_TEAM_NAME)

		unreadyTeam.color(NamedTextColor.RED)

		readyTeam = scoreboard.getTeam(READY_TEAM_NAME)
			?: scoreboard.registerNewTeam(READY_TEAM_NAME)

		readyTeam.color(NamedTextColor.GREEN)
	}

	fun updatePlayer(bgPlayer: BGPlayer) {
		val ready = GameRunner.ongoing?.currentRound()?.isPlayerReady(bgPlayer.uuid)
			?: bgPlayer.playerData.participating

		if (ready) {
			unreadyTeam.removeEntry(bgPlayer.name)
			readyTeam.addEntry(bgPlayer.name)
		} else {
			readyTeam.removeEntry(bgPlayer.name)
			unreadyTeam.addEntry(bgPlayer.name)
		}
	}

	fun resetPlayerStats(bgPlayer: BGPlayer) {
		val player = bgPlayer.player
		if (player != null) {
			player.inventory.clear()
			player.gameMode = GameMode.CREATIVE
		}
	}

	fun lobbyLocation(): Location {
		val world = Bukkit.getWorlds()[0]

		for (y in 255 downTo 0) {
			if (!world.getBlockAt(0, y, 0).isPassable) return Location(world, 0.5, y + 1.0, 0.5)
		}

		return world.spawnLocation
	}
}
