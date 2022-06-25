package org.gaseumlabs.uhc.blockfix

import org.gaseumlabs.uhc.blockfix.blockfix.*

enum class BlockFixType(createBlockFix: () -> BlockFix) {
	LEAVES(::LeavesFix),
	GRAVEL(::GravelFix),
	MELON(::MelonFix),
	GILDED(::GildedFix),
	BOOKSHELF(::BookshelfFix);

	val blockFix = createBlockFix()
}
