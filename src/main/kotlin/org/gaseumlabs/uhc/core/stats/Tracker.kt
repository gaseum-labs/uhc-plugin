package org.gaseumlabs.uhc.core.stats;

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Block
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.floor

class Tracker {
	val POSITION_CHUNK_SIZE = 512
	val playerLocations = HashMap<UUID, LinkedList<ArrayList<Int>>>()

	fun addPlayerPosition(uuid: UUID, block: Block) {
		val position = packPosition(block.x.toShort(),
			block.z.toShort(),
			block.world.environment,
			PlayerData.getPlayerData(uuid).lifeNo)

		val list = playerLocations.getOrPut(uuid) {
			val firstChunk = LinkedList<ArrayList<Int>>()
			firstChunk.add(ArrayList())
			firstChunk
		}

		val chunk = list.last

		if (chunk.size == POSITION_CHUNK_SIZE) {
			val newChunk = ArrayList<Int>(POSITION_CHUNK_SIZE)
			newChunk.add(position)
			list.add(newChunk)

		} else {
			chunk.add(position)
		}
	}

	fun generateImage(border: Int, environment: World.Environment, zoom: Int): BufferedImage {
		/* prepare map part */
		val imageSize = (border * 2 + 1) / zoom
		val pixels = IntArray(imageSize * imageSize) { BLACK }
		val environmentMatch = environment !== World.Environment.NORMAL

		/* prepare name column part */
		val playerNames = playerLocations.map { (uuid, _) -> Bukkit.getOfflinePlayer(uuid).name ?: "unknown" }
		val columnWidth = (playerNames.map { stringWidth(it) }.maxOrNull() ?: 0) + 2
		val namesPixels = IntArray(columnWidth * imageSize) { 0xFF3F3F3F.toInt() }

		var playerIndex = 0

		/* render trail */
		playerLocations.forEach { (_, trail) ->
			val (red, gre, blu) = selectColor(playerIndex, playerLocations.size)

			val textY = imageSize - (CHAR_HEIGHT + 1) * (playerIndex + 1)
			/* attempt to draw name (if it will fit in image) */
			if (textY >= 0) writeString(
				1, textY,
				playerNames[playerIndex],
				namesPixels, columnWidth,
				red.shl(16).or(gre.shl(8)).or(blu).or(BLACK)
			)

			val trailSize = (trail.size - 1) * POSITION_CHUNK_SIZE + trail.last.size

			++playerIndex

			/* for connecting lines to */
			var lastPosition = trail.firstOrNull()?.firstOrNull() ?: 0

			trail.forEachIndexed { i, chunk ->
				chunk.forEachIndexed { j, position ->
					val (x, z, e, n) = unpackPosition(position)

					/* is this pixel for the current world */
					if (e == environmentMatch) {
						val (lx, lz, le, ln) = unpackPosition(lastPosition)
						lastPosition = position

						val imgLastX = Util.floorDiv((lx + border), zoom)
						val imgLastY = Util.floorDiv((lz + border), zoom)
						val imgX = Util.floorDiv((x + border), zoom)
						val imgY = Util.floorDiv((z + border), zoom)

						val color = alongColor(red, gre, blu, (i * POSITION_CHUNK_SIZE + j) / trailSize.toFloat())

						val dx = StrictMath.abs(imgLastX - imgX)
						val dy = StrictMath.abs(imgLastY - imgY)

						/* need to draw line */
						if (e == le && n == ln && (dy >= 2 || dx >= 2)) {
							/* y = mx + b */
							if (dx > dy) {
								val slope = (imgLastY - imgY) / (imgLastX - imgX).toFloat()
								val rise = imgLastY - slope * imgLastX

								val (minX, maxX) = minMax(imgLastX, imgX)

								for (k in minX..maxX) {
									val l = floor(slope * k + rise).toInt()
									trySetColor(pixels, imageSize, k, l, color)
								}
								/* x = my + b */
							} else {
								val slope = (imgLastX - imgX) / (imgLastY - imgY).toFloat()
								val push = imgLastX - slope * imgLastY

								val (minY, maxY) = minMax(imgLastY, imgY)

								for (l in minY..maxY) {
									val k = floor(slope * l + push).toInt()
									trySetColor(pixels, imageSize, k, l, color)
								}
							}

							/* single pixel */
						} else {
							trySetColor(pixels, imageSize, imgX, imgY, color)
						}
					}
				}
			}
		}

		val image = BufferedImage(imageSize + columnWidth, imageSize, BufferedImage.TYPE_INT_ARGB)
		image.setRGB(0, 0, imageSize, imageSize, pixels, 0, imageSize)
		image.setRGB(imageSize, 0, columnWidth, imageSize, namesPixels, 0, columnWidth)

		return image
	}

