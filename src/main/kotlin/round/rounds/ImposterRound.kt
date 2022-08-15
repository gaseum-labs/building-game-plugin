package round.rounds

import BGPlayer
import game.Game
import round.Room
import round.RoomAccess
import round.TourData
import java.util.*

class ImposterRound(game: Game, rooms: Array<Room>, index: Int) : AbstractBuildRound(game, rooms, index) {
	lateinit var originalBuilder: UUID
	lateinit var originalGuesser: UUID
	lateinit var imposter: UUID

	lateinit var prompt: String
	lateinit var corruptedPrompt: String

	override fun postRoomsBuild() {
		originalBuilder = game.getOriginalBuilder(this)
		originalGuesser = game.getOriginalGuesser(this)
		imposter = game.getImposter(this)

		val originalBuild = RoomAccess.at(game, 1, originalBuilder)

		prompt = AbstractTextRound.getSubmitted(originalBuild.copy().traverse(-1))
		corruptedPrompt = AbstractTextRound.getSubmitted(originalBuild.copy().traverse(1))

		/* copy the original builders build over */
		val oldBuildRoom = originalBuild.room
		val newBuildRoom = RoomAccess.at(game, this, originalBuilder).room

		Room.copyArea(game.world, oldBuildRoom, newBuildRoom)

		makeDone(originalBuilder)
	}

	/* would spoil who the original builder is */
	override fun isPlayerReady(uuid: UUID): Boolean = false

	/* display */

	override fun splashText(uuid: UUID): Triple<String, String, String?> {
		return when (uuid) {
			originalBuilder -> Triple("Your Build!", "You're the original builder for this round", null)
			originalGuesser -> Triple("Build your guess!", "You're the original guesser for this round", null)
			imposter -> Triple("You're the imposter!", corruptedPrompt, "Use /done when you are finished")
			else -> Triple("Innocent!", prompt, "Use /done when you are finished")
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
