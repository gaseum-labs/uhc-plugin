package com.codeland.uhc.team

import com.codeland.uhc.team.Team.Companion.internalAutomaticName
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import java.util.*
import kotlin.collections.ArrayList

class Team(val id: Int, val color1: TextColor, val color2: TextColor, val members: ArrayList<UUID>) {
	var name: String? = null

	/**
	 * call this during the game when
	 * the name is "guaranteed to be set"
	 */
	fun gameName() = name ?: "NULL"

	fun automaticName() {
		name = internalAutomaticName(members.mapNotNull { Bukkit.getOfflinePlayer(it).name })
	}

	companion object {
		fun internalAutomaticName(names: List<String>): String {
			val parts = names.size
			return names.mapIndexed { i, name ->
				val len = name.length

				name.substring(
					(i * (len.toDouble() / parts)).toInt(),
					((i + 1) * (len.toDouble() / parts)).toInt(),
				)
			}.joinToString()
		}
	}
}

fun main() {
	println(internalAutomaticName(listOf("balduvian", "mcrewind", "shiverisbjorn")))
}
