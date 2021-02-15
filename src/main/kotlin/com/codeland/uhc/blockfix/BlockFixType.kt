package com.codeland.uhc.blockfix

enum class BlockFixType(private val createBlockFix: () -> BlockFix) {
	LEAVES(::LeavesFix),
	BROWN_MUSHROOM(::BrownMushroomFix),
	RED_MUSHROOM(::RedMushroomFix),
	GRAVEL(::GravelFix),
	MELON(::MelonFix),
	NETHER_WART(::NetherWartFix);

	val blockFix = createBlockFix()
}
