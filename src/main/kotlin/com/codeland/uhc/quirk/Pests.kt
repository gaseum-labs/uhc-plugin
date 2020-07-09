package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin

object Pests {
    private var META_TAG = "isPest"

    fun isPest(player: Player): Boolean {
        var meta = player.getMetadata(META_TAG)

        return meta.size != 0 && meta[0].asBoolean()
    }

    fun makePest(player: Player) {
        player.setMetadata(META_TAG, FixedMetadataValue(GameRunner.plugin as Plugin, true))
    }
}