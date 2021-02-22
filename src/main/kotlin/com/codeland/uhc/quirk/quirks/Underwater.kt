package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.dropFix.DropEntry.Companion.loot
import com.codeland.uhc.dropFix.DropEntry.Companion.lootEntity
import com.codeland.uhc.dropFix.DropEntry.Companion.lootItem
import com.codeland.uhc.dropFix.DropEntry.Companion.onFire
import com.codeland.uhc.dropFix.DropFix
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class Underwater(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	override fun onEnable() {
		if (GameRunner.uhc.isGameGoing()) {
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				if (playerData.participating) onStart(uuid)
			}
		}
	}

	override fun onDisable() {
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			GameRunner.playerAction(uuid, ::removeEffects)
		}
	}

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid, ::giveEffects)
	}

	override fun customDrops(): Array<DropFix>? {
		return arrayOf(
			DropFix(EntityType.DOLPHIN, arrayOf(
				arrayOf(lootEntity(onFire(Material.COD, Material.COOKED_COD), ::lootItem), loot(Material.LEATHER, ::lootItem))
			), arrayOf(
				lootEntity(onFire(Material.COD, Material.COOKED_COD), ::lootItem)
			))
		)
	}

	override fun customSpawnInfos(): Array<SpawnInfo>? {
		return arrayOf(
			object : SpawnInfo() {
				override fun allowSpawn(block: Block, spawnCycle: Int): EntityType? {
					return if (block.type == Material.WATER) EntityType.DOLPHIN else null
				}
				override fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {
					(entity as LivingEntity).removeWhenFarAway = true
				}
			},
			object : SpawnInfo() {
				override fun allowSpawn(block: Block, spawnCycle: Int): EntityType? {
					return if (block.type == Material.WATER) EntityType.COD else null
				}
				override fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {
					(entity as LivingEntity).removeWhenFarAway = true
				}
			}
		)
	}

	companion object {
		fun giveEffects(player: Player) {
			SchedulerUtil.nextTick {
				player.addPotionEffect(PotionEffect(PotionEffectType.CONDUIT_POWER, Int.MAX_VALUE / 2, 10, false, false, true))
			}
		}

		fun removeEffects(player: Player) {
			player.removePotionEffect(PotionEffectType.CONDUIT_POWER)
		}
	}
}