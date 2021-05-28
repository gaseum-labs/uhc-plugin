package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItemProperty
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DefaultEnvironmentCycler(index: Int) : GuiItemProperty<World.Environment>(index, UHC.defaultWorldEnvironment) {
	override fun onClick(player: Player, shift: Boolean) {
		UHC.defaultWorldEnvironment.set(
			if (UHC.defaultWorldEnvironment.get() === World.Environment.NORMAL)
				World.Environment.NETHER
			else
				World.Environment.NORMAL
		)
	}

	override fun getStackProperty(value: World.Environment): ItemStack {
		val data = if (value === World.Environment.NORMAL)
			Pair(Material.GRASS_BLOCK, "Normal")
		else
			Pair(Material.NETHERRACK, "Nether")

		return lore(name(ItemStack(data.first), stateName("World", data.second)), listOf(Component.text("Which dimension the UHC starts in")))
	}
}
