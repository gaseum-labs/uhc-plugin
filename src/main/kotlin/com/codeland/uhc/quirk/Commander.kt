package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import org.bukkit.ChatColor
import org.bukkit.entity.Entity
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
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

	fun onSummon(event: PlayerInteractEvent): Boolean {
		if (event.action != Action.RIGHT_CLICK_BLOCK)
			return false

		val item = event.item
			?: return false

		val block = event.clickedBlock
			?: return false

		val type = Summoner.getSpawnEntity(item.type, Quirk.AGGRO_SUMMONER.enabled, Quirk.PASSIVE_SUMMONER.enabled)
			?: return false

		val location = block.location.add(event.blockFace.direction).add(0.5, 0.5, 0.5)
		val entity = event.player.world.spawnEntity(location, type, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)

		val team = GameRunner.playersTeam(event.player.name)
		if (team != null) {
			setCommandedBy(entity, team.color)

			if (Quirk.COMMANDER.enabled)
				entity.customName = "${team.color}${team.displayName}${net.md_5.bungee.api.ChatColor.RESET} ${entity.name}"
		}

		--item.amount

		return true
	}
}
