package org.gaseumlabs.uhc.team

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.gaseumlabs.uhc.util.Util
import java.util.*

abstract class AbstractTeam(val colors: Array<TextColor>, val members: ArrayList<UUID>) {
	fun apply(string: String): TextComponent {
		return Util.gradientString(string, colors[0], colors[1])
	}

	abstract fun grabName(): String

	abstract fun giveName(name: String)
}