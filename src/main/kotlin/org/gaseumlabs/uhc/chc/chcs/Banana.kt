package org.gaseumlabs.uhc.chc.chcs

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Axis
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.Orientable
import org.bukkit.entity.Cow
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.chc.CHC
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.phases.Grace
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import kotlin.random.Random

fun findGroundAt(x: Int, z: Int, limitedRegion: LimitedRegion): Int? {
	for (y in 128 downTo 58) {
		val block = limitedRegion.getBlockData(x, y, z)
		if (block.material.isSolid && !RegenUtil.treeParts.contains(block.material)) {
			return y + 1
		}
	}
	return null
}

fun spawnMonkey(location: Location) {
	val monkey = location.world.spawn(location, Cow::class.java)

	monkey.isCustomNameVisible = true
	monkey.customName(Component.text("UHC Monkey", TextColor.color(0xffff00), TextDecoration.BOLD))
	monkey.activePotionEffects.add(PotionEffect(PotionEffectType.JUMP, 100000, 3))
	monkey.activePotionEffects.add(PotionEffect(PotionEffectType.SPEED, 100000, 3))
	monkey.setMetadata(Banana.KEY_MOB, FixedMetadataValue(UHCPlugin.plugin, true))
	monkey.removeWhenFarAway = true
}

val trunkBlockData = Material.STRIPPED_JUNGLE_LOG.createBlockData().let { data ->
	(data as Orientable).axis = Axis.Y
	data
}
val leavesData = Material.JUNGLE_LEAVES.createBlockData()
val spongeBlocKData = Material.SPONGE.createBlockData()

class BananaTree : BlockPopulator() {
	override fun populate(
		worldInfo: WorldInfo,
		random: java.util.Random,
		chunkX: Int,
		chunkZ: Int,
		limitedRegion: LimitedRegion
	) {
		if (random.nextInt(10) != 0) return

		val rx = limitedRegion.centerBlockX
		val rz = limitedRegion.centerBlockZ

		val cx = rx + random.nextInt(-8, 9)
		val cz = rz + random.nextInt(-8, 9)

		val startY = findGroundAt(cx, cz, limitedRegion) ?: return
		if (limitedRegion.getBlockData(cx, startY, cz).material.isSolid) return

		val height = random.nextInt(10, 20)

		for (i in 0 until height) {
			limitedRegion.setBlockData(cx, startY + i, cz, trunkBlockData)
		}
		for (i in -5 until 5) {
			if (i == 0) {
				limitedRegion.setBlockData(cx, startY + height, cz, leavesData)
			} else {
				limitedRegion.setBlockData(cx + i, startY + height, cz, leavesData)
				if (random.nextInt(10) == 0)
					limitedRegion.setBlockData(cx + i, startY + height - 1, cz, spongeBlocKData)
				limitedRegion.setBlockData(cx, startY + height, cz + i, leavesData)
				if (random.nextInt(10) == 0)
					limitedRegion.setBlockData(cx, startY + height - 1, cz + i, spongeBlocKData)
			}
		}
	}
}

class Banana : CHC<Nothing?>() {
	private var tickingTask = -1

	override fun defaultData() = null

	override fun customDestroy(game: Game) {
		Bukkit.getScheduler().cancelTask(tickingTask)
	}

	override fun onPhaseSwitch(game: Game, phase: Phase) {
		if (phase is Grace) {
			tickingTask = SchedulerUtil.everyN(1001, ::tick)
		}
	}

	private fun tick() {
		UHC.game ?: return

		val players = Bukkit.getOnlinePlayers().filter { PlayerData.get(it).participating }
		val player = players.random()

		val superBananaIndex = player.inventory.contents.indexOfFirst {
			stack -> stack?.type === Material.GOLDEN_SWORD && stack.itemMeta.hasDisplayName()
		}
		if (superBananaIndex != -1) {
			player.inventory.setItem(superBananaIndex,  ItemStack(Material.TNT, 16))
			return
		}

		val bananaIndex = player.inventory.contents.indexOfFirst {
			stack -> stack?.type === Material.GOLDEN_PICKAXE && stack.itemMeta.hasDisplayName()
		}
		if (bananaIndex == -1) return

		player.inventory.setItem(bananaIndex, ItemStack(Material.DIAMOND, 2))
	}

	override fun eventListener() = object : Listener {
		@EventHandler
		fun onWorldLoad(event: WorldInitEvent) {
			event.world.populators.add(BananaTree())
		}

		@EventHandler
		fun onStartBreakBlock(event: BlockDamageEvent) {
			if (event.block.type !== Material.SPONGE) return
			event.instaBreak = true
			event.block.world.dropItemNaturally(
				event.block.location,
				if (Random.nextInt(10) == 0) createSuperBanana() else createBanana()
			)
		}

		@EventHandler
		fun entityDeath(event: EntityDeathEvent) {
			if (isMonkey(event.entity)) {
				event.drops.clear()
				event.drops.add(if (Random.nextInt(10) == 0) createSuperBanana() else createBanana())
			}
		}

		@EventHandler
		fun spawnEntity(event: EntitySpawnEvent) {
			if (event.entity !is Player && event.entity is LivingEntity && Random.nextInt(20) == 0) {
				spawnMonkey(event.entity.location)
				event.isCancelled = true
			}
		}
	}

	fun createBanana(): ItemStack {
		return ItemCreator.display(Material.GOLDEN_PICKAXE)
			.name(Component.text("UHC Banana", TextColor.color(0xffff00), TextDecoration.BOLD))
			.lore(listOf(Component.text("BANANA"), Component.text("BANANA"), Component.text("BANANA"), Component.text("BANANA")))
			.create()
	}

	fun createSuperBanana(): ItemStack {
		return ItemCreator.display(Material.GOLDEN_SWORD)
			.name(Component.text("Super UHC Banana", TextColor.color(0xff8000), TextDecoration.BOLD))
			.lore(listOf(Component.text("OOH OOH AH AH EEEEEEEEEEEEEEEEEEEEEEEEEKKKKK")))
			.create()
	}

	companion object {
		const val KEY_MOB = "_uhc_b-mob"

		fun isMonkey(entity: Entity) = entity.getMetadata(KEY_MOB).isNotEmpty()
	}
}