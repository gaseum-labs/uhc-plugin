package com.codeland.uhc.quirk.quirks.classes

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class Classes(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	override fun onEnable() {}

	override fun onDisable() {}

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid) { player ->
			val playerData = PlayerData.getPlayerData(uuid)

			giveClassHead(player, playerData)
		}
	}

	override fun onEnd(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> removeHead(player) }
		val playerData = PlayerData.getPlayerData(uuid)

		PlayerData.getQuirkDataHolder(playerData, QuirkType.CLASSES).data = QuirkClass.NO_CLASS
	}

	override val representation: ItemStack
		get() = ItemStack(Material.LEATHER_HELMET)

	override fun defaultData(): Any {
		return QuirkClass.NO_CLASS
	}

	companion object {
		fun giveClassHead(player: Player, playerData: PlayerData) {
			val quirkClass = PlayerData.getQuirkData<QuirkClass>(playerData, QuirkType.CLASSES)

			if (quirkClass != QuirkClass.NO_CLASS) {
				val headItem = ItemStack(quirkClass.headBlock)

				val meta = headItem.itemMeta
				meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
				headItem.itemMeta = meta

				player.inventory.helmet = headItem
			}
		}

		fun removeHead(player: Player) {
			player.inventory.helmet = null
		}
	}
}
