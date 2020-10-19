package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import java.util.*

class Betrayal(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {

	override fun onEnable() {}

	override fun onDisable() {}

	companion object {
		class BetrayalData(var swaps : Int, var kills : Int);
		fun getPlayerData(player: Player):BetrayalData{
			val dataList = player.getMetadata("BetrayalData")
			if(dataList.isEmpty()) {
				val data = BetrayalData(0,0)
				player.setMetadata("BetrayalData", FixedMetadataValue(UHCPlugin.plugin, data))
				return data
			}
			return dataList[0].value() as BetrayalData
		}

		fun calculateScores(team : ArrayList<UUID>):ArrayList<Pair<UUID, Int>>{
			val scoreList = ArrayList<Pair<UUID, Int>>()
			team.forEach{member ->
				val player = Bukkit.getPlayer(member)
				if(player != null) {
					val data = getPlayerData(player)
					val score = data.kills - data.swaps
					scoreList.add(Pair(member, score))
				}
			}
			scoreList.sortBy { pair -> pair.second }
			return scoreList
		}

	}
}