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

	private inline fun ratio(a: Int, b: Int) = a.toFloat() / b.toFloat()

	val melon = RegenResourceMelon(
		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier(2, 0, ratio(1, 13)),
				Tier.default(0, ratio(1, 26)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier(2, 0, ratio(1, 13)),
				Tier.default(0, ratio(1, 26)),
			),
			PhaseType.BATTLEGROUND to ReleaseBattleground(
				ResourcePartitionGrid(3, 3)
			),
			PhaseType.ENDGAME to ReleaseBattleground(
				ResourcePartitionGrid(3, 3)
			),
		),

		WorldManager.GAME_WORLD_NAME,
		"Melon"
	)

	val sugarCane = RegenResourceSugarCane(
		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier(36, 0, ratio(1, 4)),
				Tier(30, 1, ratio(1, 4)),
				Tier.default(2, ratio(1, 4)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier(36, 0, ratio(1, 4)),
				Tier(30, 1, ratio(1, 4)),
				Tier.default(2, ratio(1, 4)),
			),
			PhaseType.BATTLEGROUND to ReleaseBattleground(
				ResourcePartitionGrid(3, 3)
			),
			PhaseType.ENDGAME to ReleaseBattleground(
				ResourcePartitionGrid(3, 3)
			),
		),

		WorldManager.GAME_WORLD_NAME,
		"Sugar cane"
	)

	val leather = RegenResourceLeather(
		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier(12, 0, ratio(1, 10)),
				Tier(10, 0, ratio(1, 16)),
				Tier.default(0, ratio(1, 25)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier(12, 0, ratio(1, 10)),
				Tier(10, 0, ratio(1, 16)),
				Tier.default(0, ratio(1, 25)),
			),
			PhaseType.BATTLEGROUND to ReleaseBattleground(
				ResourcePartitionGrid(3, 3)
			),
			PhaseType.ENDGAME to ReleaseBattleground(
				ResourcePartitionGrid(2, 3)
			),
		),

		WorldManager.GAME_WORLD_NAME,
		"Leather"
	)

	val blaze = RegenResourceBlaze(
		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier(2, 0, ratio(1, 13)),
				Tier(2, 0, ratio(1, 21)),
				Tier.default(1, ratio(1, 13)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier(2, 0, ratio(1, 13)),
				Tier(2, 0, ratio(1, 21)),
				Tier.default(1, ratio(1, 13)),
			),
			PhaseType.BATTLEGROUND to ReleaseChunked(
				Tier.default(0, ratio(1, 21)),
			),
		),

		WorldManager.NETHER_WORLD_NAME,
		"Blaze"
	)

	val netherWart = RegenResourceNetherWart(
		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier(3, 0, ratio(1, 9)),
				Tier.default(0, ratio(1, 14)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier(3, 0, ratio(1, 9)),
				Tier.default(0, ratio(1, 14)),
			),
			PhaseType.BATTLEGROUND to ReleaseChunked(
				Tier.default(0, ratio(1, 11)),
			),
		),

		WorldManager.NETHER_WORLD_NAME,
		"Nether wart"
	)

	val diamond = RegenResourceOre(
		Material.DIAMOND_ORE,
		Material.DEEPSLATE_DIAMOND_ORE,
		arrayOf(3, 2, 1),
		-54..0,
		{ y -> y < 16 },
		true,

		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier(15, 0, ratio(1, 5)),
				Tier(12, 1, ratio(1, 5)),
				Tier.default(2, ratio(1, 5)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier(15, 0, ratio(1, 5)),
				Tier(12, 1, ratio(1, 5)),
				Tier.default(2, ratio(1, 5)),
			),
			PhaseType.BATTLEGROUND to ReleaseBattleground(
				ResourcePartitionGrid(4, 4)
			),
		),

		WorldManager.GAME_WORLD_NAME,
		"Diamond"
	)

	val gold = RegenResourceOre(
		Material.GOLD_ORE,
		Material.DEEPSLATE_GOLD_ORE,
		arrayOf(5),
		-54..32,
		{ true },
		true,

		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier.default(0, ratio(1, 3)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier.default(0, ratio(1, 3)),
			),
			PhaseType.BATTLEGROUND to ReleaseBattleground(
				ResourcePartitionGrid(4, 4)
			),
		),

		WorldManager.GAME_WORLD_NAME,
		"Gold"
	)

	val emerald = RegenResourceOre(
		Material.EMERALD_ORE,
		Material.DEEPSLATE_EMERALD_ORE,
		arrayOf(1),
		-54..48,
		{ true },
		true,

		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier.default(0, ratio(1, 3)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier.default(0, ratio(1, 3)),
			),
			PhaseType.BATTLEGROUND to ReleaseBattleground(
				ResourcePartitionGrid(4, 4)
			),
		),

		WorldManager.GAME_WORLD_NAME,
		"Emerald"
	)

	val ancientDebris = RegenResourceOre(
		Material.ANCIENT_DEBRIS,
		Material.ANCIENT_DEBRIS,
		arrayOf(2),
		32..110,
		{ true },
		false,

		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier.default(0, ratio(1, 4)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier.default(0, ratio(1, 4)),
			),
			PhaseType.BATTLEGROUND to ReleaseChunked(
				Tier.default(0, ratio(1, 4)),
			),
		),

		WorldManager.NETHER_WORLD_NAME,
		"Ancient debris"
	)

	val upperFish = RegenResourceFish(
		48..70,
		48..200,
		true,

		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier(5, 0, ratio(1, 5)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier(5, 0, ratio(1, 5)),
			),
			PhaseType.BATTLEGROUND to ReleaseBattleground(
				ResourcePartitionGrid(3, 2)
			),
			PhaseType.ENDGAME to ReleaseBattleground(
				ResourcePartitionGrid(3, 2)
			),
		),

		WorldManager.GAME_WORLD_NAME,
		"Upper Fish"
	)

	val lowerFish = RegenResourceFish(
		-54..47,
		-64..47,
		false,

		hashMapOf(
			PhaseType.GRACE to ReleaseChunked(
				Tier(5, 0, ratio(1, 3)),
			),
			PhaseType.SHRINK to ReleaseChunked(
				Tier(5, 0, ratio(1, 3)),
			),
			PhaseType.BATTLEGROUND to ReleaseBattleground(
				ResourcePartitionGrid(2, 3)
			),
		),

		WorldManager.GAME_WORLD_NAME,
		"Lower Fish"
	)

	/* -------------------------------------------------- */

	fun byKeyName(name: String): RegenResource<*>? {
		return list.find { it.idName == name }
	}

	fun allKeys() = list.map { it.idName }
}


