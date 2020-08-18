package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

class SharedInventory(type: QuirkType) : Quirk(type) {
    companion object {
        lateinit var contents: Array<out ItemStack?>
        var taskId: Int = 0
    }

    override fun onEnable() {
        contents = Bukkit.getOnlinePlayers().iterator().next().inventory.contents
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameRunner.plugin, {
            for (player in Bukkit.getOnlinePlayers()) {
                val playersContents = player.inventory.contents
                if (!contentsSimilar(contents, playersContents)) {
                    contents = contentsCopy(playersContents)
                    for (other in Bukkit.getOnlinePlayers()) {
                        if (other != player) other.inventory.contents = contents as Array<out @org.jetbrains.annotations.NotNull ItemStack>
                    }
                    break
                }
            }
        }, 1, 1)
    }
    private fun contentsSimilar(contents1: Array<out ItemStack?>, contents2: Array<out ItemStack?>): Boolean {
        for (i in contents1.indices) {
            if (contents1[i] == null && contents2[i] != null) return false
            if (contents1[i] != null && contents2[i] == null) return false
            if (contents1[i] == null && contents2[i] == null) continue
            if (contents1[i] != contents2[i]) return false
        }
        return true
    }

    private fun contentsCopy(contents: Array<out ItemStack?>): Array<out ItemStack?> {
        val newContents = arrayOfNulls<ItemStack>(contents.size)
        for (i in contents.indices) {
            newContents[i] = if (contents[i] == null) null else contents[i]!!.clone()
        }
        return newContents
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTask(taskId)
    }
}