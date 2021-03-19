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

	override fun onDisable() {}

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid) { player ->
			val playerData = PlayerData.getPlayerData(uuid)

			// give them a random class if they didn't pick one

			if (getClass(player) == QuirkClass.NO_CLASS) {
				setClass(player, QuirkClass.values().takeLast(QuirkClass.values().size - 1).random())
			} else if (getClass(player) == QuirkClass.LAVACASTER) {
				player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, /* no particles */ false, true))
			} else if (getClass(player) == QuirkClass.DIVER) {
				player.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 1, false, /* no particles */ false, true))
			}

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

	private fun everyTick() {
		outer@ for (o in obsidianifiedLava) {
			for (player in Bukkit.getOnlinePlayers().filter {PlayerData.getPlayerData(it.uniqueId).alive}) {
				if (getClass(player) == QuirkClass.LAVACASTER
						&& abs(player.location.x - o.block.x) <= 4
						&& abs(player.location.y - o.block.y) <= 4
						&& abs(player.location.z - o.block.z) <= 3) continue@outer
			}
			// there aren't any lavacasters nearby
			o.block.type = if (o.flowing) Material.AIR else Material.LAVA
		}
	}

	data class ObsidianifiedLava(val block: Block, val flowing: Boolean)

	companion object {
		var obsidianifiedLava: MutableList<ObsidianifiedLava> = mutableListOf()

		fun setClass(uuid: UUID, quirkClass: QuirkClass) {
			PlayerData.getQuirkDataHolder(PlayerData.getPlayerData(uuid), QuirkType.CLASSES).data = quirkClass
		}

		fun setClass(player: Player, quirkClass: QuirkClass) {
			setClass(player.uniqueId, quirkClass)
		}

		fun getClass(uuid: UUID): QuirkClass {
			return PlayerData.getQuirkDataHolder(PlayerData.getPlayerData(uuid), QuirkType.CLASSES).data as QuirkClass
		}

		fun getClass(player: Player): QuirkClass {
			return getClass(player.uniqueId)
		}

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

		private var timerId: Int = 0
	}
}
