package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import org.bukkit.ChatColor
import org.bukkit.entity.Entity
import org.bukkit.metadata.FixedMetadataValue

object Commander {
	const val META_TAG = "commandedBy"

	fun setCommandedBy(entity: Entity, color: ChatColor) {
		entity.setMetadata(META_TAG, FixedMetadataValue(GameRunner.plugin, color))
	}

	fun isCommandedBy(entity: Entity, color: ChatColor): Boolean {
		val meta = entity.getMetadata(META_TAG)

		return (meta.size > 0 && (meta[0].value() as ChatColor) == color)
	}

	fun isCommanded(entity: Entity): Boolean {
		val meta = entity.getMetadata(META_TAG)

		return meta.size > 0
	}
}
