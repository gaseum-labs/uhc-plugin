package com.codeland.uhc.command

import org.bukkit.ChatColor
import org.bukkit.scoreboard.Scoreboard
import java.lang.Exception
import java.util.*

object TeamMaker {
	fun getTeamsRandom(names: ArrayList<String>, teamSize: Int): Array<Array<String?>> {
		val ret = ArrayList<Array<String?>>()
		while (names.size > 0) {
			val tem = arrayOfNulls<String>(teamSize)
			for (i in tem.indices) {
				if (names.size > 0) {
					val rand = (Math.random() * names.size).toInt()
					tem[i] = names.removeAt(rand)
				}
			}
			ret.add(tem)
		}
		return ret.toArray(arrayOf())
	}

	/**
	 * will return null if color list could not be created
	 */
	fun getColorList(size: Int, scoreboard: Scoreboard): Array<ChatColor>? {
		if (size > TeamData.teamColours.size - scoreboard.teams.size)
			return null;

		var availableColors = arrayOfNulls<ChatColor>(TeamData.teamColours.size);
		var numAvailableColors = 0;

		TeamData.teamColours.forEach { color ->
			var available = !scoreboard.teams.any { team ->
				if (color == team.color)
					return@any true;

				return@any false;
			}

			availableColors[numAvailableColors] = color;
			++numAvailableColors;
		}

		var ret = arrayOfNulls<ChatColor>(size);

		ret.forEachIndexed { i, _ ->
			var position = (Math.random() * numAvailableColors) as Int;
			var usingColor = null as ChatColor;

			while (availableColors[position] == null) {
				++position;
				position %= numAvailableColors;
			}

			availableColors[position] = null;
			ret[i] = usingColor;
		}

		return try {
			ret.requireNoNulls();
		} catch (ex: Exception) {
			null;
		}
	}
}