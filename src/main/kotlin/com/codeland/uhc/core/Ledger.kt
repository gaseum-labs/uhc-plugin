package com.codeland.uhc.core

import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Block
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileWriter
import java.lang.StrictMath.abs
import java.time.LocalDateTime
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

class Ledger {
	val startDate = LocalDateTime.now()

	class Entry(val username: String, val timeSurvived: Int, val killedBy: String, val winning: Boolean)

	val playerList = ArrayList<Entry>()

	val POSITION_CHUNK_SIZE = 512
	val playerLocations = HashMap<UUID, LinkedList<ArrayList<Int>>>()

	fun addEntry(username: String, timeSurvived: Int, killedBy: String?, winning: Boolean = false) {
		/* remove any prior entries for this player */
		/* this would only happen if a player dies not as part of the game and is respawned manually */
		playerList.removeIf { entry -> entry.username == username }

		playerList.add(Entry(username, timeSurvived, killedBy ?: "environment", winning))
	}

	fun packPosition(x: Short, z: Short, environment: World.Environment): Int {
		return (x.toInt().and(0xFFF).shl(12))
			.or(z.toInt().and(0xFFF))
			.or((if (environment === World.Environment.NORMAL) 0 else 1).shl(24))
	}

	fun threeByteShort(int: Int) = (
		if (int.ushr(11).and(1) == 1)
			int.or(0xf000)
		else
			int.and(0x0fff)
	).toShort()

	fun unpackPosition(packed: Int): Triple<Short, Short, Boolean> {
		return Triple(threeByteShort(packed.ushr(12)), threeByteShort(packed), packed.ushr(24).and(1) == 1)
	}

