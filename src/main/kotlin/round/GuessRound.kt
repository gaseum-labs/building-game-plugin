package round

import Game
import Room
import Round
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

class GuessRound(game: Game) : AbstractTextRound(game) {
	override fun generateRooms(x: Int, z: Int, numPlayers: Int): Array<Room>? = null

	override fun progress(): Pair<Int, Int> = Pair(numSubmitted(), game.numPlayers())

	override fun getTime(): Int? = null

	override fun doRoomTeleport() = true

	override fun postRoomsBuild() {}

	/* display */

	override fun splashText(uuid: UUID): Pair<String, String> {
		return Pair("${ChatColor.LIGHT_PURPLE}Guess!", "")
	}

	override fun barText(): Pair<String, String> {
		return Pair("Guess", "Guesses")
	}

	override fun reminderText(uuid: UUID, reminderIndex: Int): String {
		return "Use /guess to guess the original prompt of this build"
	}

	override fun tourText(tourData: TourData): Pair<String, String> {
		return Pair("${tourData.name}'s guess", (tourData.round as GuessRound).prompts[tourData.col])
	}
}
