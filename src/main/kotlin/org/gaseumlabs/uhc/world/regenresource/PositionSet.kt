package org.gaseumlabs.uhc.world.regenresource

import kotlin.math.min
import kotlin.math.max

data class Bounds(val x: Int, val y: Int, val width: Int, val height: Int) {
	fun right() = x + width
	fun down() = y + height
	fun area() = width * height
	fun xRange() = x until right()
	fun yRange() = y until down()

	fun overlapping(other: Bounds): Boolean {
		return other.x in ((x - other.width + 1) until x + width) && other.y in ((y - other.height + 1) until y + height)
	}

	fun intersection(other: Bounds): Bounds {
		val left = max(x, other.x)
		val up = max(y, other.y)
		val right = min(right(), other.right())
		val down = min(down(), other.down())

		return Bounds(
			left,
			up,
			right - left,
			down - up,
		)
	}

	fun indexInto(accessX: Int, accessY: Int): Int {
		return (accessY - y) * width + (accessX - x)
	}

	fun inside(accessx: Int, accessy: Int): Boolean {
		return accessx in xRange() && accessy in yRange()
	}

	companion object {
		fun combineAll(boundsSet: ArrayList<Bounds>): Bounds {
			var mostLeft = boundsSet[0].x
			var mostUp = boundsSet[0].y
			var mostRight = boundsSet[0].right()
			var mostDown = boundsSet[0].down()

			for (i in 1 until boundsSet.size) {
				val other = boundsSet[i]
				if (other.x < mostLeft) mostLeft = other.x
				if (other.y < mostUp) mostUp = other.y
				if (other.right() < mostRight) mostRight = other.right()
				if (other.down() < mostDown) mostDown = other.down()
			}

			return Bounds(
				mostLeft,
				mostUp,
				mostRight - mostLeft,
				mostDown - mostUp
			)
		}
	}
}
