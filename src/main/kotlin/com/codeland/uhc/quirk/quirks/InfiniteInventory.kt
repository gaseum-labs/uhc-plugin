package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.HashMap

class InfiniteInventory(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {

    // todo: dying doesn't drop blaze rods
    // todo: dying drops all of your items
    // todo: make formatting better

    companion object {
        const val BACK_BUTTON = 9
        const val FORWARD_BUTTON = 17
        const val EMPTY_SLOT = 35

        var storeTask = 0

        private val storedMap: HashMap<UUID, InfInventory> = hashMapOf()

        fun getInventory(player: Player): InfInventory {
            if (storedMap[player.uniqueId] == null) {
                storedMap[player.uniqueId] = InfInventory(player)
            }
            return storedMap[player.uniqueId]!!
        }

        fun nextPage(player: Player) {
            getInventory(player).nextPage()
        }

        fun prevPage(player: Player) {
            getInventory(player).prevPage()
        }
    }

    class InfInventory(val player: Player) {
        private val pages: MutableList<Array<ItemStack?>> = mutableListOf(newPage())
        private var currentPage: Int = 0

        private fun getIndices(): List<Pair<Int, Int>> {
            val playerInventoryIndices = (9..35).filter {it !in listOf(BACK_BUTTON, FORWARD_BUTTON, EMPTY_SLOT)}
            return (0..23).map {Pair(it, playerInventoryIndices[it])}
        }

        private fun save() {
            val contents = player.inventory.contents
            for ((i, j) in getIndices()) {
                pages[currentPage][i] = contents[j]
            }
        }

        private fun restore() {
            val inventory = player.inventory
            for ((i, j) in getIndices()) {
                inventory.setItem(j, pages[currentPage][i])
            }
        }

        fun nextPage() {
            save()
            currentPage++
            if (currentPage >= pages.size - 1) {
                pages.add(newPage())
            }
            restore()
        }

        fun prevPage() {
            save()
            if (currentPage != 0) {
                currentPage--
                restore()
            }
        }

        private fun newPage(): Array<ItemStack?> {
            return Array(24) {null}
        }

        fun store(item: ItemStack) {
            for (i in pages.indices) {
                val page = pages[i]
                if (i == currentPage) save()
                for (j in 0..23) {
                    // todo: maybe it could stack onto other items of the same type,
                    // don't know how hard that would be
                    if (page[j] == null) {
                        page[j] = item
                        if (i == currentPage) restore()
                        return
                    }
                }
            }
            // if we've gotten to this point, all pages are full
            val newPage = newPage()
            newPage[0] = item
            pages.add(newPage)
        }
    }

    override fun onEnable() {
        storeTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
            for (player in Bukkit.getOnlinePlayers()) {
                val inventory = player.inventory
                if (inventory.getItem(EMPTY_SLOT) != null) {
                    getInventory(player).store(inventory.getItem(EMPTY_SLOT)!!)
                    inventory.setItem(EMPTY_SLOT, null)
                }
            }
        }, 1, 1)
    }

    override fun onStart(uuid: UUID) {
        GameRunner.playerAction(uuid) { player ->
            val inventory = player.inventory
            val back = ItemStack(Material.BLAZE_ROD)
            val meta = back.itemMeta
            meta.setDisplayName(ChatColor.RESET.toString() + "Previous Page")
            back.itemMeta = meta
            inventory.setItem(BACK_BUTTON, ItemStack(back))
            val forward = ItemStack(Material.BLAZE_ROD)
            val meta2 = forward.itemMeta
            meta2.setDisplayName(ChatColor.RESET.toString() + "Next Page")
            forward.itemMeta = meta2
            inventory.setItem(FORWARD_BUTTON, ItemStack(forward))
        }
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTask(storeTask)
        storedMap.clear()
    }
}