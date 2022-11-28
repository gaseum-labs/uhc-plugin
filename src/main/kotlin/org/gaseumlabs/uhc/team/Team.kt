package org.gaseumlabs.uhc.team

import com.google.gson.*
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.block.Banner
import java.util.*

class Team(
	var name: String,
	color0: DyeColor,
	color1: DyeColor,
	members: ArrayList<UUID>,
	val bannerPattern: Banner,
	uuid: UUID,
) : AbstractTeam(
	arrayOf(color0, color1),
	members,
	uuid
) {
	fun serialize(): JsonObject {
		val obj = JsonObject()
		obj.addProperty("name", name)
		obj.addProperty("color0", colors[0].color.asRGB())
		obj.addProperty("color1", colors[1].color.asRGB())

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
}
