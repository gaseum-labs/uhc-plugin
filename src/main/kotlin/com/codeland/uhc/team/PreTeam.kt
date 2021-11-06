package com.codeland.uhc.team

import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import java.util.*

class PreTeam(
	color0: TextColor,
	color1: TextColor,
	members: ArrayList<UUID>
) : AbstractTeam(
	arrayOf(color0, color1),
	members
) {
	var name: String? = null

	fun toTeam(): Team {
		return Team(
			name ?: nameParts.random(),
			colors[0],
			colors[1],
			members
		)
	}

	companion object {
		fun internalAutomaticName(names: List<String>): String {
			return names.mapIndexed { i, name -> name.substring(
				(i * (name.length.toDouble() / names.size)).toInt(),
				((i + 1) * (name.length.toDouble() / names.size)).toInt(),
			)}.joinToString("")
		}

		val nameParts = Material.values()
			.filter { !it.name.startsWith("LEGACY_") }
			.map { material -> material.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() } }
	}
}