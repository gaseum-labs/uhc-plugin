package com.codeland.uhc.world.gen.layer.game2

import kotlin.random.Random

enum class Temperature {
	TEMPERATE,
	HOT,
	COLD,
	BADLANDS,
	MEGA,
	OCEAN;

	companion object {
		fun randomSpecial(random: Random): Temperature {
			return values()[random.nextInt(HOT.ordinal, MEGA.ordinal + 1)]
		}
	}
}
