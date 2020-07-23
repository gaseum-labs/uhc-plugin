package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.PhaseType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class WetSponge(type: QuirkType) : Quirk(type) {
	override fun onEnable() {}

	override fun onDisable() {}

	companion object {
		fun addSponge(player: Player) {
			if (!GameRunner.uhc.isPhase(PhaseType.WAITING) && !GameRunner.uhc.isPhase(PhaseType.POSTGAME)) {
				player.inventory.addItem(ItemStack(Material.WET_SPONGE))
			}
		}
	}
}