package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.KillReward
import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.Preset.Companion.NO_PRESET_REPRESENTATION
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.phase.PhaseType
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class KillRewardCycler(uhc: UHC, index: Int) : GuiItem(uhc, index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		uhc.killReward = KillReward.values()[(uhc.killReward.ordinal + 1) % KillReward.values().size]
	}

	override fun getStack(): ItemStack {
		val killReward = uhc.killReward
		val stack = ItemStack(killReward.representation)
		setLore(stack, killReward.lore)

		setName(stack, "${ChatColor.WHITE}Preset ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${killReward.prettyName}")

		return stack
	}
}
