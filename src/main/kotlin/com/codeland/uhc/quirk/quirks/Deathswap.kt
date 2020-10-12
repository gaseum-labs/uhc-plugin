package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class Deathswap(uhc: UHC, type: QuirkType) : Quirk(uhc, type){
    companion object {

        var WARNING = 10000
        var IMMUNITY = 10000

        var taskId = 0
        var lastSwap = 0L
        var swapTime = 0L
        val random = Random()
        // average # of seconds between swaps
        var average = 60
        var lastAnnounced = WARNING / 1000L - 1

        var immunityEndAnnouncement = true
        var hasSwappedOnce = false


        fun swap() {
            val playerList = Bukkit.getOnlinePlayers()
            if (playerList.isEmpty()) return
            val newList = playerList.toList().shuffled()
            val tempLocation = newList[0].location

            newList.subList(0, newList.size - 1).forEachIndexed { index, player ->
                player.teleport(newList[index + 1])
            }
            newList.last().teleport(tempLocation)
            updateSwapVars()
            immunityEndAnnouncement = false
            hasSwappedOnce = true
        }

        fun updateSwapVars() {
            lastSwap = System.currentTimeMillis()
            swapTime = ThreadLocalRandom.current().nextLong(average * 1000L * 2 + WARNING + 1000)
        }

        fun timeLeft() = swapTime - (System.currentTimeMillis() - lastSwap)
        fun elapsed() = System.currentTimeMillis() - lastSwap
    }

    override fun onEnable() {
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTask(taskId)
    }

    override fun onPhaseSwitch(phase: PhaseVariant) {
        if (phase.type == PhaseType.GRACE) {
            updateSwapVars()
            taskId = SchedulerUtil.everyTick {
                if (!GameRunner.uhc.isGameGoing())
                    return@everyTick
                if (timeLeft() < 0) {
                    sendAll("${ChatColor.GOLD}Swapped!")
                    swap()
                    lastAnnounced = WARNING / 1000L - 1
                } else if (timeLeft() / 1000L == lastAnnounced) {
                    sendAll("${ChatColor.GOLD}Swapping in ${ChatColor.BLUE}${timeLeft()/1000 + 1}${ChatColor.GOLD}...")
                    lastAnnounced--
                }
                if (elapsed() < IMMUNITY && hasSwappedOnce) {
                    val bars = StringBuilder()
                    val immunityLeft = (IMMUNITY - elapsed())
                    val percent = (immunityLeft/IMMUNITY.toDouble())
                    for (i in 0 until (10 * percent).toInt())
                        bars.append(ChatColor.GOLD.toString() + "▮")
                    for (i in 0 until 10 - (10 * percent).toInt())
                        bars.append(ChatColor.GRAY.toString() + "▮")
                    for (player in Bukkit.getOnlinePlayers().filter {it.gameMode == GameMode.SURVIVAL}) {
                        player.sendActionBar("${ChatColor.GOLD}Immune ${ChatColor.GRAY}- $bars")
                    }
                } else if (!immunityEndAnnouncement) {
                    for (player in Bukkit.getOnlinePlayers().filter {it.gameMode == GameMode.SURVIVAL}) {
                        player.sendActionBar("${ChatColor.GOLD}Immune ${ChatColor.GRAY}- ▮▮▮▮▮▮▮▮▮▮")
                        player.sendMessage("${ChatColor.GOLD}You are no longer immune.")
                    }
                    immunityEndAnnouncement = true
                }
            }
        }
    }
    private fun sendAll(message: String) {
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage(message)
        }
    }
}