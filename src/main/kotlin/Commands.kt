import co.aikar.commands.BaseCommand
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import game.GameRunner
import game.GameType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import round.Room
import round.RoomAccess
import round.Round
import round.rounds.*
import java.util.*

@CommandAlias("bg")
class Commands : BaseCommand() {
	companion object {
		fun registerCompletions(commandManager: PaperCommandManager) {
			commandManager.commandCompletions.registerCompletion("gameplayer") {
				PlayerData.all().map { (_, data) -> data.name }
			}
		}

		fun sendMessage(audience: Audience, message: String) {
			audience.sendMessage(Component.text(message, TextColor.color(0x8d4953), TextDecoration.BOLD))
		}

		fun errorMessage(audience: Audience, message: String) {
			audience.sendMessage(Component.text(message, TextColor.color(0xf62705), TextDecoration.ITALIC))
		}

		fun filter(sender: CommandSender, op: Boolean = false): Player? {
			return if (sender is Player) {
				if (op && !sender.isOp) {
					errorMessage(sender, "You must be an op to use this command")
					null
				} else {
					sender
				}
			} else {
				errorMessage(sender, "Command must be performed by a player")
				null
			}
		}

		fun consoleOpFilter(sender: CommandSender): CommandSender? {
			return if (sender.isOp) sender else {
				errorMessage(sender, "You must be an op to use this command")
				null
			}
		}

		fun <T : Round>roundFilter(sender: CommandSender, op: Boolean, clazz: Class<T>): Pair<Player, T>? {
			val player = filter(sender, op) ?: return null

			val game = GameRunner.ongoing
			if (game == null) {
				errorMessage(player, "The game is not going")
				return null
			}

			if (!game.gamePlayers.contains(player.uniqueId)) {
				errorMessage(player, "You must be playing the game to use this command")
			}

			val round = game.currentRound()
			if (!clazz.isInstance(round)) {
				errorMessage(player, "You can't use this command this round")
				return null
			}

			return Pair(player, round as T)
		}
	}

	@Subcommand("createBots")
	fun createBotsCommand(sender: CommandSender, count: Int) {
		consoleOpFilter(sender) ?: return

		for (i in 0 until count) PlayerData.createDummy()

		sendMessage(sender, "Created ${count} bots")
	}

	@Subcommand("killBots")
	fun killBotsCommand(sender: CommandSender) {
		consoleOpFilter(sender) ?: return

		val game = GameRunner.ongoing
		if (game != null) return errorMessage(sender, "Can't kill bots when game is going")

		PlayerData.clearDummies()
		sendMessage(sender, "Killed all bots")
	}

	@Subcommand("join")
	fun joinCommand(sender: CommandSender) {
		val player = filter(sender) ?: return

		if (GameRunner.ongoing != null) {
			return errorMessage(player, "The game has already started")
		}

		val bgPlayer = BGPlayer.from(player)

		if (!bgPlayer.playerData.participating) {
			bgPlayer.playerData.participating = true
			Teams.updatePlayer(bgPlayer)
			sendMessage(player, "You have joined the game")

		} else {
			errorMessage(player, "You have already joined, use /leave to unjoin")
		}
	}

	@Subcommand("setParticipating")
	@CommandCompletion("@gamePlayer")
	fun forceJoinCommand(sender: CommandSender, playerName: String, participating: Boolean) {
		consoleOpFilter(sender) ?: return

		if (GameRunner.ongoing != null)
			return errorMessage(sender, "Game is already going")

		val bgPlayer = BGPlayer.getPlayerName(playerName)
			?: return errorMessage(sender, "That player doesn't exist")

		bgPlayer.playerData.participating = participating
		Teams.updatePlayer(bgPlayer)

		sendMessage(sender, "${bgPlayer.name}'s participating has been set to $participating")
	}

	@Subcommand("setParticipatingAll")
	fun forceJoinCommand(sender: CommandSender) {
		consoleOpFilter(sender) ?: return

		if (GameRunner.ongoing != null)
			return errorMessage(sender, "Game is already going")

		val allPlayerData = PlayerData.all()

		allPlayerData.forEach { (uuid, playerData) ->
			playerData.participating = true
			Teams.updatePlayer(BGPlayer.getPlayer(uuid))
		}

		sendMessage(sender, "Set ${allPlayerData.size} players to participating")
	}

	@Subcommand("leave")
	fun leaveCommand(sender: CommandSender) {
		val player = filter(sender) ?: return

		if (GameRunner.ongoing != null) {
			return errorMessage(player, "The game has already started")
		}

		val bgPlayer = BGPlayer.from(player)

		if (bgPlayer.playerData.participating) {
			bgPlayer.playerData.participating = false
			Teams.updatePlayer(bgPlayer)
			sendMessage(player, "You have left the game")

		} else {
			errorMessage(player, "You haven't joined yet, user /join to join")
		}
	}

