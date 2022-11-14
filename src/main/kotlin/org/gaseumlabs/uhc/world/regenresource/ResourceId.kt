package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Material
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.type.*

object ResourceId {
	private val list = ArrayList<RegenResource<*>>()

	fun register(regenResource: RegenResource<*>): Int {
		list.add(regenResource)
		return list.size - 1
	}

	val melon = RegenResourceMelon(
		hashMapOf(
			PhaseType.GRACE to 2, PhaseType.SHRINK to 2,
			PhaseType.BATTLEGROUND to 9, PhaseType.ENDGAME to 9,
		),

		WorldManager.GAME_WORLD_NAME,
		1.0f / 13.0f,
		"Melon"
	)

	val sugarCane = RegenResourceSugarCane(
		hashMapOf(
			PhaseType.GRACE to 48, PhaseType.SHRINK to 48,
			PhaseType.BATTLEGROUND to 9, PhaseType.ENDGAME to 9,
		),

		WorldManager.GAME_WORLD_NAME,
		1.0f / 4.0f,
		"Sugar cane"
	)

	val leather = RegenResourceLeather(
		hashMapOf(
			PhaseType.GRACE to 16, PhaseType.SHRINK to 16,
			PhaseType.BATTLEGROUND to 9, PhaseType.ENDGAME to 9,
		),

		WorldManager.GAME_WORLD_NAME,
		1.0f / 10.0f,
		"Leather"
	)

	val blaze = RegenResourceBlaze(
		hashMapOf(
			PhaseType.GRACE to 2, PhaseType.SHRINK to 2,
			PhaseType.BATTLEGROUND to 2, PhaseType.ENDGAME to 2,
		),
		WorldManager.NETHER_WORLD_NAME,
		1.0f / 13.0f,
		"Blaze"
	)

	val netherWart = RegenResourceNetherWart(
		hashMapOf(
			PhaseType.GRACE to 3, PhaseType.SHRINK to 3,
			PhaseType.BATTLEGROUND to 3, PhaseType.ENDGAME to 3,
		),
		WorldManager.NETHER_WORLD_NAME,
		1.0f / 9.0f,
		"Nether wart"
	)

	val diamond = RegenResourceOre(
		Material.DIAMOND_ORE,
		Material.DEEPSLATE_DIAMOND_ORE,
		3,
		-54..0,
		{ y -> y < 16 },
		true,

		hashMapOf(
			PhaseType.GRACE to 18, PhaseType.SHRINK to 18,
			PhaseType.BATTLEGROUND to 16, PhaseType.ENDGAME to 0,
		),
		WorldManager.GAME_WORLD_NAME,
		1.0f / 6.0f,
		"Diamond"
	)

	val gold = RegenResourceOre(
		Material.GOLD_ORE,
		Material.DEEPSLATE_GOLD_ORE,
		5,
		-54..32,
		{ true },
		true,

		hashMapOf(
			PhaseType.GRACE to -1, PhaseType.SHRINK to -1,
			PhaseType.BATTLEGROUND to 16, PhaseType.ENDGAME to 0,
		),
		WorldManager.GAME_WORLD_NAME,
		1.0f / 3.0f,
		"Gold"
	)

	val emerald = RegenResourceOre(
		Material.EMERALD_ORE,
		Material.DEEPSLATE_EMERALD_ORE,
		1,
		-54..48,
		{ true },
		true,

		hashMapOf(
			PhaseType.GRACE to -1, PhaseType.SHRINK to -1,
			PhaseType.BATTLEGROUND to 16, PhaseType.ENDGAME to 0,
		),
		WorldManager.GAME_WORLD_NAME,
		1.0f / 3.0f,
		"Emerald"
	)

	val ancientDebris = RegenResourceOre(
		Material.ANCIENT_DEBRIS,
		Material.ANCIENT_DEBRIS,
		2,
		32..110,
		{ true },
		false,

		hashMapOf(
			PhaseType.GRACE to -1, PhaseType.SHRINK to -1,
			PhaseType.BATTLEGROUND to -1, PhaseType.ENDGAME to -1,
		),
		WorldManager.NETHER_WORLD_NAME,
		1.0f / 4.0f,
		"Ancient debris"
	)

	val upperFish = RegenResourceFish(
		48..70,
		48..200,
		true,
		hashMapOf(
			PhaseType.GRACE to 6, PhaseType.SHRINK to 6,
			PhaseType.BATTLEGROUND to 4, PhaseType.ENDGAME to 4,
		),
		WorldManager.GAME_WORLD_NAME,
		1.0f / 12.0f,
		"Upper Fish"
	)

	val lowerFish = RegenResourceFish(
		-54..47,
		-64..47,
		false,
		hashMapOf(
			PhaseType.GRACE to 6, PhaseType.SHRINK to 6,
			PhaseType.BATTLEGROUND to 6, PhaseType.ENDGAME to 0,
		),
		WorldManager.GAME_WORLD_NAME,
		1.0f / 3.0f,
		"Lower Fish"
	)

	fun byKeyName(name: String): RegenResource<*>? {
		return list.find { it.idName == name }
	}

	fun allKeys() = list.map { it.idName }
}


