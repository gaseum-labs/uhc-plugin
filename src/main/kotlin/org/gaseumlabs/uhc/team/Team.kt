package org.gaseumlabs.uhc.team

import com.google.gson.*
import net.kyori.adventure.text.format.TextColor
import java.util.*

class Team(
	var name: String,
	color0: TextColor,
	color1: TextColor,
	members: ArrayList<UUID>,
) : AbstractTeam(
	arrayOf(color0, color1),
	members
) {
	fun serialize(): JsonObject {
		val obj = JsonObject()
		obj.addProperty("name", name)
		obj.addProperty("color0", colors[0].value())
		obj.addProperty("color1", colors[1].value())

		val membersArray = JsonArray(members.size)
		for (member in members) membersArray.add(member.toString())
		obj.add("members", membersArray)

		return obj
	}

	override fun grabName(): String {
		return name
	}

	override fun giveName(name: String) {
		this.name = name
	}

	companion object {
		fun deserialize(jsonElement: JsonElement): Team {
			jsonElement as JsonObject

			val name = jsonElement.get("name").asString
			val color0 = TextColor.color(jsonElement.get("color0").asInt)
			val color1 = TextColor.color(jsonElement.get("color1").asInt)
			val members = ArrayList(jsonElement.get("members").asJsonArray.map { UUID.fromString(it.asString) })

			return Team(name, color0, color1, members)
		}
	}
}
