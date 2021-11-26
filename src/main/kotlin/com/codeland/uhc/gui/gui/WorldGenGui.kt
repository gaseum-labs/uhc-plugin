package com.codeland.uhc.gui.gui

import com.codeland.uhc.core.GameConfig
import com.codeland.uhc.gui.*
import com.codeland.uhc.gui.guiItem.GuiItem
import com.codeland.uhc.gui.guiItem.impl.WorldGenSetting
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldGenOption
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

class WorldGenGui(val gameConfig: GameConfig, val createGameGui: CreateGameGui) : GuiPage(
	3,
	Util.gradientString("World Gen Settings", TextColor.color(0x088756), TextColor.color(0x5a9615)),
	GuiType.WORLD_GEN
) {
	init {
		WorldGenOption.values().forEachIndexed { i, worldGenOption ->
			addItem(WorldGenSetting(i, worldGenOption, gameConfig.worldGenEnabled[i]))
		}

		addItem(object : GuiItem(coords(7, 2)) {
			override fun onClick(player: Player, shift: Boolean) = gameConfig.worldGenEnabled.forEach { it.set(false) }
			override fun getStack() =
				ItemCreator.fromType(Material.MUSIC_DISC_CHIRP).name("${ChatColor.RED}Reset").create()
		})
		addItem(object : GuiItem(coords(8, 2)) {
			override fun onClick(player: Player, shift: Boolean) = createGameGui.open(player)
			override fun getStack() =
				ItemCreator.fromType(Material.PRISMARINE_SHARD).name("${ChatColor.BLUE}Back").create()
		})
	}
}
