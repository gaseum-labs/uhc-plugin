package com.codeland.uhc.quirk.quirks.classes

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.math.abs

class Classes(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	override fun onEnable() {
		timerId = SchedulerUtil.everyTick(::everyTick)
	}

	override fun onDisable() {
		Bukkit.getScheduler().cancelTask(timerId)
	}

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid) { player ->
			val quirkClass = getClass(uuid)

			if (quirkClass != QuirkClass.NO_CLASS) startAsClass(player, quirkClass)
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

	private fun everyTick() {
		obsidianifiedLava.removeIf { ol ->
			ol.block.type != Material.OBSIDIAN ||

			(Bukkit.getOnlinePlayers().none { player ->
				val playerData = PlayerData.getPlayerData(player.uniqueId)

				playerData.participating
					&& getClass(playerData) == QuirkClass.LAVACASTER
					&& abs(player.location.x - ol.block.x) <= 4
					&& abs(player.location.y - ol.block.y) <= 4
					&& abs(player.location.z - ol.block.z) <= 3

			} && run { ol.block.setType(if (ol.flowing) Material.AIR else Material.LAVA, false); true })
		}
	}

	data class ObsidianifiedLava(val block: Block, val flowing: Boolean)

	companion object {
		var obsidianifiedLava: MutableList<ObsidianifiedLava> = mutableListOf()

		fun setClass(playerData: PlayerData, quirkClass: QuirkClass) {
			PlayerData.getQuirkDataHolder(playerData, QuirkType.CLASSES).data = quirkClass
		}

		fun setClass(uuid: UUID, quirkClass: QuirkClass) {
			setClass(PlayerData.getPlayerData(uuid), quirkClass)
		}

		fun startAsClass(player: Player, quirkClass: QuirkClass) {
			giveClassHead(player, quirkClass)
			quirkClass.onStart(player)
		}

		fun getClass(playerData: PlayerData): QuirkClass {
			return PlayerData.getQuirkData(playerData, QuirkType.CLASSES)
		}

		fun getClass(uuid: UUID): QuirkClass {
			return getClass(PlayerData.getPlayerData(uuid))
		}

		fun giveClassHead(player: Player, quirkClass: QuirkClass) {
			val headItem = ItemStack(quirkClass.headBlock)

			val meta = headItem.itemMeta
			meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
			headItem.itemMeta = meta

			player.inventory.helmet = headItem
		}

		fun removeHead(player: Player) {
			player.inventory.helmet = null
		}

		private var timerId: Int = 0
	}
}
