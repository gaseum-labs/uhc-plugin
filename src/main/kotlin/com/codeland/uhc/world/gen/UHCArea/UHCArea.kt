package com.codeland.uhc.world.gen.UHCArea

class UHCArea(baseLayer: UHCLayer) {
	var topLayer = baseLayer

	fun addLayer(layer: UHCLayer): UHCArea {
		layer.previous = topLayer

		topLayer = layer
		return this
	}

	fun merge(other: UHCArea, layer: UHCLayerMerge): UHCArea {
		layer.previous = topLayer
		layer.otherPrevious = other.topLayer

		topLayer = layer
		return this
	}

	fun sample(x: Int, z: Int): Int {
		return topLayer.sample(x, z)
	}
}