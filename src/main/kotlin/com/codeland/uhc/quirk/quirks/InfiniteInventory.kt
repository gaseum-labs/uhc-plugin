package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
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
            addButtons(player)
        }

        fun prevPage(player: Player) {
            getInventory(player).prevPage()
            addButtons(player)
        }

        fun modifyDrops(drops: MutableList<ItemStack>, player: Player) {
            drops.removeAll { itemStack ->
                itemStack.type == Material.FEATHER && itemStack.itemMeta.hasDisplayName()
            }
            drops.addAll(getInventory(player).getAllOtherItems())
            getInventory(player).resetPages()
            addButtons(player)
        }


        fun addButtons(player: Player) {
            val inventory = player.inventory
            val infinventory = getInventory(player)
            val back = ItemStack(Material.FEATHER)
            val meta = back.itemMeta
            meta.setDisplayName(ChatColor.RESET.toString() + "Previous Page (${infinventory.currentPage + 1})")
            back.itemMeta = meta
            inventory.setItem(BACK_BUTTON, ItemStack(back))
            val forward = ItemStack(Material.FEATHER)
            val meta2 = forward.itemMeta
            meta2.setDisplayName(ChatColor.RESET.toString() + "Next Page (${infinventory.currentPage + 1})")
            forward.itemMeta = meta2
            inventory.setItem(FORWARD_BUTTON, ItemStack(forward))
        }
    }

    class InfInventory(val player: Player) {
        val pages: MutableList<Array<ItemStack?>> = mutableListOf(newPage())
        var currentPage: Int = 0

        private fun getIndices(): List<Pair<Int, Int>> {
            val playerInventoryIndices = (9..35).filter {it !in listOf(BACK_BUTTON, FORWARD_BUTTON, EMPTY_SLOT)}
            return (0..23).map {Pair(it, playerInventoryIndices[it])}
        }

        fun getAllOtherItems(): List<ItemStack> {
            val list: MutableList<ItemStack> = mutableListOf()
            for (i in 0 until pages.size) {
                if (i != currentPage) {
                    for (j in pages[i]) {
                        if (j != null) list.add(j)
                    }
                }
            }
            return list
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

        fun resetPages() {
            pages.clear()
            pages.add(newPage())
            currentPage = 0
        }
    }

    override fun onEnable() {
        storeTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
            PlayerData.playerDataList
                    .filter { (_, data) -> data.participating}
                    .forEach { (uuid, _) ->
                    GameRunner.playerAction(uuid) { player ->

                val inventory = player.inventory
                if (inventory.getItem(EMPTY_SLOT) != null) {
                    getInventory(player).store(inventory.getItem(EMPTY_SLOT)!!)
                    inventory.setItem(EMPTY_SLOT, null)
                }
                if (
                   inventory.getItem(BACK_BUTTON)?.type != Material.FEATHER
                || inventory.getItem(FORWARD_BUTTON)?.type != Material.FEATHER) {
                    // this should almost never happen
                    addButtons(player)
                }
            }}
        }, 1, 1)
    }

    override fun onStart(uuid: UUID) {
        GameRunner.playerAction(uuid) { player ->
            addButtons(player)
        }
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTask(storeTask)
        storedMap.clear()
    }

    override val representation: ItemStack
        get() = ItemStack(Material.FEATHER)
}