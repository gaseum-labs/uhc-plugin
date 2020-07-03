package com.codeland.uhc.core

import com.codeland.uhc.phaseType.*
import com.codeland.uhc.phases.Phase
import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import java.util.logging.Level
import kotlin.math.max
import kotlin.math.min

class UHC(startRadius: Double, endRadius: Double, graceTime: Long, shrinkTime: Long, waitTime : Long, glowTime : Long) {

	//time is measured in seconds here.

	var startRadius = startRadius
	var endRadius = endRadius

	var phaseDurations = arrayOf(graceTime, shrinkTime, waitTime, glowTime)
	var graceType = GraceType.DEFAULT
	var shrinkType = ShrinkType.DEFAULT
	var finalType = FinalType.DEFAULT
	var glowType = GlowType.DEFAULT
	var endgameType = EndgameType.NONE

	var netherToZero = true
	var mobCapCoefficient = 1.0

	var gameMaster : CommandSender? = null

	var currentPhase : Phase? = null

	fun start(commandSender : CommandSender) {

		gameMaster = commandSender

		currentPhase = graceType.startPhase(this, phaseDurations[0])

		/*val teamCount = 1 + Bukkit.getServer().scoreboardManager.mainScoreboard.teams.size

		Bukkit.getServer().dispatchCommand(commandSender, String.format("spreadplayers 0 0 %f %f true @a", (startRadius / sqrt(teamCount.toDouble())), startRadius))

		countdownToEvent(graceTime, "GRACE PERIOD ENDING")

		object : BukkitRunnable() {
			var timeRemaining = graceTime.toLong()
			override fun run() {
				if (timeRemaining < 1L) {
					cancel()
					for (player in Bukkit.getServer().onlinePlayers) {
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(""))
					}
					endGrace()
					return
				}

				val introText = TextComponent("Grace period ends in ")
				val timeText : TextComponent =
						if (timeRemaining + 1 == 1L) {
							TextComponent("1 second")
						} else if (timeRemaining < 60) {
							TextComponent("$timeRemaining seconds")
						} else {
							val minutes = timeRemaining / 60 + 1
							if (minutes == 1L) {
								TextComponent("1 minute")
							} else {
								TextComponent("$minutes minutes")
							}
						}
				timeText.color = ChatColor.AQUA
				timeText.isBold = true
				for (player in Bukkit.getServer().onlinePlayers) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, introText, timeText)
				}
				timeRemaining -= 1
			}
		}.runTaskTimer(GameRunner.plugin!!, 0, 20)*/
	}

	fun startNextPhase() {
		if (currentPhase?.getPhaseType() == UHCPhase.GRACE) {
			currentPhase = shrinkType.startPhase(this, phaseDurations[1])
		} else if (currentPhase?.getPhaseType() == UHCPhase.SHRINKING) {
			currentPhase = finalType.startPhase(this, phaseDurations[2])
		} else if (currentPhase?.getPhaseType() == UHCPhase.FINAL) {
			currentPhase = glowType.startPhase(this, phaseDurations[3])
		} else if (currentPhase?.getPhaseType() == UHCPhase.GLOWING) {
			currentPhase = endgameType.startPhase(this)
		}
	}

	fun updateMobCaps() {
		// mobCap = constant × chunks ÷ 289
		// https://minecraft.gamepedia.com/Spawn#Java_Edition_mob_cap
		for (world in Bukkit.getServer().worlds) {
			var total = 0.0
			var inBorder = 0.0
			for (chunk in world.loadedChunks) {
				++total
				val width = min(world.worldBorder.size, chunk.x * 16.0 + 16.0) - max(-world.worldBorder.size, chunk.x * 16.0)
				val height = min(world.worldBorder.size, chunk.z * 16.0 + 16.0) - max(-world.worldBorder.size, chunk.z * 16.0)
				if (width < 0 || height < 0) {
					continue
				}
				inBorder += width * height / 256.0
			}
			val coeff = inBorder / total
			if (world.environment == World.Environment.NORMAL) {
				PaperPluginLogger.getGlobal().log(Level.INFO, "$inBorder / $total")
				PaperPluginLogger.getGlobal().log(Level.INFO, "$coeff")
			}
			world.monsterSpawnLimit = (70 * coeff * mobCapCoefficient).toInt() + 1
			world.animalSpawnLimit = (10 * coeff * mobCapCoefficient).toInt() + 1
			world.ambientSpawnLimit = (15 * coeff * mobCapCoefficient).toInt() + 1
			world.waterAnimalSpawnLimit = (5 * coeff * mobCapCoefficient).toInt() + 1
		}
	}

}

/*
spawn protection warn

discord integration

different border solutions

special team effects

*/