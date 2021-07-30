package com.codeland.uhc.blockfix

enum class BlockFixType(createBlockFix: () -> BlockFix) {
	LEAVES(::LeavesFix),
	BROWN_MUSHROOM(::BrownMushroomFix),
	RED_MUSHROOM(::RedMushroomFix),
	GRAVEL(::GravelFix),
	MELON(::MelonFix),
	NETHER_WART(::NetherWartFix),
	GILDED(::GildedFix);

	val blockFix = createBlockFix()
}
