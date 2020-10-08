package com.codeland.uhc.command.ubt

import com.codeland.uhc.UHCPlugin
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

class PartialUBT {
	var corner0Set = false
	var corner1Set = false

	var corner0X = 0
	var corner0Y = 0
	var corner0Z = 0

	var corner1X = 0
	var corner1Y = 0
	var corner1Z = 0

	fun setCorner0(x: Int, y: Int, z: Int) {
		corner0X = x
		corner0Y = y
		corner0Z = z

		sortCorners()

		corner0Set = true
	}

	fun setCorner1(x: Int, y: Int, z: Int) {
		corner1X = x
		corner1Y = y
		corner1Z = z

		sortCorners()

		corner1Set = true
	}

	fun sortCorners() {
		if (corner1X < corner0X) {
			val temp = corner0X
			corner0X = corner1X
			corner1X = temp
		}

		if (corner1Y < corner0Y) {
			val temp = corner0Y
			corner0Y = corner1Y
			corner1Y = temp
		}

		if (corner1Z < corner0Z) {
			val temp = corner0Z
			corner0Z = corner1Z
			corner1Z = temp
		}
	}

	fun isValid(): Boolean {
		return corner0Set && corner1Set
	}

	fun width(): Int {
		return corner1X - corner0X + 1
	}

	fun height(): Int {
		return corner1Y - corner0Y + 1
	}

	fun depth(): Int {
		return corner1Z - corner0Z + 1
	}

	companion object {
		val META_TAG = "_UHC_PartialUBT"

		fun getPlayersPartialUBT(player: Player): PartialUBT {
			val meta = player.getMetadata(META_TAG)

			return if (meta.size == 0) {
				val partialUBT = PartialUBT()
				player.setMetadata(META_TAG, FixedMetadataValue(UHCPlugin.plugin, partialUBT))

				partialUBT

			} else {
				meta[0].value() as PartialUBT
			}
		}
	}
}
