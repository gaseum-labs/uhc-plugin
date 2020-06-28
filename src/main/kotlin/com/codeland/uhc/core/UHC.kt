package com.codeland.uhc.core

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.command.CommandSender
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

class UHC(startRadius: Double, endRadius: Double, graceTime: Double, shrinkTime: Double) {

	//time is measured in seconds here.

	private var startRadius = 0.0
	private var endRadius = 0.0
	private var graceTime = 0.0
	private var shrinkTime = 0.0

	fun setRadius(startRadius: Double, endRadius: Double) {
		this.startRadius = startRadius
		this.endRadius = endRadius
	}

	fun setGraceTime(graceTime: Double) {
		this.graceTime = graceTime
	}

	fun setShrinkTime(shrinkTime: Double) {
		this.shrinkTime = shrinkTime
	}

	fun start(commandSender : CommandSender, w : World) {
		w.players.forEach {
			it.inventory.clear()
			it.activePotionEffects.clear()
			it.gameMode = GameMode.SURVIVAL
			it.sendTitle("main", "subtitle", 0, 20, 0)

			it.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("wtf")[0])
		}
		w.setGameRule(GameRule.NATURAL_REGENERATION, true)
		w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
		w.worldBorder.setCenter(0.0, 0.0)
		w.worldBorder.size = startRadius * 2

		val teamCount = 1 + Bukkit.getServer().scoreboardManager.mainScoreboard.teams.size

		Bukkit.getServer().dispatchCommand(commandSender, String.format("spreadplayers 0 0 %f %f true @a", (startRadius / sqrt(teamCount.toDouble())) * 0.75, startRadius))
		w.pvp = false

		var titleTimer = Timer()
		titleTimer.schedule(object : TimerTask() {
			var timeRemaining = graceTime.toLong()
			override fun run() {

				timeRemaining -= 1

				val introText = TextComponent("Grace period ends in ")
				var timeText : TextComponent
				if (timeRemaining == 1L) {
					timeText = TextComponent("1 second")
				} else if (timeRemaining < 60) {
					timeText = TextComponent("$timeRemaining seconds")
				} else {
					val minutes = timeRemaining / 60
					if (minutes == 1L) {
						timeText = TextComponent("1 minute")
					} else {
						timeText = TextComponent("$minutes minutes")
					}
				}
				timeText.color = ChatColor.AQUA
				timeText.isBold = true
				for (player in w.players) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, introText, timeText)
				}
			}
		}, 0, 1000)
		Timer().schedule(object : TimerTask() {
			override fun run() {
				titleTimer.cancel()
				endGrace(w)
				for (player in w.players) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(""))
				}
			}
		}, (graceTime * 1000.0).toLong())
	}

	fun endGrace(w : World) {
		w.setGameRule(GameRule.NATURAL_REGENERATION, false)
		w.pvp = true
		w.worldBorder.setSize(endRadius * 2, shrinkTime.toLong())
		w.players.forEach {
			it.sendMessage("Grace period has ended!")
		}

		var timer = Timer()
		timer.schedule(object  : TimerTask() {
			var border = startRadius
			var timePassed = 0
			override fun run() {
				updateMobCaps(w, border)
				border -= (startRadius - endRadius) / shrinkTime
				timePassed += 1

				val introText = TextComponent("World border radius is ")
				val borderRadText = TextComponent("" + border.toInt())
				borderRadText.isBold = true
				borderRadText.color = ChatColor.AQUA
				val minutesIntroText = TextComponent(", and reaches ")
				val minutesText = TextComponent("" + endRadius.toInt())
				minutesText.isBold = true
				minutesText.color = ChatColor.AQUA
				val connector = TextComponent(" in ")
				val timeText : TextComponent
				val remainingTime = (shrinkTime - timePassed).toLong()
				if (remainingTime == 1L) {
					timeText = TextComponent("1 second")
				} else if (remainingTime < 60) {
					timeText = TextComponent("$remainingTime seconds")
				} else {
					val mins = remainingTime / 60
					if (mins == 1L) {
						timeText = TextComponent("1 minute")
					} else {
						timeText = TextComponent("$mins minutes")
					}
				}
				timeText.color = ChatColor.AQUA
				timeText.isBold = true

				w.players.forEach {
					it.spigot().sendMessage(ChatMessageType.ACTION_BAR, introText, borderRadText, minutesIntroText, minutesText, connector, timeText)
				}
			}
		}, 0, 1000)
		Timer().schedule(object : TimerTask() {
			override fun run() {
				w.players.forEach {
					it.sendMessage("Border has stopped moving!")
					it.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(""))
				}
				timer.cancel()
			}
		}, (shrinkTime * 1000.0).toLong())
	}

	fun updateMobCaps(w : World, borderRadius : Double) {
		// mobCap = constant × chunks ÷ 289
		// https://minecraft.gamepedia.com/Spawn#Java_Edition_mob_cap
		var total = 0
		var inBorder = 0
		w.loadedChunks.forEach {
			++total
			if (abs(it.x) < borderRadius && abs(it.z) < borderRadius) {
				++inBorder
			}
		}
		val coeff = inBorder.toDouble() / total.toDouble()
		w.monsterSpawnLimit = (70 * coeff).toInt() + 1
		w.animalSpawnLimit = (10 * coeff).toInt() + 1
		w.ambientSpawnLimit = (15 * coeff).toInt() + 1
		w.waterAnimalSpawnLimit = (5 * coeff).toInt() + 1
	}

	init {
		setRadius(startRadius, endRadius)
		setGraceTime(graceTime)
		setShrinkTime(shrinkTime)
	}
}

/*
more flair when start
discord integration
spawn protection warn
hp shown on scoreboard
 */