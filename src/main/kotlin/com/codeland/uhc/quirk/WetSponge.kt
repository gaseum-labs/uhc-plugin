package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phaseType.PhaseVariant
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class WetSponge(type: QuirkType) : Quirk(type) {
	override fun onEnable() {}

	override fun onDisable() {}

	override fun onPhaseSwitch(phase: PhaseVariant) {
		if (phase.type == PhaseType.GRACE) {
			Bukkit.getServer().onlinePlayers.forEach { player ->
				player.inventory.addItem(ItemStack(Material.WET_SPONGE))
			}
		}
	}

	companion object {
		fun addSponge(player: Player) {
			if (GameRunner.uhc.isGameGoing())
				player.inventory.addItem(ItemStack(Material.WET_SPONGE))
		}
	}
}