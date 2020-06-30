package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Team
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

class UHC(startRadius: Double, endRadius: Double, graceTime: Double, shrinkTime: Double, glowTime : Double) {

	//time is measured in seconds here.

	private var startRadius = 0.0
	private var endRadius = 0.0
	private var graceTime = 0.0
	private var shrinkTime = 0.0
	private var glowTime = 0.0

	var glowType = 1

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

	fun setGlowTime(glowTime: Double) {
		this.glowTime = glowTime
	}

	fun start(commandSender : CommandSender, w : World) {
		w.players.forEach {
			it.inventory.clear()
			for (activePotionEffect in it.activePotionEffects) {
				it.removePotionEffect(activePotionEffect.type)
			}
			it.gameMode = GameMode.SURVIVAL

			it.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("wtf")[0])
		}
		w.setGameRule(GameRule.NATURAL_REGENERATION, true)
		w.setGameRule(GameRule.DO_MOB_SPAWNING, true)
		w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
		w.worldBorder.setCenter(0.0, 0.0)
		w.worldBorder.size = startRadius * 2

		val teamCount = 1 + Bukkit.getServer().scoreboardManager.mainScoreboard.teams.size

		Bukkit.getServer().dispatchCommand(commandSender, String.format("spreadplayers 0 0 %f %f true @a", (startRadius / sqrt(teamCount.toDouble())) * 0.25, startRadius))
		w.pvp = false

		var titleTimer = Timer()
		titleTimer.schedule(object : TimerTask() {
			var timeRemaining = graceTime.toLong()
			override fun run() {


				val introText = TextComponent("Grace period ends in ")
				var timeText : TextComponent
				if (timeRemaining + 1 == 1L) {
					timeText = TextComponent("1 second")
				} else if (timeRemaining < 60) {
					timeText = TextComponent("$timeRemaining seconds")
				} else {
					val minutes = timeRemaining / 60 + 1
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
				timeRemaining -= 1
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

		countdownToEvent(graceTime, "GRACE PERIOD ENDING")
	}

	fun endGrace(w : World) {
		w.setGameRule(GameRule.NATURAL_REGENERATION, false)
		w.pvp = true
		w.worldBorder.setSize(endRadius * 2, shrinkTime.toLong())
		w.players.forEach {
			it.sendMessage("Grace period has ended!")
		}

		var timer = Timer()
		GameRunner.phase = UHCPhase.SHRINKING
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
				val remainingTime = (shrinkTime - timePassed).toLong() + 1
				if (remainingTime == 1L) {
					timeText = TextComponent("1 second")
				} else if (remainingTime < 60) {
					timeText = TextComponent("$remainingTime seconds")
				} else {
					val mins = remainingTime / 60 + 1
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
				endShrinking(w, timer)
			}
		}, (shrinkTime * 1000.0).toLong())
		countdownToEvent(shrinkTime, "BORDER STOPPING")
	}

	fun endShrinking(w : World, shrinkingTimer : Timer) {
		w.players.forEach {
			it.sendMessage("Border has stopped moving!")
			it.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(""))
		}
		shrinkingTimer.cancel()
		countdownToEvent(glowTime, "GLOWING WILL BE APPLIED")
		if (glowTime > 0) {
			var runnable = object : BukkitRunnable() {
				var remainingTime = glowTime.toInt()
				override fun run() {
					if (remainingTime == 0) {
						cancel()
						startGlowing(w)
						return
					}
					val pre = TextComponent("Glowing starts in ")
					var timeComp : TextComponent
					if (remainingTime == 1) {
						timeComp = TextComponent("1 second")
					} else if (remainingTime < 60) {
						timeComp = TextComponent("$remainingTime seconds")
					} else {
						val mins = remainingTime / 60 + 1
						if (mins == 1) {
							timeComp = TextComponent("1 minute")
						} else {
							timeComp = TextComponent("$mins minutes")
						}
					}
					timeComp.color = ChatColor.AQUA
					for (player in w.players) {
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, pre, timeComp)
					}
					remainingTime -= 1
				}
			}
			if (GameRunner.plugin != null) {
				runnable.runTaskTimer(GameRunner.plugin!!, 0, 20)
			}
		}
	}

	fun startGlowing(w : World) {
		if (glowType == 0) {//general
			for (player in w.players) {
				player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Int.MAX_VALUE, 1, false, false, false))
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(""))
				val message = TextComponent("Glowing has started!")
				message.color = ChatColor.GOLD
				player.sendMessage(message)
			}
		} else if (glowType == 1) {//special
			for (player in w.players) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(""))
				val message = TextComponent("Glowing has started!")
				message.color = ChatColor.GOLD
				player.sendMessage(message)
			}
			var runnable = object : BukkitRunnable() {
				override fun run() {
					updateGlowing(w)
				}
			}
			runnable.runTaskTimer(GameRunner.plugin!!, 0, 10)
		}
		GameRunner.phase = UHCPhase.GLOWING
	}

	fun updateGlowing(w : World) {
		val remainingTeams = GameRunner.remainingTeams()
		var glowingTeam : Team? = null
		if (remainingTeams > 2) {
			glowingTeam = GameRunner.getHighestHPTeam()
		}
		for (player in w.players) {
			if (glowingTeam == null) {
				player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Int.MAX_VALUE, 1, false, false, false))
			} else if (glowingTeam == GameRunner.playersTeam(player.displayName)) {
				player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Int.MAX_VALUE, 1, false, false, false))
			} else {
				player.removePotionEffect(PotionEffectType.GLOWING)
			}
		}
	}

	fun updateMobCaps(w : World, borderRadius : Double) {
		// mobCap = constant × chunks ÷ 289
		// https://minecraft.gamepedia.com/Spawn#Java_Edition_mob_cap
		var total = 0
		var inBorder = 0
		w.loadedChunks.forEach {
			++total
			if (abs(it.x * 16) < borderRadius && abs(it.z * 16) < borderRadius) {
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
		setGlowTime(glowTime)
	}

	fun countdownToEvent(totalDelay : Double, subtitle : String) {
		val countdownRunnable = object  : BukkitRunnable() {
			var num = 3
			override fun run() {
				if (num != 0) {
					for (onlinePlayer in Bukkit.getServer().onlinePlayers) {
						onlinePlayer.sendTitle("" + num, subtitle, 0, 21, 0)
					}
				} else {
					cancel()
				}
				--num
			}
		}

		countdownRunnable.runTaskTimer(GameRunner.plugin!!, (totalDelay * 20.0 - 60.0).toLong(), 20)
	}
}

/*
countDowns

spawn protection warn

more flair when start
discord integration

border ot 0 in nether
glowing in nether
people joining with no team = spec
different border solutions
default to tm

*/