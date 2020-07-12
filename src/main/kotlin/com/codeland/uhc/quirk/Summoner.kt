package com.codeland.uhc.quirk

import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.*

object Summoner {

	class Summon(var type: EntityType, var egg: Material)

	private val summons = arrayOf(
		Summon(ELDER_GUARDIAN, ELDER_GUARDIAN_SPAWN_EGG),
		Summon(WITHER_SKELETON, WITHER_SKELETON_SPAWN_EGG),
		Summon(STRAY, STRAY_SPAWN_EGG),
		Summon(HUSK, HUSK_SPAWN_EGG),
		Summon(ZOMBIE_VILLAGER, ZOMBIE_VILLAGER_SPAWN_EGG),
		Summon(SKELETON_HORSE, SKELETON_HORSE_SPAWN_EGG),
		Summon(ZOMBIE_HORSE, ZOMBIE_HORSE_SPAWN_EGG),
		Summon(DONKEY, DONKEY_SPAWN_EGG),
		//Summon(MULE, MULE_SPAWN_EGG),
		Summon(EVOKER, EVOKER_SPAWN_EGG),
		//Summon(VEX, VEX_SPAWN_EGG),
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
		//Summon(BAT, BAT_SPAWN_EGG),
		Summon(WITCH, WITCH_SPAWN_EGG),
		Summon(ENDERMITE, ENDERMITE_SPAWN_EGG),
		Summon(GUARDIAN, GUARDIAN_SPAWN_EGG),
		Summon(SHULKER, SHULKER_SPAWN_EGG),
		//Summon(PIG, PIG_SPAWN_EGG),
		//Summon(SHEEP, SHEEP_SPAWN_EGG),
		//Summon(COW, COW_SPAWN_EGG),
		//Summon(EntityType.CHICKEN, CHICKEN_SPAWN_EGG),
		//Summon(SQUID, SQUID_SPAWN_EGG),
		Summon(WOLF, WOLF_SPAWN_EGG),
		//Summon(MUSHROOM_COW, MOOSHROOM_SPAWN_EGG),
		//Summon(OCELOT, OCELOT_SPAWN_EGG),
		//Summon(HORSE, HORSE_SPAWN_EGG),
		//Summon(EntityType.RABBIT, RABBIT_SPAWN_EGG),
		Summon(POLAR_BEAR, POLAR_BEAR_SPAWN_EGG),
		Summon(LLAMA, LLAMA_SPAWN_EGG),
		//Summon(PARROT, PARROT_SPAWN_EGG),
		//Summon(VILLAGER, VILLAGER_SPAWN_EGG),
		//Summon(TURTLE, TURTLE_SPAWN_EGG),
		Summon(PHANTOM, PHANTOM_SPAWN_EGG),
		//Summon(EntityType.COD, COD_SPAWN_EGG),
		//Summon(EntityType.SALMON, SALMON_SPAWN_EGG),
		//Summon(EntityType.PUFFERFISH, PUFFERFISH_SPAWN_EGG),
		//Summon(EntityType.TROPICAL_FISH, TROPICAL_FISH_SPAWN_EGG),
		Summon(DROWNED, DROWNED_SPAWN_EGG),
		Summon(DOLPHIN, DOLPHIN_SPAWN_EGG),
		//Summon(CAT, CAT_SPAWN_EGG),
		//Summon(PANDA, PANDA_SPAWN_EGG),
		Summon(PILLAGER, PILLAGER_SPAWN_EGG),
		Summon(RAVAGER, RAVAGER_SPAWN_EGG),
		//Summon(TRADER_LLAMA, TRADER_LLAMA_SPAWN_EGG),
		//Summon(WANDERING_TRADER, WANDERING_TRADER_SPAWN_EGG),
		//Summon(FOX, FOX_SPAWN_EGG),
		//Summon(BEE, BEE_SPAWN_EGG),
		Summon(HOGLIN, HOGLIN_SPAWN_EGG),
		Summon(PIGLIN, PIGLIN_SPAWN_EGG),
		//Summon(STRIDER, STRIDER_SPAWN_EGG),
		Summon(ZOGLIN, ZOGLIN_SPAWN_EGG)
	)

	init {
		summons.sortBy { summon -> summon.type }
	}

	fun binarySearch(value: EntityType, array: Array<Summon>): Summon? {
		var start = 0
		var end = array.size - 1
		var lookFor = value.ordinal

		while (true) {
			var position = (end + start) / 2
			var compare = array[position].type.ordinal

			when {
				lookFor == compare -> return array[position]
				end - start == 1 -> return null
				lookFor < compare -> end = position
				lookFor > compare -> start = position
			}
		}
	}

	fun getSpawnEgg(entity : EntityType) : Material? {
		val summon = binarySearch(entity, summons)
				?: return null

		return summon.egg
	}
}