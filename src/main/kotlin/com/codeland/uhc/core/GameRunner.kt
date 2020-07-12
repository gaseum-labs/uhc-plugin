package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.quirk.Pests
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.postgame.PostgameDefault
import com.codeland.uhc.quirk.Quirk
import com.destroystokyo.paper.utils.PaperPluginLogger
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import java.util.logging.Level

class GameRunner(uhc: UHC, plugin: UHCPlugin, bot: MixerBot) {

	init {
		GameRunner.uhc = uhc
		GameRunner.plugin = plugin
		GameRunner.bot = bot
	}

	companion object {
		lateinit var uhc: UHC
		lateinit var plugin: UHCPlugin
		lateinit var bot: MixerBot

		fun teamIsAlive(team: Team): Boolean {
			return team.entries.any { entry ->
				val player = Bukkit.getServer().getPlayer(entry)

				when {
					player == null -> false
					Quirk.PESTS.enabled && Pests.isPest(player) -> false
					player.gameMode == GameMode.SURVIVAL -> true
					else -> false
				}
			}
		}

		/**
		 * returns both the number of remaining teams
		 * and the last remaining team if there is exactly 1
		 */
		fun remainingTeams(focus: Team) : Triple<Int, Team?, Boolean> {
			var retRemaining = 0
			var retAlive = null as Team?
			var retFocus = false

			Bukkit.getServer().scoreboardManager.mainScoreboard.teams.forEach { team ->
				val alive = teamIsAlive(team)

				if (team == focus) retFocus = alive;

				if (alive) {
					++retRemaining
					retAlive = team
				}
			}

			/* only give last alive if only one team is alive */
			return Triple(retRemaining, if (retRemaining == 1) retAlive else null, retFocus)
		}

		fun quickRemainingTeams() : Int {
			var retRemaining = 0

			Bukkit.getServer().scoreboardManager.mainScoreboard.teams.forEach { team ->
				if (teamIsAlive(team))
					++retRemaining
			}

			/* only give last alive if only one team is alive */
			return retRemaining;
		}

		fun playerDeath(deadPlayer: Player) {
			var aliveTeam: Team? = null

			val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

			var deadPlayerTeam = playersTeam(deadPlayer.name)
					?: return

			var (remainingTeams, lastRemaining, teamIsAlive) = remainingTeams(deadPlayerTeam)

			/* broadcast elimination message */
			if (!teamIsAlive) {
				val message = TextComponent("${deadPlayerTeam.displayName} has been Eliminated!")
				message.color = ChatColor.GOLD
				message.isBold = true

				val message2 = TextComponent("$remainingTeams teams remain")
				message2.color = ChatColor.GOLD
				message2.isBold = true

				Bukkit.getServer().onlinePlayers.forEach { player ->
					player.sendMessage(message)
					player.sendMessage(message2)
				}
			}

			/* uhc ending point (stops kill reward) */
			if (lastRemaining != null)
				return endUHC(lastRemaining)

			/* kill reward awarding */
			val killer = deadPlayer.killer
			if (killer != null) {
				val killerTeam = playersTeam(killer.name)
						?: return

				uhc.killReward.applyReward(killerTeam)
			}
		}

		fun playersTeam(playerName: String) : Team? {
			return Bukkit.getServer().scoreboardManager.mainScoreboard.getEntryTeam(playerName);
		}

		fun endUHC(winner: Team) {
			uhc.startPhase(PhaseType.POSTGAME) { phase ->
				phase as PostgameDefault

				phase.winningTeam = winner
			}
		}

		fun sendPlayer(player: Player, message: String) {
			val comp = TextComponent(message)
			comp.color = ChatColor.GOLD
			comp.isBold = true
			player.sendMessage(comp)
		}

		fun netherIsAllowed() : Boolean {
			return !(uhc.netherToZero && (uhc.isPhase(PhaseType.FINAL) || uhc.isPhase(PhaseType.GLOWING) || uhc.isPhase(PhaseType.ENDGAME)))
		}

		fun log(message: String) {
			PaperPluginLogger.getGlobal().log(Level.INFO, message)
		}

		fun topBlockY(world: World, x: Int, z: Int): Int {
			for (y in 255 downTo 0) {
				var block = world.getBlockAt(x, y, z)

				if (!block.isPassable)
					return y
			}

			return 0
		}
	}
}