	companion object {
		data class Character(val pixels: BooleanArray, val width: Int)

		const val BLACK = 0xff000000.toInt()
		const val CHAR_HEIGHT = 5

		val characterList = Array(256) { Character(BooleanArray(0), 0) }

		private fun extractSection(x: Int, y: Int, width: Int, height: Int, image: BufferedImage): BooleanArray {
			val array = IntArray(width * height)

			image.getRGB(x, y, width, height, array, 0, width)

			return BooleanArray(array.size) { i ->
				array[i] == 0xffffffff.toInt()
			}
		}

		fun loadCharacters() {
			val characterImage = ImageIO.read(this::class.java.getResourceAsStream("/letters.png"))

			characterList['_'.code] = Character(extractSection(0, 12, 3, CHAR_HEIGHT, characterImage), 3)

			for (i in '0'..'9') {
				characterList[i.code] = Character(
					extractSection((i - '0') * 4, 6, 3, CHAR_HEIGHT, characterImage), 3
				)
			}

			val letterWidths = arrayOf(3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 5, 4, 3, 3, 4, 3, 3, 3, 3, 3, 5, 3, 3, 3)

			var advance = 0
			for (i in 'a'..'z') {
				val width = letterWidths[i - 'a']
				val character = Character(
					extractSection(advance, 0, width, CHAR_HEIGHT, characterImage), width
				)

				characterList[i.code] = character
				characterList[i.code - ('a' - 'A')] = character

				advance += width + 1
			}
		}

		/* INTERNAL CHARACTERS */

		private fun stringWidth(string: String): Int {
			return string.fold(string.length - 1) { acc, c -> acc + characterList[c.code].width }
		}

		private fun writeString(x: Int, y: Int, string: String, array: IntArray, width: Int, color: Int) {
			var advance = x

			string.forEach { c ->
				val character = characterList[c.code]

				for (i in 0 until character.width) {
					for (j in 0 until CHAR_HEIGHT) {
						if (character.pixels[j * character.width + i]) {
							array[(j + y) * width + (i + advance)] = color
						}
					}
				}

				advance += character.width + 1
			}
		}

		/* INTERNAL */

		private fun packPosition(x: Short, z: Short, environment: World.Environment, lifeNo: Int): Int {
			return (x.toInt().and(0xFFF).shl(12))
				.or(z.toInt().and(0xFFF))
				.or((if (environment === World.Environment.NORMAL) 0 else 1).shl(24))
				.or((lifeNo % 2).shl(25))
		}

		private fun threeByteShort(int: Int) = (
		if (int.ushr(11).and(1) == 1)
			int.or(0xf000)
		else
			int.and(0x0fff)
		).toShort()

		private data class Position(val x: Short, val z: Short, val environment: Boolean, val lifeNo: Boolean)

		private fun unpackPosition(packed: Int): Position {
			return Position(threeByteShort(packed.ushr(12)),
				threeByteShort(packed),
				packed.ushr(24).and(1) == 1,
				packed.ushr(25).and(1) == 1)
		}

		private fun selectColor(index: Int, total: Int): Triple<Int, Int, Int> {
			val color = Color.HSBtoRGB(index / total.toFloat(), 1.0f, 1.0f)

			return Triple(color.ushr(16).and(0xff), color.ushr(8).and(0xff), color.and(0xff))
		}

		private fun alongColor(red: Int, gre: Int, blu: Int, along: Float): Int {
			return (red * Util.interp(0.1f, 1.0f, along)).toInt().shl(16)
				.or((gre * Util.interp(0.1f, 1.0f, along)).toInt().shl(8))
				.or((blu * Util.interp(0.1f, 1.0f, along)).toInt())
				.or(BLACK)
		}

		private fun inBound(x: Int, y: Int, size: Int): Boolean {
			return x >= 0 && y >= 0 && x < size && y < size
		}

		private fun minMax(first: Int, second: Int): Pair<Int, Int> {
			return if (first < second) Pair(first, second) else Pair(second, first)
		}

		private fun trySetColor(array: IntArray, size: Int, x: Int, y: Int, color: Int) {
			val i = y * size + x

			if (inBound(x, y, size) && array[i] == BLACK) {
				array[i] = color
			}
		}
	}
}
