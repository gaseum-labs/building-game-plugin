package round

import BGPlayer
import WorldManager
import org.bukkit.Material
import round.rounds.*
import kotlin.random.Random

object FakeBuilder {
	private fun build(block: Material, room: Room) {
		val area = room.area as Room.Area

		val left = room.x + area.x + area.width / 4
		val up = room.z + area.z + area.depth / 4
		val width = area.width / 2 + 1
		val depth = area.depth / 2 + 1

		for (x in left until left + width) {
			for (z in up until up + depth) {
				for (y in Room.FLOOR_Y + 1 until Room.FLOOR_Y + 12) {
					if (Random.nextBoolean()) {
						WorldManager.gameWorld!!.getBlockAt(x, y, z).setType(block, false)
					}
				}
			}
		}
	}

	private fun getPrompt(): String {
		return (0 until 3).joinToString(" ") { nouns.random() }
	}

	fun performRoundAction(player: BGPlayer, round: Round, room: Room) {
		if (round is ImposterRound) {
			if (player.uuid != round.originalBuilder) {
				build(
					if (round.imposter == player.uuid)
						Material.RED_CONCRETE
					else
						Material.GREEN_CONCRETE,
					room
				)

				round.makeDone(player.uuid)
			}
		} else if (round is AbstractBuildRound) {
			build(player.playerData.buildingBlock, room)
			round.makeDone(player.uuid)
		} else if (round is AbstractTextRound) {
			round.submit(player.uuid, getPrompt())
		} else if (round is VoteRound) {
			round.vote(player.uuid, round.game.gamePlayers.random())
		}
	}

	private val nouns = this::class.java.getResource("/english-nouns.txt").readText().lines()
}
