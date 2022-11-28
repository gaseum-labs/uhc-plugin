package org.gaseumlabs.uhc.team

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.DyeColor
import org.gaseumlabs.uhc.util.Util
import java.util.*

abstract class AbstractTeam(val colors: Array<DyeColor>, val members: ArrayList<UUID>, val uuid: UUID) {
	fun apply(string: String): TextComponent {
		return Util.gradientString(string, colors[0], colors[1])
	}

	fun swapColors() {
		colors.reverse()
	}

	abstract fun grabName(): String

	abstract fun giveName(name: String)
}