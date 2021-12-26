package round

import game.Game
import java.util.*

class RoomAccess(val game: Game, var round: Round, var room: Room) {
	companion object {
		/**
		 * room is set to the first room in the round
		 */
		fun at(game: Game, round: Round): RoomAccess {
			return RoomAccess(game, round, round.rooms.first())
		}

		/**
		 * room is set to the first room in the round by default
		 */
		fun at(game: Game, roundIndex: Int, roomIndex: Int = 0): RoomAccess {
			val round = game.rounds[roundIndex]
			return RoomAccess(game, round, round.rooms[roomIndex])
		}

		fun at(game: Game, roundIndex: Int, uuid: UUID): RoomAccess {
			val round = game.rounds[roundIndex]
			return RoomAccess(game, round, round.rooms[game.grid.indexOnRow(round.index, game.playersIndex(uuid))])
		}

		fun at(game: Game, round: Round, uuid: UUID): RoomAccess {
			return RoomAccess(game, round, round.rooms[game.grid.indexOnRow(round.index, game.playersIndex(uuid))])
		}
	}

	/**
	 * move to this player's room on the current round
	 */
	fun jumpToPlayer(uuid: UUID): RoomAccess {
		room = round.rooms[game.grid.indexOnRow(round.index, game.playersIndex(uuid))]

		return this
	}

	/**
	 * move the round while keeping the room consistent
	 */
	fun traverse(offset: Int): RoomAccess {
		round = game.rounds[round.index + offset]
		room = round.rooms[room.index]

		return this
	}

	fun copy(): RoomAccess {
		return RoomAccess(game, round, room)
	}

	fun <T: Round> round(): T {
		return round as T
	}
}
