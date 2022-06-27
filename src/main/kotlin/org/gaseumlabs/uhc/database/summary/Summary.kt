package org.gaseumlabs.uhc.database.summary

import com.google.gson.*
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.util.*
import org.gaseumlabs.uhc.util.Util.fieldError
import java.io.InputStream
import java.io.InputStreamReader
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.*

class Summary(
	val gameType: GameType,
	val date: ZonedDateTime,
	val gameLength: Int,
	val teams: List<Team>,
	val players: List<SummaryEntry>,
) {
	data class SummaryEntry(
		val place: Int,
		val uuid: UUID,
		val name: String,
		val timeSurvived: Int,
		val killedBy: UUID?,
	) {
		fun serialize(): JsonObject {
			val obj = JsonObject()
			obj.addProperty("place", place)
			obj.addProperty("uuid", uuid.toString())
			obj.addProperty("name", name)
			obj.addProperty("timeSurvived", timeSurvived)
			obj.addProperty("killedBy", killedBy?.toString())
			return obj
		}

		companion object {
			fun deserialize(jsonElement: JsonElement): SummaryEntry {
				jsonElement as JsonObject

				val place = jsonElement.get("place").asInt
				val uuid = UUID.fromString(jsonElement.get("uuid").asString)
				val name = jsonElement.get("name").asString
				val timeSurvived = jsonElement.get("timeSurvived").asInt
				val killedBy = jsonElement.get("killedBy")?.asString

				return SummaryEntry(
					place,
					uuid,
					name,
					timeSurvived,
					if (killedBy == null) null else UUID.fromString(killedBy)
				)
			}
		}
	}

	fun serialize(): JsonObject {
		val obj = JsonObject()
		obj.addProperty("gameType", gameType.name)
		obj.addProperty("date", date.toString())
		obj.addProperty("gameLength", gameLength)

		val teamsArray = JsonArray(teams.size)
		for (team in teams) teamsArray.add(team.serialize())
		obj.add("teams", teamsArray)

		val playersArray = JsonArray(players.size)
		for (entry in players) playersArray.add(entry.serialize())
		obj.add("players", playersArray)

		return obj
	}

	/* util */

	fun numKills(uuid: UUID): Int {
		return players.fold(0) { acc, entry ->
			acc + if (entry.uuid == uuid) {
				0
			} else {
				if (uuid == entry.killedBy) 1 else 0
			}
		}
	}

	fun nameMap(): Map<UUID, String> {
		return players.associate { (_, uuid, name) -> Pair(uuid, name) }
	}

	fun playersTeam(uuid: UUID): Team? {
		return teams.find { team -> team.members.contains(uuid) }
	}
}

