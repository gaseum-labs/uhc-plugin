package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.*
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

class Summoner(type: QuirkType, game: Game) : Quirk(type, game) {
	override fun customDestroy() {
		/* remove commanded tag from all commanded mobs */
		Bukkit.getWorlds().forEach { world ->
			world.entities.forEach { entity ->
				if (isCommanded(entity)) setCommandedByNone(entity)
			}
		}
	}

	override fun modifyEntityDrops(entity: Entity, killer: Player?, drops: MutableList<ItemStack>): Boolean {
		val spawnEgg = getSpawnEgg(entity.type)
		if (spawnEgg != null) drops.add(ItemStack(spawnEgg))

		return false
	}

	fun getSpawnEgg(entity: EntityType): Material? {
		return getSpawnEgg(entity, true, true)
	}

	fun onSummon(event: PlayerInteractEvent): Boolean {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return false

		val item = event.item ?: return false

		val block = event.clickedBlock ?: return false

		val type = getSpawnEntity(item.type, true, true) ?: return false

		val location = block.location.add(event.blockFace.direction).toCenterLocation()
		val entity = event.player.world.spawnEntity(location, type, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)

		val team = game.teams.playersTeam(event.player.uniqueId)
		if (team != null) {
			setCommandedBy(entity, team)

			entity.customName(team.apply("${team.name} ${entity.name}"))
		}

		--item.amount

		return true
	}

