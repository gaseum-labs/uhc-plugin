package org.gaseumlabs.uhc.world.regenresource

sealed class Release

data class Tier(val released: Int, val tier: Int, val spawnChance: Float) {
	companion object {
		fun default(tier: Int, spawnChance: Float) = Tier(-1, tier, spawnChance)

		fun none() = Tier(0, 0, 0.0f)
	}

	fun isUnlimited() = released == -1

	fun isNone() = released == 0
}

class ReleaseChunked(
	private vararg val tiers: Tier,
) : Release() {
	companion object {
		fun unlimited(tier: Tier) = ReleaseChunked(tier)
	}

	fun getTier(collected: Int): Tier {
		var soFar = 0

		for (tier in tiers) {
			if (tier.isUnlimited() || collected < soFar + tier.released) return tier

			soFar += tier.released
		}

		return Tier.none()
	}
}

class ReleaseBattleground(
	val partition: ResourcePartition
) : Release()