	@Subcommand("start")
	fun startCommand(sender: CommandSender, type: GameType) {
		consoleOpFilter(sender) ?: return

		val errorMessage = GameRunner.startGame(type)

		if (errorMessage != null) {
			errorMessage(sender, errorMessage)
		} else {
			sendMessage(sender, "Game starting")
		}
	}

	@Subcommand("lobby")
	fun lobbyCommand(sender: CommandSender) {
		consoleOpFilter(sender) ?: return

		GameRunner.stopGame()

		val gameWorld = WorldManager.gameWorld ?: return errorMessage(sender, "There is not game world")
		val lobbyLocation = Teams.lobbyLocation()

		gameWorld.players.forEach { player ->
			player.teleport(lobbyLocation)
		}

		sendMessage(sender, "Players returned to lobby")
	}

	/* game commands */
	@CommandAlias("prompt")
	fun promptCommand(sender: CommandSender, prompt: String) {
		val (player, round) = roundFilter(sender, false, EntryRound::class.java) ?: return

		if (round.submit(player.uniqueId, prompt)) {
			sendMessage(player, "Submitted prompt, you may /prompt again to update your prompt")
		} else {
			errorMessage(player, "Your prompt was empty or contained illegal characters, please resubmit")
		}
	}

	@CommandAlias("next")
	fun nextCommand(sender: CommandSender) {
		val (player, round) = roundFilter(sender, true, TourRound::class.java) ?: return

		if (++round.tourAlong == round.tourSize()) {
			sendMessage(player, "End of tour reached")
		} else {
			round.startTour(round.tourAlong)
		}
	}

	@CommandAlias("done")
	fun doneCommand(sender: CommandSender) {
		val (player, round) = roundFilter(sender, true, AbstractBuildRound::class.java) ?: return

		if (round.game.playersIndex(player.uniqueId) == -1) {
			return errorMessage(player, "You are not playing")
		}

		if (round.donePlayers.contains(player.uniqueId)) {
			errorMessage(player, "You are already done")

		} else {
			round.makeDone(player.uniqueId)
			sendMessage(player, "Marked as done, you may keep building until everyone is done")
		}
	}

	@CommandAlias("guess")
	fun guessCommand(sender: CommandSender, prompt: String) {
		val (player, round) = roundFilter(sender, false, GuessRound::class.java) ?: return

		if (round.submit(player.uniqueId, prompt)) {
			sendMessage(player, "Submitted guess, you may /guess again to update your guess")
		} else {
			errorMessage(player, "Your guess was empty or contained illegal characters, please resubmit")
		}
	}

	@CommandAlias("vote")
	@CommandCompletion("@gamePlayer")
	fun voteCommand(sender: CommandSender, candidateName: String) {
		val (player, round) = roundFilter(sender, false, VoteRound::class.java) ?: return

		val candidate = BGPlayer.getPlayerName(candidateName)
			?: return errorMessage(player, "That player doesn't exist")

		if (round.vote(player.uniqueId, candidate.uuid)) {
			sendMessage(player, "Voted for ${candidate.name}")
		} else {
			errorMessage(player, "That player isn't playing")
		}
	}

	@Subcommand("forceEnd")
	fun forceEndCommand(sender: CommandSender) {
		val (player, round) = roundFilter(sender, true, Round::class.java) ?: return

		when (round) {
			is AbstractTextRound -> {
				round.game.gamePlayers.forEachIndexed { i, uuid ->
					if (!round.submittedPlayers.contains(uuid)) {
						round.prompts[i] = "[Unsubmitted Prompt] (Do what you want)"
						round.submittedPlayers.add(uuid)
					}
				}
			}
			is AbstractBuildRound -> {
				round.game.gamePlayers.forEach { uuid ->
					if (!round.donePlayers.contains(uuid)) round.donePlayers.add(uuid)
				}
			}
			is VoteRound -> {
				round.game.gamePlayers.forEach { uuid ->
					round.votes.putIfAbsent(uuid, round.game.gamePlayers.random())
				}
			}
			else -> errorMessage(player, "Force is not available for this round")
		}
	}

	@CommandAlias("spectate")
	fun spectateCommand(sender: CommandSender) {
		val player = filter(sender) ?: return

		val world = player.world

		if (world === WorldManager.gameWorld) {
			player.teleport(Teams.lobbyLocation())
			player.gameMode = GameMode.CREATIVE

		} else {
			val gameWorld = WorldManager.gameWorld ?: return errorMessage(sender, "Wait a sec, the game world doesn't exist yet")

			player.teleport(Location(gameWorld, 0.5, Room.FLOOR_Y + 1.0, 0.5))
			player.gameMode = GameMode.SPECTATOR
		}
	}

	@CommandAlias("buildTime")
	fun buildTimeCommand(sender: CommandSender, minutes: Int) {
		consoleOpFilter(sender) ?: return

		GameRunner.pregameSetup.buildTime = minutes * 60
		GameRunner.ongoing?.setup?.buildTime = minutes * 60

		sendMessage(sender, "Set build time to ${minutes} minutes")
	}
}
