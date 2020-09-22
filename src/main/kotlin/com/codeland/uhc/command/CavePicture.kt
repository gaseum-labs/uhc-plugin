package com.codeland.uhc.command

import co.aikar.commands.annotation.CommandAlias
import com.codeland.uhc.util.Util.log
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Chunk
import org.bukkit.command.CommandSender
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil

object CavePicture {
	val numChunks = ceil(1001 / 16.0).toInt()
	val offset = numChunks / 2
	val img = BufferedImage(numChunks, numChunks, BufferedImage.TYPE_INT_ARGB)

	fun addChunk(chunk: Chunk) {
		val x = chunk.x + offset
		val z = chunk.z + offset

		if (x >= 0 && z >= 0 && x < numChunks && z < numChunks) {
			var caveCount = 0

			for (x in 0..15) {
				for (z in 0..15) {
					for (y in 11..15) {
						if (chunk.getBlock(x, y, x).isPassable) ++caveCount
					}
				}
			}

			var caveValue = caveCount / (16 * 16 * 2f)
			if (caveValue > 1f) caveValue = 1f

			img.setRGB(x, z, (caveValue * 0xff).toInt().shl(16).or(0xff000000.toInt()))
			ImageIO.write(img, "png", File("diamondCaves.png"))
		}
	}
}