package com.codeland.uhc.core

class WorldSettings(settings: Array<Boolean>) {
	val netherWorldFix: Boolean
	val mushroomWorldFix: Boolean
	val oreWorldFix: Boolean
	val melonWorldFix: Boolean
	val dungeonWorldFix: Boolean
	val sugarCaneWorldFix: Boolean
	val netherIndicators: Boolean
	val halloweenGeneration: Boolean
	val christmasGeneration: Boolean
	val chunkSwapping: Boolean
	val waterWorld: Boolean

	init {
		netherWorldFix = settings[0]
		mushroomWorldFix = settings[1]
		oreWorldFix = settings[2]
		melonWorldFix = settings[3]
		dungeonWorldFix = settings[4]
		sugarCaneWorldFix = settings[5]
		netherIndicators = settings[6]
		halloweenGeneration = settings[7]
		christmasGeneration = settings[8]
		chunkSwapping = settings[9]
		waterWorld = settings[10]
	}
}
