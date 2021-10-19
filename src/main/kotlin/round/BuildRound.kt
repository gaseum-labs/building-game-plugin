package round

import Game
import Room
import Round
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

class BuildRound(game: Game) : Round(game) {
	val donePlayers = ArrayList<UUID>()

	override fun generateRooms(x: Int, z: Int, numPlayers: Int): Array<Room> {
		return Array(numPlayers) { i ->
			val roomSize = game.setup.roomSize
			val buildSize = game.setup.buildSize

			Room(
				x + i * roomSize, z,
				roomSize,
				roomSize,
				roomSize / 2,
				(roomSize - buildSize) / 4,
				arrayOf(
					Room.Area(
						(roomSize - buildSize) / 2,
						(roomSize - buildSize) / 2,
						buildSize,
						buildSize
					)
				)
			)
		}
	}

	override fun progress(): Pair<Int, Int> = Pair(donePlayers.size, game.numPlayers())

	override fun getTime(): Int = game.setup.buildTime

	override fun isPlayerReady(uuid: UUID): Boolean {
		return donePlayers.contains(uuid)
	}

	override fun doRoomTeleport() = true

	/* display */

	override fun splashText(uuid: UUID): Pair<String, String> {
		val previous = game.previousRound() as AbstractTextRound
		return Pair("${ChatColor.AQUA}Build!", previous.prompts[game.playersRoom(uuid)])
	}

	override fun barText(): Pair<String, String> {
		return Pair("Build", "Builds Complete")
	}

	override fun reminderText(uuid: UUID, reminderIndex: Int): String {
		return if (reminderIndex % 3 == 1 && !donePlayers.contains(uuid)) {
			"Use /done when you are finished"

		} else {
			val previous = game.previousRound() as AbstractTextRound
			"Prompt: ${previous.prompts[game.playersRoom(uuid)]}"
		}
	}

	override fun tourText(tourData: TourData): Pair<String, String> {
		return Pair("${tourData.name}'s build for",
			(game.rounds[tourData.row - 1] as AbstractTextRound).prompts[tourData.col]
		)
	}
}
