package org.gaseumlabs.uhc.util.extensions

object IntRangeExtensions {
	fun IntRange.rangeIntersection(other: IntRange): IntRange {
		return this.first.coerceAtLeast(other.first)..this.last.coerceAtMost(other.last)
	}
}
