package com.codeland.uhc.command.ubt

import java.io.FileInputStream
import java.io.FileOutputStream

class BlockRep(val blockID: Int, val repeat: Int, val tags: Array<TagRep>) {
	companion object {
		const val MAX_BLOCK_ID = 0x3FF
		const val MAX_REPEAT = 0x1F
		const val MAX_REPEAT_TAGS = 0x3FF
		const val MAX_TAGS = 7

		fun read(stream: FileInputStream): BlockRep {
			/* mot yet implemented */
			return BlockRep(0, 0, emptyArray())
		}
	}

	fun write(stream: FileOutputStream) {
		val blockBytes = 0

		/* place the id in the 10 bits to the right of the most significant bit */
		blockBytes.or(blockID.shl(5))

		if (tags.isEmpty()) {
			/* palce the repeat in the first 5 bits */
			blockBytes.or(repeat)

		} else {
			/* place has data bit (most significant bit of a short) */
			blockBytes.or(0x00008000)

			/* place the most significant 5 bits in the last 5 bits of the short */
			blockBytes.or(repeat.ushr(5))
		}

		/* write most significant byte then least significant byte */
		stream.write(blockBytes.ushr(8).and(0xFF))
		stream.write(blockBytes.and(0xFF))

		if (tags.isNotEmpty()) {
			val tagHeaderByte = 0

			/* place the least significant 5 bits in the first 5 bits of the header */
			tagHeaderByte.or(repeat.and(0x1F).shl(3))

			/* place the repeats in the last 3 bits of the header */
			tagHeaderByte.or(tags.size.and(7))

			tags.forEach { tag ->
				/* write the tag id as 1 byte */
				stream.write(tag.tagID.and(0xFF))

				val valueByte = 0

				/* place the 2 tag bits in the 2 most significant bits of the value byte */
				valueByte.or(tag.type.and(3).shl(6))

				if (tag.type == TagRep.TYPE_STRING) {
					/* place the value in the 7 least significant bits */
					valueByte.or(tag.value.and(0x7F))

				} else {
					/* place the value in the 6 least significant bits */
					valueByte.or(tag.value.and(0x3F))
				}

				stream.write(valueByte)
			}
		}
	}

	class TagRep(val tagID: Int, val type: Int, val value: Int) {
		companion object {
			const val TYPE_NUMBER = 0
			const val TYPE_BOOLEAN = 1
			const val TYPE_STRING = 2

			const val MAX_TAG_ID = 0xFF
			const val MAX_VALUE = 0x7F
		}
	}
}
