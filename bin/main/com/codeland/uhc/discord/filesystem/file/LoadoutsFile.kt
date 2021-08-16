package com.codeland.uhc.discord.filesystem.file

import com.codeland.uhc.discord.filesystem.DataManager.void
import com.codeland.uhc.discord.filesystem.DiscordFile
import com.codeland.uhc.lobbyPvp.Loadout
import com.codeland.uhc.lobbyPvp.Loadouts
import com.google.gson.*
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Integer.min
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class LoadoutsFile(header: String, channelName: String): DiscordFile<Loadouts>(header, channelName) {
	/* the loadouts are stored in string like */
	/* [id]~[id][option]~[id]~[id]~ */
	/* a sequence of ids and maybe options terminated by a ~ */
	/* ids will be omitted if they are -1 */
	/* ids are encoded as ascii characters from ' ' (0) to '}' (105)*/
	/* options will be omitted if they are -1 */
	/* options are encoded as ascii characters from ' ' (0) fto '}' (105)*/

	private fun loadoutToString(loadout: Loadout): String {
		val builder = StringBuilder(Loadout.LOADOUT_SIZE * 2)

		loadout.ids.indices.forEach { i ->
			val id = loadout.ids[i]
			if (id > -1) builder.append((id + 32).toChar())

			val option = loadout.options[i]
			if (option > -1) builder.append((option + 32).toChar())

			builder.append('~')
		}

		return builder.toString()
	}

	private fun stringToLoadout(string: String): Loadout {
		val loadout = Loadout()

		var loadoutIndex = 0
		var strIndex = 0

		while (strIndex < string.length) {
			val current = string[strIndex]

			if (current != '~') {
				loadout.ids[loadoutIndex] = (current - ' ')

				if (++strIndex < string.length) {
					val next = string[strIndex]

					if (next != '~') {
						loadout.options[loadoutIndex] = (next - ' ')

						if (++strIndex < string.length && string[strIndex] != '~') {
							throw Exception("Each loadout string section must be no longer than 2 characters")
						}
					}
				}
			}

			++strIndex
			++loadoutIndex
		}

		return loadout
	}

	override fun fromStream(stream: InputStream, onError: (String) -> Unit): Loadouts? {
		val uuids = ArrayList<UUID>()
		val loadouts = ArrayList<Array<Loadout>>()

		return try {
			val array = Gson().fromJson(InputStreamReader(stream), ArrayList::class.java) as ArrayList<Map<String, Any>>

			array.forEach { element ->
				uuids.add(UUID.fromString(element["id"] as String))

				val slotsElement = element["slots"] as ArrayList<String>
				if (slotsElement.size != Loadouts.NUM_SLOTS) return onError("There must be ${Loadouts.NUM_SLOTS} slots").void()

				val slots = Array(Loadouts.NUM_SLOTS) { i ->
					stringToLoadout(slotsElement[i])
				}

				slots.forEach {
					if (!it.validate()) return onError("Invalid loadout parsed").void()
				}

				loadouts.add(slots)
			}

			Loadouts(uuids, loadouts)

		} catch (ex: Exception) {
			onError(ex.message ?: "Unknown JSON error").void()
		}
	}

	override fun write(data: Loadouts): ByteArray {
		val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

		val array = ArrayList<Map<String, Any>>(data.uuids.size)

		data.uuids.indices.forEach { i ->
			val slots = ArrayList<String>(Loadouts.NUM_SLOTS)

			for (j in 0 until Loadouts.NUM_SLOTS) {
				slots.add(loadoutToString(data.loadouts[i][j]))
			}

			array.add(mapOf(
				Pair("id", data.uuids[i].toString()),
				Pair("slots", slots)
			))
		}

		return gson.toJson(array).toByteArray()
	}

	override fun defaultData(): Loadouts {
		return Loadouts(
			arrayListOf(UUID.randomUUID()),
			arrayListOf(Loadouts.defaultSlots())
		)
	}
}
