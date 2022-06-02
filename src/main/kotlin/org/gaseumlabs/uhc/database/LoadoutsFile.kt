package org.gaseumlabs.uhc.database

import org.gaseumlabs.uhc.lobbyPvp.Loadout

object LoadoutsFile {
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