	companion object {
		class Summon(var type: EntityType, var egg: Material)

		private val aggroSummons = arrayOf(
			Summon(ELDER_GUARDIAN, ELDER_GUARDIAN_SPAWN_EGG),
			Summon(WITHER_SKELETON, WITHER_SKELETON_SPAWN_EGG),
			Summon(STRAY, STRAY_SPAWN_EGG),
			Summon(HUSK, HUSK_SPAWN_EGG),
			Summon(ZOMBIE_VILLAGER, ZOMBIE_VILLAGER_SPAWN_EGG),
			Summon(SKELETON_HORSE, SKELETON_HORSE_SPAWN_EGG),
			Summon(ZOMBIE_HORSE, ZOMBIE_HORSE_SPAWN_EGG),
			Summon(DONKEY, DONKEY_SPAWN_EGG),
			Summon(EVOKER, EVOKER_SPAWN_EGG),
			Summon(VINDICATOR, VINDICATOR_SPAWN_EGG),
			Summon(CREEPER, CREEPER_SPAWN_EGG),
			Summon(SKELETON, SKELETON_SPAWN_EGG),
			Summon(SPIDER, SPIDER_SPAWN_EGG),
			Summon(ZOMBIE, ZOMBIE_SPAWN_EGG),
			Summon(SLIME, SLIME_SPAWN_EGG),
			Summon(GHAST, GHAST_SPAWN_EGG),
			Summon(ZOMBIFIED_PIGLIN, ZOMBIFIED_PIGLIN_SPAWN_EGG),
			Summon(ENDERMAN, ENDERMAN_SPAWN_EGG),
			Summon(CAVE_SPIDER, CAVE_SPIDER_SPAWN_EGG),
			Summon(SILVERFISH, SILVERFISH_SPAWN_EGG),
			Summon(BLAZE, BLAZE_SPAWN_EGG),
			Summon(MAGMA_CUBE, MAGMA_CUBE_SPAWN_EGG),
			Summon(WITCH, WITCH_SPAWN_EGG),
			Summon(ENDERMITE, ENDERMITE_SPAWN_EGG),
			Summon(GUARDIAN, GUARDIAN_SPAWN_EGG),
			Summon(SHULKER, SHULKER_SPAWN_EGG),
			Summon(WOLF, WOLF_SPAWN_EGG),
			Summon(POLAR_BEAR, POLAR_BEAR_SPAWN_EGG),
			Summon(LLAMA, LLAMA_SPAWN_EGG),
			Summon(PHANTOM, PHANTOM_SPAWN_EGG),
			Summon(DROWNED, DROWNED_SPAWN_EGG),
			Summon(DOLPHIN, DOLPHIN_SPAWN_EGG),
			Summon(PILLAGER, PILLAGER_SPAWN_EGG),
			Summon(RAVAGER, RAVAGER_SPAWN_EGG),
			Summon(HOGLIN, HOGLIN_SPAWN_EGG),
			Summon(PIGLIN, PIGLIN_SPAWN_EGG),
			Summon(ZOGLIN, ZOGLIN_SPAWN_EGG),
			Summon(VEX, VEX_SPAWN_EGG)
		)

		private val inverseAggroSummons = aggroSummons.copyOf()

		private val passiveSummons = arrayOf(
			Summon(MULE, MULE_SPAWN_EGG),
			Summon(BAT, BAT_SPAWN_EGG),
			Summon(PIG, PIG_SPAWN_EGG),
			Summon(SHEEP, SHEEP_SPAWN_EGG),
			Summon(COW, COW_SPAWN_EGG),
			Summon(EntityType.CHICKEN, CHICKEN_SPAWN_EGG),
			Summon(SQUID, SQUID_SPAWN_EGG),
			Summon(MUSHROOM_COW, MOOSHROOM_SPAWN_EGG),
			Summon(OCELOT, OCELOT_SPAWN_EGG),
			Summon(HORSE, HORSE_SPAWN_EGG),
			Summon(EntityType.RABBIT, RABBIT_SPAWN_EGG),
			Summon(PARROT, PARROT_SPAWN_EGG),
			Summon(VILLAGER, VILLAGER_SPAWN_EGG),
			Summon(TURTLE, TURTLE_SPAWN_EGG),
			Summon(EntityType.COD, COD_SPAWN_EGG),
			Summon(EntityType.SALMON, SALMON_SPAWN_EGG),
			Summon(EntityType.PUFFERFISH, PUFFERFISH_SPAWN_EGG),
			Summon(EntityType.TROPICAL_FISH, TROPICAL_FISH_SPAWN_EGG),
			Summon(CAT, CAT_SPAWN_EGG),
			Summon(PANDA, PANDA_SPAWN_EGG),
			Summon(TRADER_LLAMA, TRADER_LLAMA_SPAWN_EGG),
			Summon(WANDERING_TRADER, WANDERING_TRADER_SPAWN_EGG),
			Summon(FOX, FOX_SPAWN_EGG),
			Summon(BEE, BEE_SPAWN_EGG),
			Summon(STRIDER, STRIDER_SPAWN_EGG)
		)

		private val inversePassiveSummons = passiveSummons.copyOf()

		init {
			aggroSummons.sortBy { summon -> summon.type }
			passiveSummons.sortBy { summon -> summon.type }
			inverseAggroSummons.sortBy { summon -> summon.egg }
			inversePassiveSummons.sortBy { summon -> summon.egg }
		}

		fun getSpawnEgg(entity: EntityType, allowAggro: Boolean, allowPassive: Boolean): Material? {
			var ret = null as Summon?

			if (allowAggro)
				ret = Util.binaryFind(entity, aggroSummons) { summon -> summon.type }

			if (ret == null && allowPassive)
				ret = Util.binaryFind(entity, passiveSummons) { summon -> summon.type }

			return ret?.egg
		}

		fun getSpawnEntity(egg: Material, allowAggro: Boolean, allowPassive: Boolean): EntityType? {
			var ret = null as Summon?

			if (allowAggro)
				ret = Util.binaryFind(egg, inverseAggroSummons) { summon -> summon.egg }

			if (ret == null && allowPassive)
				ret = Util.binaryFind(egg, inversePassiveSummons) { summon -> summon.egg }

			return ret?.type
		}

		/* COMMADNER */

		const val META_TAG = "commandedBy"

		fun setCommandedBy(entity: Entity, team: Team) {
			entity.setMetadata(META_TAG, FixedMetadataValue(org.gaseumlabs.uhc.UHCPlugin.plugin, team))
		}

		fun setCommandedByNone(entity: Entity) {
			entity.removeMetadata(META_TAG, org.gaseumlabs.uhc.UHCPlugin.plugin)

			entity.customName = null
		}

		fun isCommandedBy(entity: Entity, team: Team): Boolean {
			val meta = entity.getMetadata(META_TAG)

			return (meta.size > 0 && (meta[0].value() as Team) === team)
		}

		fun isCommanded(entity: Entity): Boolean {
			val meta = entity.getMetadata(META_TAG)

			return meta.size > 0
		}
	}
}