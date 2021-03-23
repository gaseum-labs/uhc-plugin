package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Horse
import org.bukkit.inventory.ItemStack
import java.util.*

class HorseQuirk(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	companion object {
		val horseMap: MutableMap<Horse, UUID> = mutableMapOf()
	}

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid) { player ->
			val horse = player.world.spawnEntity(player.location, EntityType.HORSE) as Horse
			horse.inventory.saddle = ItemStack(Material.SADDLE)
			horse.isTamed = true
			horse.owner = player
			horse.setPassenger(player)
			horseMap[horse] = uuid
		}
	}

	override fun onEnable() {

	}

	override fun onDisable() {

	}

	override val representation: ItemStack
		get() = ItemStack(Material.SADDLE)


}