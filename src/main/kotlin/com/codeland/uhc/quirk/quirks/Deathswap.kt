package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList
import kotlin.math.ceil

class Deathswap(uhc: UHC, type: QuirkType) : Quirk(uhc, type){
    companion object {
        var WARNING = 10 * 20
        var IMMUNITY = 10 * 20

        var taskId = 0
        var swapTime = 0

        val random = Random()

        fun doSwaps() {
            val teleportList = ArrayList<Location>()

            GameRunner.uhc.playerDataList.forEach { (uuid, playerData) ->
                if (playerData.participating && playerData.alive) {
                    teleportList.add(GameRunner.getPlayerLocation(uuid) ?: GameRunner.uhc.spectatorSpawnLocation())
                }
            }

            GameRunner.uhc.playerDataList.forEach { (uuid, playerData) ->
                if (playerData.participating && playerData.alive) {
                    GameRunner.teleportPlayer(uuid, teleportList.removeAt(Util.randRange(0, teleportList.lastIndex)))
                }
            }
        }

        fun resetTimer() {
            swapTime = ((ThreadLocalRandom.current().nextInt() % (60 * 20)) + IMMUNITY)
        }
    }

    override fun onEnable() {

    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTask(taskId)
    }

    private fun sendImmunity(percent: Float) {
        val message = StringBuilder("${ChatColor.GOLD}Immune ${ChatColor.GRAY}- ")

        for (i in 0 until (10 * percent).toInt())
            message.append(ChatColor.GOLD.toString() + "▮")

        for (i in 0 until 10 - (10 * percent).toInt())
            message.append(ChatColor.GRAY.toString() + "▮")

        sendAll(message.toString(), false)
    }

    override fun onPhaseSwitch(phase: PhaseVariant) {
        if (phase.type == PhaseType.GRACE) {
            resetTimer()

            taskId = SchedulerUtil.everyTick {
                --swapTime

                when {
                    swapTime <= 0 -> {
                        resetTimer()
                        sendAll("${ChatColor.GOLD}You are no longer immune.", false)
                    }
                    swapTime < IMMUNITY -> {
                        sendImmunity(swapTime / IMMUNITY.toFloat())
                    }
                    swapTime == IMMUNITY -> {
                        doSwaps()
                        sendAll("${ChatColor.GOLD}Swapped!", true)
                    }
                    swapTime < IMMUNITY + WARNING -> {
                        sendAll("${ChatColor.GOLD}Swapping in ${ChatColor.BLUE}${ceil(swapTime / 20.0)}${ChatColor.GOLD}...", true)
                    }
                }
            }
        } else if (phase.type == PhaseType.POSTGAME) {
            Bukkit.getScheduler().cancelTask(taskId)
        }
    }

    private fun sendAll(message: String, allowSpecs: Boolean) {
        GameRunner.uhc.playerDataList.forEach { (uuid, playerData) ->
            if ((playerData.participating && playerData.alive) || allowSpecs)
                Bukkit.getPlayer(uuid)?.sendActionBar(message)
        }
    }
}