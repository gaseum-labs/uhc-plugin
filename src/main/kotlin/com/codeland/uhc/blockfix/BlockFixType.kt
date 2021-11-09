package com.codeland.uhc.blockfix

import com.codeland.uhc.blockfix.blockfix.*

enum class BlockFixType(createBlockFix: () -> BlockFix) {
	LEAVES(::LeavesFix),
	GRAVEL(::GravelFix),
	MELON(::MelonFix),
	NETHER_WART(::NetherWartFix),
	GILDED(::GildedFix);

	val blockFix = createBlockFix()
}
