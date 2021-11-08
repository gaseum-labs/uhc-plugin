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
			name ?: randomName(),
			colors[0],
			colors[1],
			members
		)
	}

	override fun grabName(): String {
		return name ?: "[Name not chosen]"
	}

	override fun giveName(name: String) {
		this.name = name
	}

	companion object {
		fun randomName(): String {
			return nameParts.random()
		}

		val nameParts = Material.values()
			.filter { !it.name.startsWith("LEGACY_") }
			.map { material -> material.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() } }
	}
}