	fun addPlayerPosition(uuid: UUID, block: Block) {
		val position = packPosition(block.x.toShort(), block.z.toShort(), block.world.environment)

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

	fun selectColor(index: Int, total: Int): Triple<Int, Int, Int> {
		val color = Color.HSBtoRGB(index / total.toFloat(), 1.0f, 1.0f)

		return Triple(color.ushr(16).and(0xff), color.ushr(8).and(0xff), color.and(0xff))
	}

	fun alongColor(red: Int, gre: Int, blu: Int, along: Float): Int {
		return (red * Util.interp(0.1f, 1.0f, along)).toInt().shl(16)
			.or((gre * Util.interp(0.1f, 1.0f, along)).toInt().shl(8))
			.or((blu * Util.interp(0.1f, 1.0f, along)).toInt())
			.or(BLACK)
	}

	fun inBound(x: Int, y: Int, size: Int): Boolean {
		return x >= 0 && y >= 0 && x < size && y < size
	}

	fun minMax(first: Int, second: Int): Pair<Int, Int> {
		return if (first < second) Pair(first, second) else Pair(second, first)
	}

	fun trySetColor(array: IntArray, size: Int, x: Int, y: Int, color: Int) {
		val i = y * size + x

		if (inBound(x, y, size) && array[i] == BLACK) {
			array[i] = color
		}
	}

	fun generateImage(border: Int, environment: World.Environment, zoom: Float): BufferedImage {
		/* prepare map part */
		val imageSize = ((border * 2 + 1) / zoom).roundToInt()
		val pixels = IntArray(imageSize * imageSize) { BLACK }
		val environmentMatch = environment !== World.Environment.NORMAL

		/* prepare name column part */
		val playerNames = playerLocations.map { (uuid, _) -> Bukkit.getOfflinePlayer(uuid).name ?: "unknown" }
		val columnWidth = (playerNames.map { stringWidth(it) }.max() ?: 0) + 2
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
					val (x, z, e) = unpackPosition(position)

					/* is this pixel for the current world */
					if (e == environmentMatch) {
						val (lx, lz, le) = unpackPosition(lastPosition)
						lastPosition = position

						val imgLastX = floor((lx + border) / zoom).toInt()
						val imgLastY = floor((lz + border) / zoom).toInt()
						val imgX = floor((x + border) / zoom).toInt()
						val imgY = floor((z + border) / zoom).toInt()

						val color = alongColor(red, gre, blu, (i * POSITION_CHUNK_SIZE + j) / trailSize.toFloat())

						val dx = abs(imgLastX - imgX)
						val dy = abs(imgLastY - imgY)

						/* need to draw line */
						if (e == le && (dy >= 2 || dx >= 2)) {
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

	fun generateContents(): String {
		var ret = ""

		var heldPosition = 1
		var position = 1

		var lastWinning = true
		var lastTime = -1

		playerList.asReversed().forEach { entry ->
			if (lastWinning) {
				heldPosition = if (!entry.winning) position else 1
				lastTime = -1
			} else {
				if (lastTime != entry.timeSurvived)
					heldPosition = position
			}

			ret += "$heldPosition ${entry.username} ${entry.timeSurvived} ${entry.killedBy}\n"

			lastWinning = entry.winning
			lastTime = entry.timeSurvived

			++position
		}

		return ret
	}

	fun selectFilename(matchNumber: Int = 0): File {
		val file = File("./summaries/${filename(startDate.year, startDate.monthValue, startDate.dayOfMonth, matchNumber)}")

		return if (file.exists()) selectFilename(matchNumber + 1) else file
	}

	fun selectImagename(environment: World.Environment, matchNumber: Int = 0): File {
		val file = File("./summaries/${imagename(startDate.year, startDate.monthValue, startDate.dayOfMonth, matchNumber, environment)}")

		return if (file.exists()) selectImagename(environment, matchNumber + 1) else file
	}

	fun createFile() {
		/* get or create summaries directory */
		val directory = File("./summaries")
		if (!directory.exists()) directory.mkdir()

		val writer = FileWriter(selectFilename())
		writer.write(generateContents())
		writer.close()

		val image0 = generateImage(UHC.startRadius, World.Environment.NORMAL, 4.0f)
		val image1 = generateImage(UHC.startRadius, World.Environment.NETHER, 4.0f)

		ImageIO.write(image0, "PNG", selectImagename(World.Environment.NORMAL))
		ImageIO.write(image1, "PNG", selectImagename(World.Environment.NETHER))
	}

	fun makeTest() {
		addEntry("test_user_8",  45, "environment")
		addEntry("test_user_0",  400, "environment")
		addEntry("test_user_5",  400, "environment")
		addEntry("test_user_11", 567, "environment")
		addEntry("test_user_1",  1945, "environment")
		addEntry("test_user_2",  1945, "environment")
		addEntry("test_user_7",  1945, "environment")
		addEntry("test_user_4",  2666, "environment")
		addEntry("test_user_9",  3011, "environment")
		addEntry("test_user_3",  4567, "environment")
		addEntry("test_user_6",  4567, "winning", true)
		addEntry("test_user_10", 4567, "winning", true)
	}

	companion object {
		const val BLACK = 0xff000000.toInt()

		const val CHAR_HEIGHT = 5

		data class Character(var pixels: BooleanArray, var width: Int)

		val characterList = Array(256) { Character(BooleanArray(0), 0) }

		private fun extractSection(x: Int, y: Int, width: Int, height: Int, image: BufferedImage): BooleanArray {
			val array = IntArray(width * height)

			image.getRGB(x, y, width, height, array, 0, width)

			return BooleanArray(array.size) { i ->
				array[i] == 0xffffffff.toInt()
			}
		}

		init {
			val characterImage = ImageIO.read(this::class.java.getResourceAsStream("/letters.png"))

			characterList['_'.toInt()] = Character(extractSection(0, 12, 3, CHAR_HEIGHT, characterImage), 3)

			for (i in '0'..'9') {
				characterList[i.toInt()] = Character(extractSection((i - '0') * 4, 6, 3, CHAR_HEIGHT, characterImage), 3)
			}

			val letterWidths = arrayOf(3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 5, 4, 3, 3, 4, 3, 3, 3, 3, 3, 5, 3, 3, 3)

			var advance = 0
			for (i in 'a'..'z') {
				val width = letterWidths[i - 'a']
				val character = Character(extractSection(advance, 0, width, CHAR_HEIGHT, characterImage), width)

				characterList[i.toInt()] = character
				characterList[i.toInt() - ('a' - 'A')] = character

				advance += width + 1
			}
		}

		fun filename(year: Int, month: Int, day: Int, number: Int): String {
			return "${year}_${month}_${day}_${number}.txt"
		}

		fun imagename(year: Int, month: Int, day: Int, number: Int, environment: World.Environment): String {
			return "${year}_${month}_${day}_${number} ${environment.name}.png"
		}

		data class InverseFilenameReturn (val year: Int, val month: Int, val day: Int, val number: Int)

		fun inverseFilename(filename: String): InverseFilenameReturn? {
			val lastSlash = filename.indexOfLast { it == '/' }

			val parts = filename.substring(lastSlash + 1).substringBefore('.').split('_')

			if (parts.size != 4) return null

			val year   = parts[0].toIntOrNull() ?: return null
			val month  = parts[1].toIntOrNull() ?: return null
			val day    = parts[2].toIntOrNull() ?: return null
			val number = parts[3].toIntOrNull() ?: return null

			return InverseFilenameReturn(year, month, day, number)
		}

		fun stringWidth(string: String): Int {
			return string.fold(string.length - 1) { acc, c -> acc + characterList[c.toInt()].width }
		}

		fun writeString(x: Int, y: Int, string: String, array: IntArray, width: Int, color: Int) {
			var advance = x

			string.forEach { c ->
				val character = characterList[c.toInt()]

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
	}
}

fun main() {
	val strings = arrayOf(
		"Hello",
		"_World__",
		"yo mama",
		"go_d",
		"May god have mercy on your soul",
		"not doing a god damn th1ng",
		"22 j Die in p0000453 deep weeb 35190",
		"a _lot_cooler_ than always"
	)

	val maxWidth = strings.map { Ledger.stringWidth(it) }.max()!!

	val width = maxWidth + 2
	val height = strings.size * (Ledger.CHAR_HEIGHT + 1) + 2

	val array = IntArray(width * height) { Ledger.BLACK }

	strings.forEachIndexed { i, string ->
		Ledger.writeString(1, height - (Ledger.CHAR_HEIGHT + 1) * (i + 1), string, array, width, 0xff17a9e8.toInt())
	}

	val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
	image.setRGB(0, 0, width, height, array, 0, width)
	ImageIO.write(image, "PNG", File("./testText.png"))
}
