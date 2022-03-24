package org.gaseumlabs.uhc.blockfix

import org.gaseumlabs.uhc.blockfix.blockfix.*

enum class BlockFixType(createBlockFix: () -> org.gaseumlabs.uhc.blockfix.BlockFix) {
	LEAVES(::LeavesFix),
	GRAVEL(::GravelFix),
	MELON(::MelonFix),
	NETHER_WART(::NetherWartFix),
	GILDED(::GildedFix);

	val blockFix = createBlockFix()
}
