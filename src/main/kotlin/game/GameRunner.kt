package game

import BarManager
import BuildingGamePlugin
import PlayerData
import Teams
import Util
import WorldManager
import net.kyori.adventure.text.Component
import net.minecraft.world.BossEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.entity.Player

object GameRunner {
	var pregameSetup: Setup = Setup()
	var ongoing: Game? = null

	fun beginTicking() {
		Teams.initTeams()

		var currentTick = 0

		Bukkit.getScheduler().scheduleSyncRepeatingTask(BuildingGamePlugin.plugin, {
			if (currentTick % 20 == 0) {
				checkRoundComplete()

				Bukkit.getOnlinePlayers().forEach { player ->
					updateBossBarAndHotbar(player)
				}
			}

			currentTick = (currentTick + 1) % 294053760
		}, 0, 1)
	}

	fun startGame(type: GameType): String? {
		if (ongoing != null) return "game.Game is already running"

		val gamePlayers = PlayerData.list.filter { (_, playerData) -> playerData.participating }.map { (uuid, _) -> uuid }

		val requiredPlayers = if (pregameSetup.imposter) 4 else 2
		if (gamePlayers.size < requiredPlayers) return "Need at least $requiredPlayers players to start"

		val gameWorld = WorldManager.refreshGameWorld()

		pregameSetup.imposter = type === GameType.IMPOSTER
		val game = Game(gameWorld, gamePlayers, pregameSetup)
		game.startNextRound()

		ongoing = game

		PlayerData.list.forEach { (_, playerData) -> playerData.participating = false }

		return null
	}

	fun checkRoundComplete() {
		val game = ongoing ?: return

		val timer = game.timer
		if (timer != null) {
			game.timer = timer - 1
		}

		val (count, outOf) = game.currentRound().progress()

		if (count == outOf || (timer != null && timer - 1 <= 0)) {
			if (game.startNextRound() == null) {
				/* end game */
				val lobbyLocation = Teams.lobbyLocation()

				game.gamePlayers.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
					Teams.updatePlayer(player)
					player.teleport(lobbyLocation)
				}

				ongoing = null
			}
		}
	}

	/* happens every second (20 ticks) */
	fun updateBossBarAndHotbar(player: Player) {
		val game = ongoing

		if (game == null) {
			BarManager.updateBossBar(player, "Building game.Game", 1.0f, BossEvent.BossBarColor.WHITE)

			val playerData = PlayerData.get(player.uniqueId)

			player.sendActionBar(Component.text(if (playerData.participating) {
				"${GREEN}You have joined, Use /bg leave to leave the game"
			} else {
				"${RED}Use /bg join to join the game"
			}))

		} else {
			val round = game.currentRound()
			val (name, outOf) = round.barText()
			val (count, total) = round.progress()

			val timer = game.timer
			val timeString = if (timer != null) " ${AQUA}Time Remaining: $BOLD${Util.timeString(timer)}" else ""

			BarManager.updateBossBar(
				player,
				"$AQUA$BOLD${name} $BLUE${outOf}: $BOLD${count}$BLUE / $BOLD$total" + timeString,
				if (timer != null) timer / game.time.toFloat() else count / total.toFloat(),
				BossEvent.BossBarColor.BLUE
			)

			if (game.playerInGame(player.uniqueId)) {
				player.sendActionBar(Component.text("$GOLD${round.reminderText(player.uniqueId)}"))
			}
		}
	}
}
