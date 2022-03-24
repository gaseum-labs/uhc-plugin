package org.gaseumlabs.uhc.gui.gui

import org.gaseumlabs.uhc.core.GameConfig
import org.gaseumlabs.uhc.gui.*
import org.gaseumlabs.uhc.gui.guiItem.GuiItem
import org.gaseumlabs.uhc.gui.guiItem.impl.WorldGenSetting
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.WorldGenOption
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
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
				ItemCreator.fromType(Material.MUSIC_DISC_CHIRP).name(Component.text("Reset", NamedTextColor.RED))
					.create()
		})
		addItem(object : GuiItem(coords(8, 2)) {
			override fun onClick(player: Player, shift: Boolean) = createGameGui.open(player)
			override fun getStack() =
				ItemCreator.fromType(Material.PRISMARINE_SHARD).name(Component.text("Back", NamedTextColor.BLUE))
					.create()
		})
	}
}
