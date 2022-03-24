package org.gaseumlabs.uhc.util.extensions

import org.bukkit.util.Vector

object VectorExtensions {
	operator fun Vector.plus(other: Vector): Vector {
		return Vector(
			this.x + other.x,
			this.y + other.y,
			this.z + other.z,
		)
	}

	operator fun Vector.minus(other: Vector): Vector {
		return Vector(
			this.x - other.x,
			this.y - other.y,
			this.z - other.z,
		)
	}

	operator fun Vector.times(n: Number): Vector {
		return Vector(
			this.x * n.toDouble(),
			this.y * n.toDouble(),
			this.z * n.toDouble(),
		)
	}

	operator fun Vector.div(n: Number): Vector {
		return this * (1 / n.toDouble())
	}

	operator fun Vector.unaryMinus(): Vector = this * -1
}