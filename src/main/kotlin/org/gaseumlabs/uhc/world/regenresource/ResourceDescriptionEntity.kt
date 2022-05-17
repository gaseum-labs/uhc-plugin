package org.gaseumlabs.uhc.world.regenresource

abstract class ResourceDescriptionEntity(
	initialReleased: Int,
	maxReleased: Int,
	maxCurrent: Int,
	interval: Int,
) : ResourceDescription(
	initialReleased,
	maxReleased,
	maxCurrent,
	interval
)
