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
				PlayerData.list.map { (uuid, _) -> Bukkit.getOfflinePlayer(uuid).name }
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

		fun <T : Round>roundFilter(sender: CommandSender, op: Boolean = false): Pair<Player, T>? {
			val player = filter(sender, op) ?: return null

			val game = GameRunner.ongoing
			if (game == null) {
				errorMessage(player, "The game is not going")
				return null
			}

			if (!game.gamePlayers.contains(player.uniqueId)) {
				errorMessage(player, "You must be playing the game to use this command")
			}

			val round = game.currentRound() as? T
			if (round == null) {
				errorMessage(player, "You can't use this command this round")
				return null
			}

			return Pair(player, round)
		}
	}

	@Subcommand("join")
	fun joinCommand(sender: CommandSender) {
		val player = filter(sender) ?: return

		if (GameRunner.ongoing != null) {
			return errorMessage(player, "The game has already started")
		}

		val playerData = PlayerData.get(player.uniqueId)

		if (!playerData.participating) {
			playerData.participating = true
			Teams.updatePlayer(player)
			sendMessage(player, "You have joined the game")

		} else {
			errorMessage(player, "You have already joined, use /leave to unjoin")
		}
	}

	@Subcommand("setParticipating")
	@CommandCompletion("@gamePlayer")
	fun forceJoinCommand(sender: CommandSender, participating: Boolean, player: OfflinePlayer) {
		filter(sender, true) ?: return

		val playerData = PlayerData.get(player.uniqueId)

		playerData.participating = participating

		val onlinePlayer = Bukkit.getPlayer(player.uniqueId)
		if (onlinePlayer != null) Teams.updatePlayer(onlinePlayer)

		sendMessage(sender, "${player.name}'s participating has been set to $participating")
	}

	@Subcommand("leave")
	fun leaveCommand(sender: CommandSender) {
		val player = filter(sender) ?: return

		if (GameRunner.ongoing != null) {
			return errorMessage(player, "The game has already started")
		}

		val playerData = PlayerData.get(player.uniqueId)

		if (playerData.participating) {
			playerData.participating = false
			Teams.updatePlayer(player)
			sendMessage(player, "You have left the game")

		} else {
			errorMessage(player, "You haven't joined yet, user /join to join")
		}
	}

	@Subcommand("start")
	fun startCommand(sender: CommandSender, type: GameType) {
		val player = filter(sender, true) ?: return

		val errorMessage = GameRunner.startGame(type)

		if (errorMessage != null) {
			errorMessage(player, errorMessage)
		} else {
			sendMessage(player, "game.Game starting")
		}
	}

	/* game commands */
	@CommandAlias("prompt")
	fun promptCommand(sender: CommandSender, prompt: String) {
		val (player, round) = roundFilter<EntryRound>(sender) ?: return

		if (round.submit(player.uniqueId, prompt)) {
			sendMessage(player, "Submitted prompt, you may /prompt again to update your prompt")
		} else {
			errorMessage(player, "Your prompt was empty or contained illegal characters, please resubmit")
		}
	}

	@CommandAlias("next")
	fun nextCommand(sender: CommandSender) {
		val (player, round) = roundFilter<TourRound>(sender, true) ?: return

		if (++round.tourAlong == round.tourSize()) {
			sendMessage(player, "End of tour reached")
		} else {
			round.startTour(round.tourAlong)
		}
	}

	@CommandAlias("done")
	fun doneCommand(sender: CommandSender) {
		val (player, round) = roundFilter<BuildRound>(sender) ?: return

		if (round.game.playersIndex(player.uniqueId) == -1) {
			return errorMessage(player, "You are not playing")
		}

		if (round.donePlayers.contains(player.uniqueId)) {
			errorMessage(player, "You are already done")

		} else {
			round.donePlayers.add(player.uniqueId)
			Teams.updatePlayer(player)
			sendMessage(player, "Marked as done, you may keep building until everyone is done")
		}
	}

	@CommandAlias("guess")
	fun guessCommand(sender: CommandSender, prompt: String) {
		val (player, round) = roundFilter<GuessRound>(sender) ?: return

		if (round.submit(player.uniqueId, prompt)) {
			sendMessage(player, "Submitted guess, you may /guess again to update your guess")
		} else {
			errorMessage(player, "Your guess was empty or contained illegal characters, please resubmit")
		}
	}

	@CommandAlias("vote")
	@CommandCompletion("@gamePlayer")
	fun voteCommand(sender: CommandSender, votePlayer: OfflinePlayer) {
		val (player, round) = roundFilter<VoteRound>(sender) ?: return

		val voteUUID = votePlayer.uniqueId

		if (round.game.gamePlayers.contains(voteUUID)) {
			round.votes[player.uniqueId] = voteUUID
			Teams.updatePlayer(player)

			/* create the reveal order once everyone has voted */
			if (round.votes.size == round.game.numPlayers()) {
				val imposter = RoomAccess.at(round.game, round).traverse(-1).round<ImposterRound>().imposter

				/* don't include imposter yet */
				val voteCounts = round.votes
					.filter { (player, _) -> player != imposter }
					.map { (player, _) -> Pair(player, round.votes.filterValues { it == player }.size) }
					.sortedBy { (_, count) -> count } as MutableList<Pair<UUID, Int>>

				/* place imposter last */
				voteCounts.add(Pair(imposter, round.votes.filterValues { it == imposter }.size))

				round.revealOrder.addAll(voteCounts)
			}

		} else {
			errorMessage(player, "That player isn't playing")
		}
	}

	@Subcommand("forceEnd")
	fun forceEndCommand(sender: CommandSender) {
		val (player, round) = roundFilter<Round>(sender, true) ?: return

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
}
