package com.codeland.uhc.core.stats

import com.codeland.uhc.discord.filesystem.DiscordFilesystem.fieldError
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Result
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import it.unimi.dsi.fastutil.Hash
import org.apache.commons.collections4.multimap.HashSetValuedHashMap
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Summary(
	val gameType: GameType,
	val date: ZonedDateTime,
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
				Pair("killedBy", killedBy?.toString())
			)
		}

		companion object {
			fun deserialize(map: AbstractMap<String, *>): Result<SummaryEntry> {
				val place = (map["place"] as? Double ?: return fieldError("place", "int")).toInt()

				val uuid = try {
					UUID.fromString(map["uuid"] as? String ?: return fieldError("uuid", "string"))
				} catch (ex: Exception) { return Bad(ex.message ?: "Unknown UUID error") }

				val name = map["name"] as? String ?: return fieldError("name", "string")

				val timeSurvived = (map["timeSurvived"] as? Double ?: return fieldError("timeSurvived", "int")).toInt()

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
		fun readSummary(inputStream: InputStream): Result<Summary> {
			val gson = try {
				Gson().fromJson(InputStreamReader(inputStream), HashMap::class.java) as HashMap<String, *>
			} catch (ex: Exception) { return Bad(ex.message ?: "Unknown JSON error") }

			val gameType = when(
				val res = GameType.fromString(gson["gameType"] as? String ?: return fieldError("gameType", "string"))
			) {
				is Good -> res.value
				is Bad -> return res.forward()
			}

			val date = try {
				ZonedDateTime.parse(gson["date"] as? String ?: return fieldError("date", "string"))
			} catch (ex: Exception) { return Bad(ex.message ?: "Unknown date error") }

			val gameLength = (gson["gameLength"] as? Double ?: return fieldError("gameLength", "int")).toInt()

			val players = (
				gson["players"] as? ArrayList<AbstractMap<String, *>>
					?: return fieldError("players", "array")
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

