package com.codeland.uhc.team

import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Result
import net.kyori.adventure.text.format.TextColor
import java.util.*

class Team(
	val name: String,
	color0: TextColor,
	color1: TextColor,
	members: ArrayList<UUID>
) : AbstractTeam(
	arrayOf(color0, color1),
	members
) {
	fun serialize(): HashMap<String, *> {
		return hashMapOf(
			Pair("name", name),
			Pair("color0", colors[0].value()),
			Pair("color1", colors[1].value()),
			Pair("members", members.map { it.toString() }),
		)
	}

	companion object {
		fun deserialize(map: AbstractMap<String, *>): Result<Team> {
			val name = map["name"] as? String ?: return DiscordFilesystem.fieldError("name", "string")
			val color0 = (map["color0"] as? Double ?: return DiscordFilesystem.fieldError("color0", "int")).toInt()
			val color1 = (map["color1"] as? Double ?: return DiscordFilesystem.fieldError("color1", "int")).toInt()
			val members = (map["members"] as? ArrayList<String> ?: return DiscordFilesystem.fieldError("members", "array"))
				.map { try { UUID.fromString(it) } catch (ex: Exception) { return Bad(ex.message) } }

			return Good(Team(name, TextColor.color(color0), TextColor.color(color1), members as ArrayList<UUID>))
		}
	}
}
