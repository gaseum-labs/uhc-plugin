package org.gaseumlabs.uhc.world.gen.UHCArea

abstract class UHCLayerMerge(seed: Long) : UHCLayer(seed) {
	lateinit var otherPrevious: UHCLayer
}
