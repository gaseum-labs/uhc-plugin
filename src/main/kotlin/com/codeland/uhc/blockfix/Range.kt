package com.codeland.uhc.blockfix

import com.codeland.uhc.UHCPlugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import kotlin.random.Random
import kotlin.random.nextInt

sealed class Range(val prettyName: String)

class SingleRange(
	prettyName: String,
	val onDrop: (Material, MutableList<ItemStack>, Int) -> ItemStack?,
) : Range(prettyName)

class CountingRange(
	prettyName: String,
	val size: Int,
	val onDrop: (Material, MutableList<ItemStack>) -> ItemStack?,
	val offDrop: (Material, MutableList<ItemStack>) -> ItemStack?,
) : Range(prettyName) {
	private val metaName: String = "${prettyName}_BF"

	class Metadata(var count: Int, var index: Int)

	fun getMeta(player: Player): Metadata {
		val meta = player.getMetadata(metaName)

		return if (meta.isEmpty()) {
			val metadata = Metadata(0, Random.nextInt(1..size))
			player.setMetadata(metaName, FixedMetadataValue(UHCPlugin.plugin, metadata))
			metadata

		} else {
			meta.first().value() as Metadata
		}
	}
}
