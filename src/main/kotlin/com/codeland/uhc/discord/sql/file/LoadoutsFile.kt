package com.codeland.uhc.discord.sql.file

import com.codeland.uhc.discord.sql.DatabaseFile
import com.codeland.uhc.lobbyPvp.Loadout
import com.codeland.uhc.lobbyPvp.Loadouts
import java.lang.StringBuilder
import java.sql.ResultSet
import java.util.*
import kotlin.collections.HashMap

class LoadoutsFile : DatabaseFile<Loadouts, LoadoutsFile.LoadoutEntry>() {
	class LoadoutEntry(val uuid: UUID, val slot: Int, val loadout: Loadout)

	override fun query(): String {
		//language=sql
		return "SELECT DISTINCT p.uuid, slot0, slot1, slot2 FROM PvpLoadout p\n" +
			"INNER JOIN (SELECT uuid, slot, loadoutData AS slot0 FROM PvpLoadout) p0 ON p.uuid = p0.uuid AND p0.slot = 0\n" +
			"INNER JOIN (SELECT uuid, slot, loadoutData AS slot1 FROM PvpLoadout) p1 ON p.uuid = p1.uuid AND p1.slot = 1\n" +
			"INNER JOIN (SELECT uuid, slot, loadoutData AS slot2 FROM PvpLoadout) p2 ON p.uuid = p2.uuid AND p2.slot = 2;"
	}

	override fun parseResults(results: ResultSet): Loadouts {
		/* a player may have only some of their loadout slots stored in the database */
		/* these will come back as null, and are initialized to the default loadout */
		val map = HashMap<UUID, Array<Loadout>>()

		while (results.next()) {
			val uuid = UUID.fromString(results.getString(0))
			val slot0 = results.getNString(1)
			val slot1 = results.getNString(2)
			val slot2 = results.getNString(3)

			map[uuid] = arrayOf(
				if (slot0 == null) Loadout.genDefault() else stringToLoadout(slot0),
				if (slot1 == null) Loadout.genDefault() else stringToLoadout(slot1),
				if (slot2 == null) Loadout.genDefault() else stringToLoadout(slot2)
			)
		}

		return Loadouts(map)
	}

	override fun defaultData(): Loadouts {
		return Loadouts(HashMap())
	}

	override fun pushQuery(entry: LoadoutEntry): String {
		//language=sql
		return "EXECUTE updateLoadout ${sqlString(entry.uuid)}, ${entry.slot}, ${sqlString(loadoutToString(entry.loadout))};"
	}

	companion object {
		/* the loadouts are stored in string like */
		/* [id]~[id][option]~[id]~[id]~ */
		/* a sequence of ids and maybe options terminated by a ~ */
		/* ids will be omitted if they are -1 */
		/* ids are encoded as ascii characters from ' ' (0) to '}' (105)*/
		/* options will be omitted if they are -1 */
		/* options are encoded as ascii characters from ' ' (0) to '}' (105)*/

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
	}
}
