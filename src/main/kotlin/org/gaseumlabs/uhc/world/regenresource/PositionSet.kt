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

class PositionSetBuilder {
	val bounds = ArrayList<Bounds>()

	fun addBounds(x: Int, y: Int, width: Int, height: Int): PositionSetBuilder {
		bounds.add(Bounds(x, y, width, height))
		return this
	}

	fun build(): PositionSet {
		val overlaps = ArrayList<ArrayList<Bounds>>()

		/* find all overlaps */
		while (bounds.isNotEmpty()) {
			val overlapSet = arrayListOf(bounds.removeLast())

			bounds.removeIf { bound1 ->
				if (overlapSet.any { inSet -> inSet.overlapping(bound1) }) {
					overlapSet.add(bound1)
					true
				} else {
					false
				}
			}

			overlaps.add(overlapSet)
		}

		/* fill in positions in the overlaps */
		val gridBounds = Array(overlaps.size) { i -> Bounds.combineAll(overlaps[i]) }
		val grids = Array(overlaps.size) { i ->
			val gridBound = gridBounds[i]
			val array = BooleanArray(gridBound.area())

			for (subBound in overlaps[i]) {
				val intersection = gridBound.intersection(subBound)

				for (y in intersection.yRange()) {
					for (x in intersection.xRange()) {
						array[gridBound.indexInto(x, y)] = true
					}
				}
			}

			array
		}

		return PositionSet(gridBounds, grids)
	}
}

class PositionSet(
	val gridBounds: Array<Bounds>,
	val grids: Array<BooleanArray>,
) {
	fun inSet(x: Int, y: Int): Boolean {
		for (i in gridBounds.indices) {
			val bound = gridBounds[i]
			if (bound.inside(x, y)) return grids[i][bound.indexInto(x, y)]
		}

		return false
	}

	fun newIndicesVs(old: PositionSet, onPosition: (x: Int, y: Int) -> Unit) {
		for (i in gridBounds.indices) {
			val bound = gridBounds[i]
			val tempGrid = grids[i].copyOf()

			for (j in old.gridBounds.indices) {
				val oldBound = old.gridBounds[j]
				val oldGrid = old.grids[j]

				val intersection = bound.intersection(oldBound)

				for (y in intersection.yRange()) {
					for (x in intersection.xRange()) {
						if (oldGrid[oldBound.indexInto(x, y)]) {
							tempGrid[bound.indexInto(x, y)] = false
						}
					}
				}
			}

			for (y in bound.yRange()) {
				for (x in bound.xRange()) {
					if (tempGrid[bound.indexInto(x, y)]) {
						onPosition(x, y)
					}
				}
			}
		}
	}
}
