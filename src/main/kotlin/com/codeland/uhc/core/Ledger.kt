package com.codeland.uhc.core

import com.codeland.uhc.util.Util
import org.bukkit.World
import org.bukkit.block.Block
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.floor
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

		val lastPosition = list.last.lastOrNull()

		if (lastPosition != null) {
			/* TODO raster line algorithm */
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

	fun generateImage(border: Int, environment: World.Environment, zoom: Float): BufferedImage {
		val BLACK = 0xff000000.toInt()

		val imageSize = ((border * 2 + 1) / zoom).roundToInt()
		val pixels = IntArray(imageSize * imageSize) { BLACK }
		val environmentMatch = environment !== World.Environment.NORMAL

		var playerIndex = 0

		/* render trail */
		playerLocations.forEach { (uuid, trail) ->
			val (red, gre, blu) = selectColor(playerIndex++, playerLocations.size)
			val trailSize = (trail.size - 1) * POSITION_CHUNK_SIZE + trail.last.size

			trail.forEachIndexed { i, chunk ->
				chunk.forEachIndexed { j, position ->
					val trailIndex = i * POSITION_CHUNK_SIZE + j

					val (x, z, e) = unpackPosition(position)

					val imageX = floor((x + border) / zoom).toInt()
					val imageY = floor((z + border) / zoom).toInt()
					val imageI = imageY * imageSize + imageX

					if (
						e == environmentMatch &&
						imageX >= 0 &&
						imageY >= 0 &&
						imageX < imageSize &&
						imageY < imageSize &&
						pixels[imageI] == BLACK
					) {
						val tRed = (red * Util.interp(0.1f, 1.0f, trailIndex / trailSize.toFloat())).toInt()
						val tGre = (gre * Util.interp(0.1f, 1.0f, trailIndex / trailSize.toFloat())).toInt()
						val tBlu = (blu * Util.interp(0.1f, 1.0f, trailIndex / trailSize.toFloat())).toInt()

						pixels[imageI] = tRed.shl(16).or(tGre.shl(8)).or(tBlu).or(BLACK)
					}
				}
			}
		}

		val image = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB)
		image.setRGB(0, 0, imageSize, imageSize, pixels, 0, imageSize)

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
		fun filename(year: Int, month: Int, day: Int, number: Int): String {
			return "${year}_${month}_${day}~${number}.txt"
		}

		fun imagename(year: Int, month: Int, day: Int, number: Int, environment: World.Environment): String {
			return "${year}_${month}_${day}~${number} ${environment.name}.png"
		}

		data class InverseFilenameReturn (val year: Int, val month: Int, val day: Int, val number: Int)

		fun inverseFilename(filename: String): InverseFilenameReturn? {
			val lastSlash = filename.indexOfLast { it == '/' }

			val parts = filename.substring(lastSlash + 1).substringBefore('.').split('_', '~')

			if (parts.size != 4) return null

			val year   = parts[0].toIntOrNull() ?: return null
			val month  = parts[1].toIntOrNull() ?: return null
			val day    = parts[2].toIntOrNull() ?: return null
			val number = parts[3].toIntOrNull() ?: return null

			return InverseFilenameReturn(year, month, day, number)
		}
	}
}
