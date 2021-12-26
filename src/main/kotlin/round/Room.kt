package round

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import kotlin.math.atan2

class Room (
	val x: Int,
	val z: Int,
	val width: Int,
	val depth: Int,
	val spawnX: Int,
	val spawnZ: Int,
	val area: Area?,
	val index: Int,
) {
	data class Area (val x: Int, val z: Int, val width: Int, val depth: Int)

	companion object {
		const val FLOOR_Y = 60
		const val WALL_HEIGHT = 48
		const val FLOOR_DESCEND = 12

		val FLOOR_BLOCK = Material.BROWN_TERRACOTTA
		val AREA_BLOCK = Material.SNOW_BLOCK
		val WALL_BLOCK = Material.GRAY_TERRACOTTA

		fun copyArea(world: World, fromRoom: Room, toRoom: Room) {
			val (aX0, aZ0, aW, aD) = fromRoom.area ?: return
			val (aX1, aZ1, _, _) = toRoom.area ?: return

			for (i in 0 until aW) {
				for (k in 0 until aD) {
					for (j in FLOOR_Y - FLOOR_DESCEND until FLOOR_Y + WALL_HEIGHT) {
						val fromBlock = world.getBlockAt(aX0 + i, j, aZ0 + k)
						val toBlock = world.getBlockAt(aX1 + i, j, aZ1 + k)

						toBlock.setBlockData(fromBlock.blockData, false)
					}
				}
			}
		}

		fun generateSmall(
			x: Int,
			z: Int,
			roomSize: Int,
			buildSize: Int,
			holdingSize: Int,
			numPlayers: Int
		): Array<Room> {
			val offsetX = (roomSize - holdingSize) / 2

			return Array(numPlayers) { i -> Room(
				x + i * roomSize + offsetX, z,
				holdingSize,
				holdingSize,
				holdingSize / 2,
				holdingSize / 2,
				null,
				i
			)
			}
		}

		fun generate(
			x: Int,
			z: Int,
			roomSize: Int,
			buildSize: Int,
			holdingSize: Int,
			numPlayers: Int
		): Array<Room> {
			return Array(numPlayers) { i -> Room(
				x + i * roomSize, z,
				roomSize,
				roomSize,
				roomSize / 2,
				(roomSize - buildSize) / 4,
				Area(
					(roomSize - buildSize) / 2,
					(roomSize - buildSize) / 2,
					buildSize,
					buildSize
				),
				i
			)
			}
		}

		fun doNotGenerate(
			x: Int,
			z: Int,
			roomSize: Int,
			buildSize: Int,
			holdingSize: Int,
			numPlayers: Int
		): Array<Room>? {
			return null
		}
	}

	fun spawnLocation(world: World): Location {
		val centerX = x + width / 2.0
		val centerZ = z + depth / 2.0
		val tpX = x + spawnX + 0.5
		val tpZ = z + spawnZ + 0.5

		val angle = atan2(centerZ - tpZ, centerX - tpX).toFloat()

		return Location(world, tpX, FLOOR_Y + 1.0, tpZ, 0.0f, angle)
	}

	/* the wall in which x is negative */
	fun destroyLeftWall(world: World) {
		for (k in z + 1 until z + depth - 1) {
			for (j in FLOOR_Y + 1 until FLOOR_Y + WALL_HEIGHT) {
				world.getBlockAt(x, j, k)
			}
		}
	}

	/* the wall in which x is positive */
	fun destroyRightWall(world: World) {
		for (k in z + 1 until z + depth - 1) {
			for (j in FLOOR_Y + 1 until FLOOR_Y + WALL_HEIGHT) {
				world.getBlockAt(x + width - 1, j, k)
			}
		}
	}

	fun build(world: World) {
		/* floor */
		for (i in x until x + width) {
			for (k in z until z + depth) {
				for (j in FLOOR_Y downTo FLOOR_Y - FLOOR_DESCEND) {
					world.getBlockAt(i, j, k).setType(FLOOR_BLOCK, false)
				}
			}
		}

		/* area */
		if (area != null) {
			val (aX, aZ, aW, aD) = area
			for (i in x + aX until x + aX + aW) {
				for (k in z + aZ until z + aZ + aD) {
					world.getBlockAt(i, FLOOR_Y, k).setType(AREA_BLOCK, false)
				}
			}
		}

		/* spawn */
		world.getBlockAt(spawnX, FLOOR_Y, spawnZ).setType(Material.LAPIS_BLOCK, false)

		/* clear out the air */
		for (i in x + 1 until x + width - 1) {
			for (k in z + 1 until z + depth - 1) {
				for (j in FLOOR_Y + 1 until FLOOR_Y + WALL_HEIGHT) {
					world.getBlockAt(i, j, k).setType(Material.AIR, false)
				}
			}
		}

		/* top and bottom walls */
		for (j in FLOOR_Y + 1 until FLOOR_Y + WALL_HEIGHT) {
			for (i in x until x + width) {
				world.getBlockAt(i, j, z).setType(WALL_BLOCK, false)
				world.getBlockAt(i, j, z + depth - 1).setType(WALL_BLOCK, false)
			}

			for (k in z + 1 until z + width - 1) {
				world.getBlockAt(x, j, k).setType(WALL_BLOCK, false)
				world.getBlockAt(x + width - 1, j, k).setType(WALL_BLOCK, false)
			}
		}
	}
}
