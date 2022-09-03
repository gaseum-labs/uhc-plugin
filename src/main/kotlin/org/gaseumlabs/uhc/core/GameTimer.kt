package org.gaseumlabs.uhc.core

class GameTimer {
	enum class Mode {
		NONE,
		LAUNCHED,
		GAMING
	}

	private var timer = 0
	var mode = Mode.NONE

	fun onMode(mode: Mode) = this.mode == mode
	fun tick() = (++timer)
	fun get() = timer

	fun reset() {
		timer = 0
		mode = Mode.NONE
	}

	fun launch() {
		timer = -10
		mode = Mode.LAUNCHED
	}

	fun start() {
		timer = -10
		mode = Mode.GAMING
	}
}