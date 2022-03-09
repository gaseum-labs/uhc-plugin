package com.codeland.uhc.util.extensions

import org.bukkit.Location
import org.bukkit.util.Vector

object LocationExtensions {
	operator fun Location.plus(other: Location): Location {
		return Location(
			this.world,
			this.x + other.x,
			this.y + other.y,
			this.z + other.z,
		)
	}

	operator fun Location.plus(other: Vector): Location {
		return Location(
			this.world,
			this.x + other.x,
			this.y + other.y,
			this.z + other.z,
		)
	}

	operator fun Location.minus(other: Location): Location {
		return Location(
			this.world,
			this.x - other.x,
			this.y - other.y,
			this.z - other.z,
		)
	}

	operator fun Location.minus(other: Vector): Location {
		return Location(
			this.world,
			this.x - other.x,
			this.y - other.y,
			this.z - other.z,
		)
	}

	// these don't really make sense in the context
	// of Locations but i'm keeping them for completeness

	operator fun Location.times(n: Number): Location {
		return Location(
			this.world,
			this.x * n.toDouble(),
			this.y * n.toDouble(),
			this.z * n.toDouble(),
		)
	}

	operator fun Location.div(n: Number): Location {
		return this * (1 / n.toDouble())
	}

	operator fun Location.unaryMinus(): Location = this * -1
}