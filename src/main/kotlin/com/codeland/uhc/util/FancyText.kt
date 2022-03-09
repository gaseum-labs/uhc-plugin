package com.codeland.uhc.util

import kotlin.random.Random

object FancyText {
	val table = arrayOf(
		arrayOf('A', '\u1d27', '\u1430', '\u15c5', '\u15e9'),
		arrayOf('B', '\u0392', '\u15f8'),
		arrayOf('C', '\u00a9', '\u2ca4', '\u122d', '\u1455'),
		arrayOf('D', '\u2ad0', '\u15ea'),
		arrayOf('E', '\u2c88', '\u2d39', '\u13cb', '\u22f5'),
		arrayOf('F', '\u0584', '\uff26'),
		arrayOf('G', '\uff27', '\u01f4'),
		arrayOf('H', '\u04a2', '\u4dcf', '\u12e0', '\u210c'),
		arrayOf('I', '\u0f0f', '\u1963', '\u2139', '\u2503'),
		arrayOf('J', '\u1353', '\u149b', '\u1887'),
		arrayOf('K', '\u04a0', '\u051e', '\u16d5'),
		arrayOf('L', '\u14b6', '\u1967', '\u230a'),
		arrayOf('M', '\u1bcb', '\u0717', '\u1320', '\u164f'),
		arrayOf('N', '\u1262', '\u24c3', '\ua499'),
		arrayOf('O', '\u0a66', '\u1c5b', '\u4dda', '\u09e6'),
		arrayOf('P', '\u2c63', '\u146d', '\u15b0'),
		arrayOf('Q', '\u10b3', '\u211a', '\ua755'),
		arrayOf('R', '\u00ae', '\u13a1', '\u1587', '\u211c'),
		arrayOf('S', '\u10bd', '\u1949'),
		arrayOf('T', '\u03d2', '\u0372', '\u1350', '\u1748'),
		arrayOf('U', '\u2a4f', '\u2a03', '\u2a3f', '\u1201'),
		arrayOf('V', '\u2a52', '\u074d', '\u1720', '\u194e'),
		arrayOf('W', '\u0bf0', '\u0e1c', '\u0429'),
		arrayOf('X', '\u04fc', '\u16b7', '\u1763', '\u3024'),
		arrayOf('Y', '\u038e', '\u31a9'),
		arrayOf('Z', '\u2c8c', '\u2f04', '\u1901'),
	)

	fun make(input: String): String {
		val random = Random(input.hashCode())
		val builder = StringBuilder()

		for (c in input) {
			val char = c.lowercaseChar()

			if (char in 'a'..'z') {
				val options = table[char - 'a']
				builder.append(options[random.nextInt(options.size)])
			} else {
				builder.append(char)
			}
		}

		return builder.toString()
	}
}
