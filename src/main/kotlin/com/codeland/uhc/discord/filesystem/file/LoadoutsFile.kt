package com.codeland.uhc.discord.filesystem.file

import com.codeland.uhc.discord.filesystem.DataManager.void
import com.codeland.uhc.discord.filesystem.DiscordFile
import com.codeland.uhc.lobbyPvp.Loadouts
import com.google.gson.*
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Integer.min
import java.util.*
import kotlin.collections.ArrayList

class LoadoutsFile(header: String, channelName: String): DiscordFile<Loadouts>(header, channelName) {
	override fun fromStream(stream: InputStream, onError: (String) -> Unit): Loadouts? {
		val uuids = ArrayList<UUID>()
		val loadouts = ArrayList<Array<Array<Int>>>()

		return try {
			val array = Gson().fromJson(InputStreamReader(stream), ArrayList::class.java) as ArrayList<Map<String, Any>>

			array.forEach { element ->
				uuids.add(UUID.fromString(element["id"] as String))

				val slotsElement = element["slots"] as ArrayList<ArrayList<Double>>

				val slots = Loadouts.defaultArray()

				for (i in 0 until min(slotsElement.size, Loadouts.NUM_SLOTS)) {
					for (j in 0 until min(slotsElement[i].size, Loadouts.LOADOUT_SIZE)) {
						slots[i][j] = slotsElement[i][j].toInt()
					}
				}

				loadouts.add(slots)
			}

			Loadouts(uuids, loadouts)

		} catch (ex: Exception) {
			onError(ex.message ?: "Unknown JSON error").void()
		}
	}

	override fun write(data: Loadouts): ByteArray {
		val gson = GsonBuilder().setPrettyPrinting().create()

		val array = ArrayList<Map<String, Any>>(data.uuids.size)

		data.uuids.indices.forEach { i ->
			val slots = ArrayList<ArrayList<Double>>(Loadouts.NUM_SLOTS)

			for (j in 0 until Loadouts.NUM_SLOTS) {
				val inventory = ArrayList<Double>(Loadouts.LOADOUT_SIZE)

				for (k in 0 until Loadouts.LOADOUT_SIZE) {
					inventory.add(data.loadouts[i][j][k].toDouble())
				}

				slots.add(inventory)
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
			arrayListOf(Loadouts.defaultArray())
		)
	}
}
