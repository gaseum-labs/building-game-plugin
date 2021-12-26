package round

import game.Game
import round.rounds.*

enum class RoundType(
	val generateRooms: (x: Int, z: Int, r: Int, b: Int, h: Int, numPlayers: Int) -> Array<Room>?,
	val create: (game: Game, rooms: Array<Room>, index: Int) -> Round
) {
	ENTRY(Room.Companion::generateSmall, ::EntryRound),
	BUILD(Room.Companion::generate, ::BuildRound),
	GUESS(Room.Companion::doNotGenerate, ::GuessRound),
	TOUR(Room.Companion::doNotGenerate, ::TourRound),
	IMPOSTER(Room.Companion::generate, ::ImposterRound),
	VOTE(Room.Companion::doNotGenerate, ::VoteRound)
}
