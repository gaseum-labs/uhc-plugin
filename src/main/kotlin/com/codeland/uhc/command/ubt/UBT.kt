package com.codeland.uhc.command.ubt

import java.io.FileOutputStream
import java.lang.StringBuilder
import kotlin.experimental.or
import kotlin.math.ceil

class UBT {
	var ubtWidth: Int = 0
	var ubtHeight: Int = 0
	var ubtDepth: Int = 0

	var blockMap = HashMap<String, Short>()
	var numBlocks: Int = 0

	companion object {
		val MAX_ID = 0b1111111111
		val MAX_REPEAT = 0b11111

		fun writeBlock(stream: FileOutputStream, id: Int, repeat: Int, data: ByteArray) {
			val blockBytes = 0

			/* place has data bit (most significant bit of a short) */
			if (data != null) blockBytes.or(0x00008000)

			/* place the id in the 10 bits to the right of the most significant bit */
			blockBytes.or(id.shl(5))

			/* palce the repeat in the first 5 bits */
			blockBytes.or(repeat)

			/* write bytes into the stream */
			stream.write(blockBytes.ushr(8))
			stream.write(blockBytes.and(0xFF))
		}

		fun stringToUBTString(string: String): ByteArray {
			return ByteArray(string.length + 1) { i ->
				/* null terminator */
				if (i == string.length) {
					0
				} else {
					var currentChar = string[i].toInt()

					when {
						currentChar >= 97 ->
							/* make room for the 5 special characters and null */
							currentChar -= (97 + 6)
						currentChar == 95 ->
							/* underscore */
							currentChar = 5
						currentChar == 44 ->
							/* comma */
							currentChar = 4
						currentChar <= 57 ->
							/* numbers get encoded as a - j */
							/* starting from 48 ('0') */
							currentChar -= (48 + 6)
						else ->
							/* equality from ; < = */
							/* make room for null character */
							currentChar -= (59 + 1)
					}

					currentChar.toByte()
				}
			}
		}

		fun UBTStringToString(byteArray: ByteArray): String {
			val builder = StringBuilder(byteArray.size - 1)

			for (i in 0 until byteArray.size - 1) {
				var currentChar = byteArray[i].toInt()

				when {
					currentChar <= 3 -> currentChar += 59 - 1 /* equality - the value of ; (1) */
					currentChar == 4 -> currentChar  = 44     /* comma */
					currentChar == 5 -> currentChar  = 95     /* underscore */
					else             -> currentChar += 97 - 6 /* alphabet - the value of a (6) */
				}

				builder.append(currentChar.toChar())
			}

			return builder.toString()
		}

		fun stringToNBTString(string: String): String {
			val builder = StringBuilder((string.length * 3) / 2)

			builder.append('[')

			val STATE_NORMAL = 0
			val STATE_NUMBER = 1
			val STATE_BOOLEAN = 2

			var state = STATE_NORMAL

			for (currentChar in string) {
				when (state) {
					STATE_NORMAL -> {
						when (currentChar) {
							';' -> {
								state = STATE_NUMBER
								builder.append('=')
							}
							'<' -> {
								state = STATE_BOOLEAN
								builder.append('=')
							}
							else -> builder.append(currentChar)
						}
					}
					STATE_NUMBER -> {
						when (currentChar) {
							',' -> {
								state = STATE_NORMAL
								builder.append(',')
							}
							else -> {
								builder.append((currentChar - ('a' - '0'.toInt())).toChar())
							}
						}
					}
					STATE_BOOLEAN -> {
						when (currentChar) {
							'a' -> {
								builder.append("false")
							}
							'b' -> {
								builder.append("true")
							}
							else -> {
								state = STATE_NORMAL
								builder.append(currentChar)
							}
						}
					}
				}
			}

			builder.append(']')

			return builder.toString()
		}

		fun NBTStringToString(string: String): String {
			val builder = StringBuilder(string.length - 2)

			val FALSE = "false"
			val TRUE = "true"

			/* skip the first and last character */
			var i = 1
			while (i < string.length - 1) {
				val currentChar = string[i]

				if (currentChar == '=') {
					++i
					val start = i

					var isTrue = true
					var isFalse = true
					var isNumber = true

					while (string[i] != ']' && string[i] != ',') {
						if (isFalse && ((i - start) == FALSE.length || string[i] != FALSE[i - start]))
							isFalse = false

						if (isTrue && ((i - start) == TRUE.length || string[i] != TRUE[i - start]))
							isTrue = false

						if (string[i] < '0' || string[i] > '9')
							isNumber = false

						++i
					}

					builder.append(when {
						isFalse  -> "<a"
						isTrue   -> "<b"
						isNumber -> {
							val numberArray = ByteArray(i - start) { j ->
								(string[start + j] - '0' + 'a'.toInt()).toByte()
							}
							";${String(numberArray)}"
						}
						else     -> "=${string.substring(start, i)}"
					})

				} else {
					builder.append(currentChar)
					++i
				}
			}

			return builder.toString()
		}

		fun writeString(stream: FileOutputStream, data: ByteArray) {
			/* relative position to start filling the character within the byte */
			var bitOffset = 0

			var currentByte = 0

			for (i in data.indices) {
				val currentChar = data[i].toInt()

				/* -3 starts the 5 bits at the beginning of the byte */
				/* the bit offset then shifts the bit right potentially trimming the least significant ones */
				currentByte.or(currentChar.ushr(bitOffset - 3))

				/* advance by length of written 5 bits */
				bitOffset += 5
				/* if there was trimming and another byte needs to be written to to store the least significant bits */
				/* or if the 5 bits fit perfectly into the end of the byte */
				if (bitOffset >= 8) {
					stream.write(currentByte.and(0xFF))

					currentByte = 0
					/* 8 puts the 5 bits ending right before the start of the byte */
					/* (bit offset % 8) is the amount that was trimmed from the last write */
					currentByte.or(currentChar.shl(8 - (bitOffset % 8)))
				}

				/* new position within the current byte to start writing */
				bitOffset %= 8
			}

			/* non 0 bit offest signifies there's a byte that has not been written yet */
			if (bitOffset != 0) {
				stream.write(currentByte.and(0xFF))
			}
		}
	}
}
