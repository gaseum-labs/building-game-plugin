package game

import BGPlayer
import BarManager
import BuildingGamePlugin
import PlayerData
import Teams
import Util
import WorldManager
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration.BOLD
import org.bukkit.Bukkit
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
		if (ongoing != null) return "game is already running"

		val gamePlayers = PlayerData.all().filter { (_, playerData) -> playerData.participating }.map { (uuid, _) -> uuid }

		val requiredPlayers = if (pregameSetup.imposter) 4 else 2
		if (gamePlayers.size < requiredPlayers) return "Need at least $requiredPlayers players to start"

		val gameWorld = WorldManager.refreshGameWorld()

		pregameSetup.imposter = type === GameType.IMPOSTER
		val game = Game(gameWorld, gamePlayers, pregameSetup)
		game.startNextRound()

		ongoing = game

		PlayerData.all().forEach { (_, playerData) -> playerData.participating = false }

		return null
	}

	fun stopGame() {
		val game = ongoing ?: return
		ongoing = null
		game.gamePlayers.map(BGPlayer::getPlayer).forEach(Teams::updatePlayer)
		PlayerData.cleanData()
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
				stopGame()
			}
		}
	}

	/* happens every second (20 ticks) */
	fun updateBossBarAndHotbar(player: Player) {
		val game = ongoing

		if (game == null) {
			BarManager.newBossBar(player, Component.text("Building game"), 1.0f, BossBar.Color.WHITE)
			val playerData = PlayerData.getOrCreate(player)

			player.sendActionBar(
				if (playerData.participating) {
					Component.text("You have joined, Use /bg leave to leave the game", GREEN)
				} else {
					Component.text("Use /bg join to join the game", RED)
				}
			)


		} else {
			val round = game.currentRound()
			val (name, outOf) = round.barText()
			val (count, total) = round.progress()

			val timer = game.timer

			val component = Component.text(name, AQUA, BOLD).append(
				Component.text(" $outOf: ", BLUE)
			).append(
				Component.text(count, BLUE, BOLD)
			).append(
				Component.text(" / ", BLUE)
			).append(
				Component.text(total, BLUE, BOLD)
			).append(if (timer != null)
				Component.text(" Time Remaining ", AQUA).append(Component.text(Util.timeString(timer), WHITE, BOLD))
			 else
				Component.empty()
			)

			BarManager.newBossBar(
				player,
				component,
				if (timer != null) timer / game.time.toFloat() else count / total.toFloat(),
				BossBar.Color.BLUE
			)

			if (game.playerInGame(player.uniqueId)) {
				player.sendActionBar(Component.text(round.reminderText(player.uniqueId), GOLD))
			}
		}
	}
}
