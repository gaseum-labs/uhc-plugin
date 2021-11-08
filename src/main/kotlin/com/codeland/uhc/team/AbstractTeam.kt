package com.codeland.uhc.team

import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.*

abstract class AbstractTeam(val colors: Array<TextColor>, val members: ArrayList<UUID>) {
	fun apply(string: String): Component {
		return Util.gradientString(string, colors[0], colors[1])
	}

	abstract fun grabName(): String

	abstract fun giveName(name: String)
}