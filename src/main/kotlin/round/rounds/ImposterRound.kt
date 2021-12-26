package round.rounds

import game.Game
import round.Room
import org.bukkit.ChatColor
import round.RoomAccess
import round.TourData
import java.util.*

class ImposterRound(game: Game, rooms: Array<Room>, index: Int) : AbstractBuildRound(game, rooms, index) {
	lateinit var originalBuilder: UUID
	lateinit var imposter: UUID

	lateinit var prompt: String
	lateinit var corruptedPrompt: String

	override fun postRoomsBuild() {
		originalBuilder = game.getOriginalBuilder(this)
		imposter = game.getImposter(this)

		val originalBuild = RoomAccess.at(game, 1, originalBuilder)

		prompt = AbstractTextRound.getSubmitted(originalBuild.copy().traverse(-1))
		corruptedPrompt = AbstractTextRound.getSubmitted(originalBuild.copy().traverse(1))

		/* copy the original builders build over */
		val oldBuildRoom = originalBuild.room
		val newBuildRoom = RoomAccess.at(game, this, originalBuilder).room

		Room.copyArea(game.world, oldBuildRoom, newBuildRoom)

		donePlayers.add(originalBuilder)
	}

	/* would spoil who the original builder is */
	override fun isPlayerReady(uuid: UUID): Boolean = false

	/* display */

	override fun splashText(uuid: UUID): Triple<String, String, String?> {
		return when (uuid) {
			originalBuilder -> Triple("${ChatColor.GOLD}Your Build!", "You're the original builder for this round", null)
			imposter -> Triple("${ChatColor.RED}You're the imposter!", corruptedPrompt, "Use /done when you are finished")
			else -> Triple("${ChatColor.GREEN}Innocent!", prompt, "Use /done when you are finished")
		}
	}

	override fun barText(): Pair<String, String> {
		return Pair("Imposter Build", "Builds Complete")
	}

	override fun reminderText(uuid: UUID): String {
		return if (uuid == originalBuilder) {
			"Original prompt: $prompt"
		} else {
			"Prompt: ${if (uuid == imposter) corruptedPrompt else prompt}"
		}
	}

	/* this round gets skipped in the tour always */
	override fun tourText(tourData: TourData): Pair<String, String> {
		return Pair("${tourData.name}'s imposter round build for", prompt)
	}
}
