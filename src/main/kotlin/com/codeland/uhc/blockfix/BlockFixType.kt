package com.codeland.uhc.blockfix

enum class BlockFixType(private val createBlockFix: () -> BlockFix) {
	LEAVES_FIX(::LeavesFix),
	BROWN_MUSHROOM_FIX(::BrownMushroomFix),
	RED_MUSHROOM_FIX(::RedMushroomFix),
	GRAVEL_FIX(::GravelFix);

	val blockFix = createBlockFix()
}
