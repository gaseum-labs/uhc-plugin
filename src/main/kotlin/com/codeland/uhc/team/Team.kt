package com.codeland.uhc.team

import com.codeland.uhc.team.Team.Companion.internalAutomaticName
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import java.util.*
import kotlin.collections.ArrayList

class Team(val id: Int, color1: TextColor, color2: TextColor, val members: ArrayList<UUID>) {
	var name: String? = null
	val colors = arrayOf(color1, color2)

	/**
	 * call this during the game when
	 * the name is "guaranteed to be set"
	 */
	fun gameName() = name ?: "NULL"

	fun automaticName() {
		name = internalAutomaticName(members.mapNotNull { Bukkit.getOfflinePlayer(it).name })
	}

	fun apply(string: String): Component {
		return Util.gradientString(string, colors[0], colors[1])
	}

	companion object {
		fun internalAutomaticName(names: List<String>): String {
			return names.mapIndexed { i, name -> name.substring(
				(i * (name.length.toDouble() / names.size)).toInt(),
				((i + 1) * (name.length.toDouble() / names.size)).toInt(),
			)}.joinToString("")
		}
	}
}

fun main() {
	println(internalAutomaticName(listOf("balduvian", "mcrewind", "shiverisbjorn")))
}
