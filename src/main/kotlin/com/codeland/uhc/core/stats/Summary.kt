package com.codeland.uhc.core.stats

import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Result
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import it.unimi.dsi.fastutil.Hash
import org.apache.commons.collections4.multimap.HashSetValuedHashMap
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Summary(
	val gameType: GameType,
	val date: LocalDateTime,
	val gameLength: Int,
	val players: List<SummaryEntry>
) {
	data class SummaryEntry(
		val place: Int,
	    val uuid: UUID,
		val name: String,
		val timeSurvived: Int,
		val killedBy: UUID?,
	) {
		fun serialize(): HashMap<String, *> {
			return hashMapOf(
				Pair("place", place),
				Pair("uuid", uuid.toString()),
				Pair("name", name),
				Pair("timeSurvived", timeSurvived),
				Pair("killedBy", killedBy?.toString() ?: "null")
			)
		}

		companion object {
			fun deserialize(map: HashMap<String, *>): Result<SummaryEntry> {
				val place = map["place"] as? Int ?: return Bad(fieldError("place", "int"))

				val uuid = try {
					UUID.fromString(map["uuid"] as? String ?: return Bad(fieldError("uuid", "string")))
				} catch (ex: Exception) { return Bad(ex.message ?: "Unknown UUID error") }

				val name = map["name"] as? String ?: return Bad(fieldError("name", "string"))

				val timeSurvived = map["timeSurvived"] as? Int ?: return Bad(fieldError("timeSurvived", "int"))

				val killedByUuid = try {
					val killedBy = map["killedBy"] as? String?
					if(killedBy == null) null else UUID.fromString(killedBy)
				} catch (ex: Exception) { return Bad(ex.message ?: "Unknown UUID error") }

				return Good(SummaryEntry(place, uuid, name, timeSurvived, killedByUuid))
			}
		}
	}

	fun write(): String {
		return GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create()
			.toJson(hashMapOf(
				Pair("gameType", gameType.name),
				Pair("date", date.toString()),
				Pair("gameLength", gameLength),
				Pair("players", players.map { it.serialize() }),
			))
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

	companion object {
		private fun fieldError(name: String, type: String): String {
			return "No value for \"${name}\" <${type}> found"
		}

		fun readSummary(filename: String, inputStream: InputStream): Result<Summary> {
			val gson = try {
				Gson().fromJson(InputStreamReader(inputStream), HashMap::class.java) as HashMap<String, *>
			} catch (ex: Exception) { return Bad(ex.message ?: "Unknown JSON error") }

			val gameType = when(
				val res = GameType.fromString(gson["gameType"] as? String ?: return Bad(fieldError("gameType", "string")))
			) {
				is Good -> res.value
				is Bad -> return res.forward()
			}

			val date = try {
				LocalDateTime.parse(gson["date"] as? String ?: return Bad(fieldError("date", "string")))
			} catch (ex: Exception) { return Bad(ex.message ?: "Unknown date error") }

			val gameLength = gson["gameLength"] as? Int ?: return Bad(fieldError("gameLength", "int"))

			val players = (
				gson["players"] as? ArrayList<HashMap<String, *>>
					?: return Bad(fieldError("players", "array"))
			).map {
				when (val r = SummaryEntry.deserialize(it)) {
					is Good -> r.value
					is Bad -> return r.forward()
				}
			}

			return Good(Summary(gameType, date, gameLength, players))
		}
	}
}

